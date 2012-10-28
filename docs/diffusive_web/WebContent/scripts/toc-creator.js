/**
 * User: Robert Philipp
 * Date: 10/13/12
 * Time: 3:43 PM
 *
 * Use the function "createToc(...)" at the bottom of the file. The other functions are intended to be "private"
 *
 * Makes the following assumptions:
 * 1. Each header has an ID which is used as the tag (#name) to jump to that part of the page
 *
 * Use the CSS to define how the TOC is placed and displayed. I use these to put the TOC on the side
 * and keep it there during scrolling.
 *
 // makes the box to hold the TOC and keeps it fixed at that position
 #toc
 {
     position: fixed;
     left: 10px;
     top: 145px;
     width: 155px;
     background-color: #fff;
     font-family: calibri;
     padding: 0px 5px 5px 5px;
     border-color: #EBEBEB;
     border-style: solid;
     border-width: 1px;
     z-index: 10;
 }

 // the styling for the TOC title
 p.overview_toc, #overview_toc
 {
     font-style: italic;
     color: #C67171;
     margin-bottom: 0px;
 }

 // allows for the lists to be nested and the font size decreases for each nesting level
 ul.overview_toc_list {
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
        var elements = document.getElementsByTagName( "*" );
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
 * Recursive method for creating a nested list holding the list of contents
 * @param headers The list of header elements
 * @param index The current index into the list of headers
 * @param level The current level of the header being processed
 * @param parent The parent element (unordered list) to which to add the items
 * @return {*} The current index processed (this is needed when a nested list is complete)
 */
function $createTocList( headers, index, level, parent )
{
    var unorderedList = document.createElement( "ul" );
    unorderedList.setAttribute( "class", "overview_toc_list" );

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
            i = $createTocList( headers, i, headerLevel, unorderedList );
        }
        else
        {
            var headerId = header.getAttribute( "id" );
            if ( headerId !== null )
            {
                // create the link to the section in the html doc
                var link = document.createElement( "a" );
                link.setAttribute( "href", "#" + headerId.toString() );
                link.innerHTML = header.getElementsByTagName( "a" )[ 0 ].innerHTML;

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
 * @param title The title to appear at the top of the content list
 */
function createToc( title )
{
    // create a document fragment to avoid reflow as we add the elements to the toc
    var frag = document.createDocumentFragment();

    // create the toc div
    var tocDiv = document.createElement( "div" );
    tocDiv.setAttribute( "id", "toc" );
    frag.appendChild( tocDiv );

    // create the title paragraph
    var tocTitle = document.createElement( "p" );
    tocTitle.setAttribute( "class", "overview_toc" );
    tocTitle.innerHTML = title;
    tocDiv.appendChild( tocTitle );

    // recursively build the list
    var headers = $grabHeaders();
    var levels = $findMinMaxHeaders( headers );
    $createTocList( headers, 0, levels[ 0 ], tocDiv );

    // add the TOC div to the document
//    document.body.appendChild( frag );
    document.body.insertBefore( frag, document.getElementsByTagName( 'article' )[ 0 ] );

    // return the div element for reference
    return tocDiv;
}
