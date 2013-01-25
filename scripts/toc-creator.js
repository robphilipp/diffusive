/**
 * User: Robert Philipp
 * Date: 10/13/12
 * Time: 3:43 PM
 *
 * Use the function "createToc(...)" at the bottom of the file. The other functions are intended to be "private"
 *
 * Creates an unordered list (<ul class="nav-list></ul>) containing list-items that hold the table of contents
 * and places the unordered list into the specified <div>
 *
 * Makes the following assumptions:
 * 1. Each header has an ID which is used as the tag (#name) to jump to that part of the page
 * 2. Each header has a value (content) that will be displayed in the TOC
 *
 * Use the CSS to define how the TOC is placed and displayed. I use these to put the TOC on the side
 * and keep it there during scrolling.
 *
 // allows for the lists to be nested and the font size decreases for each nesting level
 ul.nav-list {
        list-style-type: none;
        font-family: calibri;
        font-size:0.9em;
        padding-left: 5px;
    }
 *
 */

/**
 * Returns an array of header elements found in the document
 * @return {Array} containing the header elements
 */
function $grabHeaders()
{
    var headers = [];
    if ( document.querySelectorAll )
    {
        headers = document.querySelectorAll( "h1,h2,h3,h4,h5,h6" );
    }
    else
    {
        var elements = document.getElementsByTagName( "h*" );
        if ( elements )
        {
            var j = 0;
            for ( var i = 0, len = elements.length; i < len; ++i )
            {
                var tag = elements[ i ];
                var tagName = tag.tagName.toLowerCase();
                if ( tagName.search( /^h[1-6]$/g ) >= 0 )
                {
                    headers[ j ] = tag;
                    ++j;
                }
            }
        }
    }
    return headers;
}

/**
 * Returns the element based on the class name
 * @param searchTag The element containing the tag to narrow the search scope
 * @param className The name of the class to be found
 * @return {*} The first element found that has the specified class name
 */
function $getElementByClassName( searchTag, className ) {
    var elements = [];
    if( document.querySelectorAll ) {
         return document.querySelectorAll( searchTag + "." + className )[0];
    }
    else {
        elements = document.getElementsByTagName( searchTag );
        if ( elements )
        {
            var j = 0;
            for ( var i = 0, len = elements.length; i < len; ++i )
            {
                if ( elements[ i ].className.toLowerCase() === className )
                {
                    return elements[ i ];
                }
            }
        }
    }
}

/**
 * Returns the level of the header. For example, "h2" returns a
 * level of 2. If the header is null or invalid, then the returned
 * level is -1
 * @param header The header
 * @return {Number} The level of the header
 */
function $getHeaderLevel( header )
{
    var level = -1;
    if ( header )
    {
        var tagName = header.tagName.toLowerCase();
        if ( tagName.search( /^h[1-6]$/g ) >= 0 )
        {
            level = tagName.charAt( 1 ).valueOf()
        }
    }
    return level;
}

/**
 * Recursive method for creating a nested list holding the table of contents
 * @param headers The list of header elements
 * @param index The current index into the list of headers
 * @param level The current level of the header being processed
 * @param parent The parent element (unordered list) to which to add the items
 * @param depth The depth of the headers for which to create a table of contents
 * @param title The title that appears at the top of the TOC
 * @return {*} The current index processed (this is needed when a nested list is complete)
 */
function $createTocList( headers, index, level, parent, depth, title )
{
    if( level > depth ) {
        return index;
    }

    var unorderedList = document.createElement( "ul" );
    unorderedList.setAttribute( "class", "nav nav-list" );

    // create the title paragraph
    if( title ) {
        var tocTitle = document.createElement( "li" );
        tocTitle.setAttribute( "class", "nav-header" );
        tocTitle.innerHTML = title;
        unorderedList.appendChild( tocTitle );
    }

    var i = index;
    for ( var len = headers.length; i < len; ++i )
    {
        var header = headers[ i ];

        var headerLevel = $getHeaderLevel( header );
        if ( headerLevel < level )
        {
            // done with the current list
            --i;
            break;
        }
        else if ( headerLevel > level )
        {
            // create new list--this is a sub-list
            i = $createTocList( headers, i, headerLevel, unorderedList, depth );
        }
        else
        {
            var headerId = header.getAttribute( "id" );
            var anchorElements = header.getElementsByTagName( "a" );
            if ( headerId != null && anchorElements != null && anchorElements.length > 0 )
            {
                // create the link to the section in the html doc
                var link = document.createElement( "a" );
                link.setAttribute( "href", "#" + headerId.toString() );
                link.setAttribute( "class", "scroll-offset" );
                link.innerHTML = anchorElements[ 0 ].innerHTML;

                // create the list item and add it to the list
                var listItem = document.createElement( "li" );
                listItem.appendChild( link );
                unorderedList.appendChild( listItem );
            }
        }
    }
    parent.appendChild( unorderedList );
    return i;
}

/**
 * Returns an array containing the minimum level as the first element and the maximum
 * level as the second element. For example, the minimum level for h2, h3 and h4 would be
 * h2.
 * @param headers The list of header elements
 * @return {Array} containing the minimum level as teh first element and the maximum leve
 * as the second element.
 */
function $findMinMaxHeaders( headers )
{
    var minLevel = 100;
    var maxLevel = 0;
    for ( var i = 0, len = headers.length; i < len; ++i )
    {
        var level = $getHeaderLevel( headers[ i ] );
        if ( level < minLevel )
        {
            minLevel = level;
        }
        if ( level > maxLevel )
        {
            maxLevel = level;
        }
    }
    return [ minLevel, maxLevel ];
}

/**
 * MAIN FUNCTION: call this one. Makes the following assumptions:
 * 1. Each header has an ID which is used as the tag (#name) to jump to that part of the page
 * 2. Each header has a value (content) that will be displayed in the TOC
 * @param divClassName The class name of the div element into which the TOC list will be placed
 * @param depth The depth of the nesting in the TOC. For example, if the depth is 3, then there
 * will be three levels of headers placed in the TOC.
 * @param title The title to appear at the top of the content list
 */
function createToc( divClassName, depth, title )
{
    depth = depth || 6;

    // grab the div that holds the toc list
    var tocDiv = $getElementByClassName( "div", divClassName );

    // recursively build the list
    var headers = $grabHeaders();
    var levels = $findMinMaxHeaders( headers );
    $createTocList( headers, 0, levels[ 0 ], tocDiv, 1*depth + 1*levels[ 0 ], title );

    // return the div element for reference
    return tocDiv;
}
