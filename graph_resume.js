/**
 * User: rob
 * Date: 3/29/13
 * Time: 8:16 AM
 */
GraphLayout = function( width, height, linkDist ) {
    "use strict";

    // constants for accessing the resume data
    var ROOT = "person";
    var GROUP = "group";
    var CATEGORY = "category";
    var CATEGORIES = "categories";
    var CHILDREN = "children";
    var HIDDEN_CHILDREN = "_children";
    var NAME = "name";
    var DISPLAY_AS = "displayAs";
    var ITEMS = "items";

    var LINE_WRAP = 35;

    /**
     * Defines how the sub-nodes of the specified categories will be displayed.
     * @see setNodeFormat to add, update formatting
     * @type {Object}
     */
    var nodeNames = {
        "education": function( node ) {
            return node[ "degree" ] + ", " + node[ "major" ] + " (" + node[ "end" ] + ")";
        },
        "positions": function( node ) {
            return node[ "short_title" ];
        },
        "accomplishments": function( node ) {
            return node[ "description" ];
        }
    };

    var linkStyleColor;
    var linkStyleWidth;

    width = width || 1050;
    height = height || 900;
    linkDist = linkDist || 200;
    var charge = -8 * linkDist;

    var svg = d3.select("body").append("svg")
        .attr("width", width)
        .attr("height", height);

    var force = d3.layout.force()
        .on( "tick", tick )
        .charge( charge )
        .linkDistance( linkDist )
        .size( [width, height] )
        .friction(.88 );

    var node, enterNode, link, enterLink, json;

    /*--------------------------------------------------------------------

                    P U B L I C   M E T H O D S

     --------------------------------------------------------------------*/

    /**
     * Loads the resume from the specified URL, converts the JSON resume
     * data into a directed-graph structure, creates the d3 force-layout,
     * sets up the start view to hold only the categories, and sets up
     * the view for interaction
     * @param jsonUrl the URL to the location of the JSON representation of
     * the resume
     */
    var load = function( jsonUrl ) {
        d3.json( jsonUrl, function( error, resume ) {
            // check for xhr errors
            if (error) {
                console.log(error);
                return;
            }

            // convert the data to the format needed by the force layout
            // i.e. into a graph representation of the resume with nodes
            // and edges and parent nodes and child nodes.
            json = convert( resume );

            update();
            startView( json );
            update();
        } );
    };

    /**
     * Sets the format with which sub-nodes of the specified category will
     * be displayed. For example, if the category is specified as "eduction",
     * and then the specified callback can define how the sub-nodes of
     * eduction will be displayed.
     *
     * By default "education" sub-nodes are displayed using the following
     * callback function:
     * function( node ) {
     *       return node[ "degree" ] + ", " + node[ "major" ] + " (" + node[ "end" ] + ")";
     *   }
     *
     * @param category The category for which sub-node will be formatted
     * @param callback The function defining the format with which the sub-nodes
     * of the specified category will be displayed.
     */
    var setNodeFormat = function( category, callback ) {
        nodeNames[ category ] = callback;
    };


    var removeNodeFormat = function( category ) {
        delete nodeNames[ category ];
    };

    /*--------------------------------------------------------------------

                    P R I V A T E   M E T H O D S

     --------------------------------------------------------------------*/

    var nodes, links;
    /**
     * Performs a complete update of the nodes and links by setting the
     * nodes' children visible or hidden and updates the links between dependent
     * nodes.
     */
    function update() {

        nodes = flatten( json );
        links = d3.layout.tree().links( nodes );

        var additionalLinks = linkDependencies( nodes );
        links = links.concat( additionalLinks );

        force.nodes(nodes).links(links).start();

        lightUpdate();
    }

    /**
     * Doesn't update the structure of the nodes, just the display aspects when
     * the structure or data attributes change
     */
    function lightUpdate() {

        link = svg.selectAll(".link").data(links);
        link//.transition()
            .style( "stroke", function( d ) {
                return linkColor( d, this );
            })
            .style( "stroke-width", function( d ) {
                return linkWidth( d, this );
            });

        enterLink = link.enter().insert("line", ".node")
            .attr("class", "link");

        link.exit().remove();

        node = svg.selectAll(".node").data( nodes, function(d) { return d.id; } );
        node//.transition()
            .style( "fill", function( d ) {
                if( !d.highlight && d.suppress ) {
                    return rgba( this, "fill", 0.20 );
                } else {
                    return null;
                }
            });

        enterNode = node.enter().append("g")
            .attr( "class", function( d ) { return d.category; } )
            .classed( "node", true )
            .on( "click", function( d ) { click( d ); } )
            .call(force.drag);

        // add the text associated with the node, but wrap the text into lines
        enterNode.append("text")
            .attr( "dx", function( d ) { return 3 + circleRadius( d ); } )
            .attr("dy", ".35em")
            .each( function( d ) { wrapText( this, d.name, circleRadius( d ), LINE_WRAP ); } );
//        var foreignObjects = enterNode.append( "foreignObject" )
//            .attr( "width", 200 )
//            .attr( "height", 50 )
//            .append( "xhtml:body")//.append( "xhtml:textarea" )
//            .style( "color", function( d ) {
//                if( !d.highlight && d.suppress ) {
//                    return "green";
//                } else {
//                    return "red";
//                }
//            })
//            .html( function( d ) { return d.name; } );

        // add the circles that represent the nodes
        enterNode.append("circle")
            .attr( "r", function( d ) { return circleRadius( d ); } )
            .on("mouseover", function( d ) { mouseOver( d, this, links ); } )
            .on("mouseout", function( d ) { mouseOut( d, this, links ); } );

        node.exit().remove();
    }

    /**
     * Wraps the text by adding svg "tspan" elements to the "text" element and
     * adjusting the y-offset for each wrapped line.
     * @param textElement The svg text element to which to add the "tspan" element
     * @param text The text (string) that is to be wrapped
     * @param radius The radius of the node circle
     * @param wrapSize The length of the wrapped line
     */
    function wrapText( textElement, text, radius, wrapSize ) {
        var node = d3.select( textElement );

        // in some cases, an item of the category may not have a "name" property
        // and in this case, the "text" variable will be undefined, so, at least
        // we set it to null;
        var text = text || null;
        if( text === null || text.length === 0 ) {
            node.append("tspan").text( " " );
            return;
        }

        // if the length of the text is less than or equal to the wrap size, then
        // we can just use it as is, otherwise we need to wrap
        var length = text.length;
        if( length <= wrapSize ) {
            node.append("tspan").text( text );
            return;
        }

        var firstLine = true;
        var lineHeight = 13;
        var index = 0;
        var wordArray = text.split( /\s+/ );
        while( index < wordArray.length ) {
            var textLine = "";
            while( index < wordArray.length && ( textLine.length + wordArray[ index ].length < wrapSize || textLine.length === 0 ) ) {
                textLine += wordArray[ index++ ] + " ";
            }

            // the gnarly code is to avoid the firstLine from changing because of the
            // loop in the closure. so we define an anonymous function and pass the
            // firstLine variable to it [i.e. (function(x) { ... })( firstLine ) ]
            // ouch...that is gross...
            node.append("tspan")
                .attr("dy", (function(first) {
                        return function( d ) {
                            return first ? 0: lineHeight;
                        };
                    })( firstLine ) )
                .attr( "x", (function(first) {
                        return function( d ) {
                            return first ? 0 : 3 + radius;
                        };
                    })(firstLine) )
                .text( textLine );
            firstLine = false;
        }
    }

    /**
     * Creates the "rgba" function string based on the element, its property,
     * and the desired alpha. This function allows you to change the alpha (transparency)
     * of the element
     * @param element The svg element
     * @param property the property of the element
     * @param alpha The new alpha
     * @return {String}
     */
    function rgba( element, property, alpha ) {
        var color = d3.rgb( getComputedStyle( element, null ).getPropertyValue( property ) );
        return "rgba(" + color.r + "," + color.g + "," + color.b + "," + alpha + ")";
    }

    /**
     * Adds transparency to the specified color.
     * @param color The rgb color
     * @param alpha The transparency which should be in (0.0, 1.0) where 0.0 is transparent
     * and 1.0 is opaque.
     * @return {String}
     */
    function addColorAlpha( color, alpha ) {
        return "rgba(" + color.r + "," + color.g + "," + color.b + "," + alpha + ")";
    }

    /**
     * Determines whether the nodes and edges should be highlighted or not.
     * Returns true if both endpoints of the edge (source and target) are highlighted
     * @param d the data holding the source and target
     * @return {Boolean}
     */
    function highlight( d ) {
        var highlight = false;
        if( d.source.highlight && d.target.highlight ) {
            highlight = true;
        }
        return highlight;
    }

    /**
     * Determines whether the nodes and edges should be suppress (i.e. pushed into the
     * background in a faded state).
     * Returns true if both endpoints of the edge (source and target) are suppress.
     * @param d the data holding the source and target
     * @return {Boolean}
     */
    function suppress( d ) {
        var suppress = false;
        if( d.source.suppress && d.target.suppress ) {
            suppress = true;
        }
        return suppress;
    }

    /**
     * Determines whether the nodes and edges should be normal
     * Returns true if both endpoints of the edge (source and target) are normal.
     * @param d the data holding the source and target
     * @return {Boolean}
     */
    function normal( d ) {
        var normal = false;
        if( !highlight( d ) && !suppress( d ) ) {
            normal = true;
        }
        return normal;
    }

    /**
     * Returns the edge color based on whether the edge should be highlighted,
     * suppress, or normal
     * @param d The data holding the source and target
     * @param element The SVG element (should resolve to line.link)
     * @return {*} string representation of the color (rgba(x,y,z,a))
     */
    function linkColor( d, element ) {
        // grab the link color from the CSS style and store it as the baseline value
        // from which to adjust the link color's alpha
        if( !linkStyleColor ) {

            // if the color comes back as "none" then no link color was specified
            // in the CSS and so we'll go to a default color, otherwise use the
            // color specified in the CSS
            var color = getComputedStyle( element, null).getPropertyValue( "stroke" );
            if( color === "none" ) {
                linkStyleColor = d3.rgb( "rgb(194, 194, 194)" );
            } else {
                linkStyleColor = d3.rgb( color );
            }
        }

        // increase of decrease the color's transparency based on its state
        var color;
        if( highlight( d ) ) {
            color = addColorAlpha( linkStyleColor, 0.5 );
        } else if( suppress( d ) ) {
            color = addColorAlpha( linkStyleColor, 0.25 );
        } else {
            color = addColorAlpha( linkStyleColor, 0.5 );
        }
        return color;
    }

    /**
     * Returns the width of the edge based on whether the edge should be highlighted,
     * supressed, or normal.
     * @param d The data holding the source and target
     * @param element The SVG element (should resolve to line.link)
     * @return {*} the width of the line as an integer
     */
    function linkWidth( d, element ) {

        // grab the baseline link line width. if no stroke-width is specified in
        // the CSS then it appears that 1px is returned.
        if( !linkStyleWidth ) {
            linkStyleWidth = parseInt( getComputedStyle( element, null).getPropertyValue( "stroke-width" ) );
        }

        // adjust the width of the line based on its state
        var width;
        if( highlight( d ) ) {
            width = linkStyleWidth + 1;
        } else if( suppress( d ) ) {
            width = Math.max( linkStyleWidth  - 1, 1 );
        } else {
            width = linkStyleWidth;
        }
        return width;
    }

    /**
     * Updates the coordinates of the link and the nodes
     */
    function tick() {
        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });

        node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    }

    /**
     * converts the json resume object into a tree structure that can be used
     * bey the tree layout.
     * @param json The resume json object
     * @return {*}
     */
    function convert( json ) {

        // create the object holding the resume data
        var group = 0;

        // add the person information
        json[ ROOT ][ GROUP ] = group;
        json[ ROOT ][ CATEGORY ] = ROOT;
        json[ ROOT ][ CHILDREN ] = [];
        json[ ROOT ][ HIDDEN_CHILDREN ] = null;
        var root = json[ ROOT ];

        // loop through all the categories (i.e. education, companies, education, etc)
        // and add all their items to the graph.
        var categories = root[ CATEGORIES ];
        for( var i = 0; i < categories.length; ++i ) {
            addChildren( categories[ i ][ NAME ], root, ++group );
        }

        return root;
    }

    /**
     * Adds a new group with the specified name to the parent, and
     * with the specified array of children nodes.
     * @param category The identifying category
     * @param parent The parent node to which to add this group
     * @param group The group number for determining the color
     * @return {Object} The root node of the group
     */
    function addChildren( category, parent, group ) {
        // grab the object in the categories list that has the specified category
        // and then grab the child nodes holding the category values
        var categoryNode = getCategory( category, parent );
        var children = categoryNode[ ITEMS ];

        var nodes = [];
        for( var i = 0; i < children.length; ++i ) {
            var item = children[ i ];

            // if nodeNames has a function for overriding the name then use it
            // otherwise the name defaults to the item's name as specified in the json
            if( nodeNames[ category ] != undefined ) {
                item[ NAME ] = nodeNames[ category ]( children[ i ] );
            }
            item[ GROUP ] = group;
            item[ CATEGORY ] = category;
            nodes.push( item );
        }

        var child = {
            "name":categoryNode[ DISPLAY_AS ],
            "category": category,
            "group": group,
            "children": nodes
        };

        parent.children.push( child );

        return child;
    }

    function getCategory( category, parent ) {
        var categories = parent[ CATEGORIES ];
        for( var i = 0; i < categories.length; ++i ) {
            if( categories[ i ][ NAME ] === category ) {
                return categories[ i ];
            }
        }
    }

    /**
     * Returns a list of all nodes under the root.
     * @param root The root node of the resume object
     * @return {Array} containing a list of the nodes and all their connections
     */
    function flatten( root ) {
        var nodes = [], i = 0;

        function recurse( node ) {
            if( node.children ) {
                node.children.forEach( recurse );
            }
            if( !node.id ) {
                node.id = ++i;
            }
            nodes.push( node );
        }

        recurse( root );

        return nodes;
    }

    /**
     * Close all the children that aren't part of the top level groups. Leave the
     * top level groups (i.e. positions, education, companies, etc)
     * @param root The root node of the resume tree-structured json object
     */
    function startView( root ) {

        // close all the children of the top-level groups
        root.children.forEach( function( node ) {
            node._children = node.children;
            node.children = null;
        });
    }

    /**
     * Returns the radius of the circle representing the node of that data
     * @param d
     * @return {Number}
     */
    function circleRadius( d ) {
        return d.children || d._children ? 15 : 10;
    }

    /**
     * Opens and closes a node
     * @param d
     */
    function click( d ) {
        if( !d.children && !d._children ) {
            // click on leaf node, do nothing
        } else {
            if( d.children ) {
                // has children showing, click means hide the children
                d._children = d.children;
                d.children = null;
            } else {
                // has children hidden, click means show the children
                d.children = d._children;
                d._children = null;
            }
            update();
        }
    }

    /**
     * Highlights the circle and all the incoming and outgoing links
     * @param d The data bind to the element
     * @param element The element bound to the data
     * @param links The link objects holding the source and target objects
     */
    function mouseOver( d, element, links ) {

        d3.select( element )
            .style( "stroke", function() {
                return d3.rgb(getComputedStyle(this, null).getPropertyValue("fill")).darker();
            })
            .transition()
            .style("stroke-width",3);

        // find all the incoming and outgoing links and set the highlight of the
        // source and target to true to signify that it should be highlighted
        var edges = connectedLinks( d, links );
        edges.forEach( function( link ) {
            link.source.highlight = true;
            link.target.highlight = true;
        });

        // find all the links that aren't connected edges and suppress their view
        var notEdges = links.elementsNotIn( edges );
        notEdges.forEach( function( link ) {
            link.source.suppress = true;
            link.target.suppress = true;
        });

        lightUpdate();
    }

    /**
     * Removes the highlight from the circle and all the incoming and outgoing links
     * @param d The data bind to the element
     * @param element The element bound to the data
     * @param links The link objects holding the source and target objects
     */
    function mouseOut( d, element, links ) {
        d3.select( element ).transition().style( "stroke","" );

        // reset all the source and target highlight values (no highlighting)
        links.forEach( function( link ) {
            link.source.highlight = false;
            link.target.highlight = false;
            link.source.suppress = false;
            link.target.suppress = false;
        });
        lightUpdate();
    }

    /**
     * Adds a new method to the javascript array class that returns
     * all the elements of the array that are NOT in the specified array.
     * Effectively, this removes the elements of the specified array
     * @param array The array elements to remove from the array
     * @return {Array} The array with the elements removed
     */
    Array.prototype.elementsNotIn = function( array ) {
        return this.filter( function( element ) {
            var matches = array.filter( function( arrayElem ) {
                return arrayElem === element;
            });
            return matches.length === 0;
        })
    };

    /**
     * Returns the node with the specified ID or null if none is found
     * @param id The ID to find within the node list
     * @param nodes The list of nodes to search for the ID
     * @return {*}
     */
    function findNodeForId( id, nodes ) {
        // find all the nodes that have the specified ID
        var matches = nodes.filter( function( node ) {
            return node.id === id;
        });

        // if there was a match, the "matches" array has a non-zero length, and
        // in that case, return the first match, otherwise return null representing
        // that there is no match
        return matches.length > 0 ? matches[ 0 ] : null;
    }

    /**
     * Creates an array of all the references in the resume object. For example, a
     * position that references the ID of a company should have a link. Note that if
     * the "positions" node is closed, then these links will go away.
     * @param nodes The list of open nodes.
     * @return {Array}
     */
    function linkDependencies( nodes ) {
        var links = [];
        nodes.forEach( function( node ) {
            if( node[ "targets" ] ) {
                node[ "targets" ].forEach( function( target ) {
                    var targetNode = findNodeForId( target.id, nodes );
                    if( targetNode ) {
                        links.push( {
                            "source": node,
                            "target": targetNode
                        });
                    }
                });
            }
        });
        return links;
    }

    /**
     * Returns a list of outgoing links. Outgoing links are those whose
     * source is the specified node.
     * @param node The node for which to find all outgoing links
     * @param links The links from which to search for outgoing links
     * @return {*} array of outgoing links
     */
    function outgoingLinks( node, links ) {
        return links.filter( function( link ) {
            return link.source === node;
        });
    }

    /**
     * Returns a list of incoming links. Incoming links are those whose target
     * is the specified node
     * @param node The node for which to find all incoming links
     * @param links The links from which to search for incoming links
     * @return {*} array of incoming links
     */
    function incomingLinks( node, links ) {
        return links.filter( function( link ) {
            return link.target === node;
        });
    }

    /**
     * More efficient way of getting both incoming and outgoing edges
     * @param node The node for which to find all connected links
     * @param links The links from which to search for connected links
     * @return {*} array of connected links
     */
    function connectedLinks( node, links ) {
        return links.filter( function( link ) {
            return link.source === node || link.target === node;
        });
    }

    /**
     * make the public methods public
     */
    return {
        load: load,
        setNodeFormat: setNodeFormat,
        removeNodeFormat: removeNodeFormat
    }
}