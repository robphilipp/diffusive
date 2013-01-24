/**
 * User: rob
 * Date: 10/25/12
 * Time: 11:41 AM
 */

/**
 * Return the view port size as w and h properties of an object
 * @param win
 * @return {Object}
 */
function getViewPortSize( win )
{
    // use the specified window or the current window if no argument
    win = win || window;

    // this works for all browsers except IE8 and before
    if ( win.innerWidth != null )
    {
        return {width: win.innerWidth, height: win.innerHeight};
    }

    // for IE (or any browser) in Standards mode
    var doc = win.document;
    if ( document.compatMode == "CSS1Compat" )
    {
        return { width: doc.documentElement.clientWidth, height: doc.documentElement.clientHeight };
    }

    // for browsers in Quirks mode
    return { width: doc.body.clientWidth, height: doc.body.clientWidth };
}

function setElementSize( elem, bounds, padding, win )
{
    // grab the size of the view port
    var viewPort = getViewPortSize( win );

    // adjust the height
    if( viewPort.height > bounds.minHeight && viewPort.height < bounds.maxHeight )
    {
        if( elem.offsetTop + elem.clientHeight > viewPort.height )
        {
            elem.style.height = ( viewPort.height - elem.offsetTop - padding ) + "px";
        }
        else if( viewPort.height > elem.clientHeight + elem.offsetTop + padding )
        {
            elem.style.height = ( viewPort.height - elem.offsetTop - padding ) + "px";
        }
    }

    // adjust the width
    if( viewPort.width - elem.offsetLeft > bounds.minWidth && viewPort.width - elem.offsetLeft < bounds.maxWidth )
    {
        if( elem.offsetLeft + elem.clientWidth > viewPort.width )
        {
            elem.style.width = ( viewPort.width - elem.offsetLeft - padding ) + "px";
        }
        else if( viewPort.width > elem.clientWidth + elem.offsetLeft + padding )
        {
            elem.style.width = ( viewPort.width - elem.offsetLeft - padding ) + "px";
        }
    }
    return { height: elem.style.height, width: elem.style.width };
}

function setArticleSize( padding, win )
{
    win = win || window;
    padding = padding || 40;

    return setElementSize( win.document.getElementsByTagName( 'article' )[ 0 ], padding, win );
}

/**
 *
 * @param id
 * @param padding
 * @param win
 * @return {*}
 */
function setTocSize( id, padding, win )
{
    id = id || 'toc';
    win = win || window;
    padding = padding || 30;

    return setElementSize( win.document.getElementById( id ), padding, win );
}

/**
 * Adjust the size of the article and the TOC divs. Switches from the CSS styled
 * for desktop to mobile when the view port width is less than the min width
 * @param minWidth The width at which the styling switches from desktop to mobile
 */
function adjustSizes( tocBounds, articleBounds, minWidth )
{
    var toc = window.document.getElementById( 'toc' );
    var article = window.document.getElementsByTagName( 'article' )[ 0 ];

    // grab the size of the view port
    var viewPort = getViewPortSize( window );

    minWidth = minWidth || 500;
    if( viewPort.width < minWidth )
    {
        // remove all the css stylesheets
        var sheets = document.querySelectorAll( '*' );
        for( var i = 0; i < sheets.length; i++ )
        {
            sheets[ i ].disabled = true;
        }
        if( article ) article.clientWidth = minWidth;
    }
    else
    {
        // if the article fit into the view-port when it was less then
        if( article.clientLeft + article.clientWidth < minWidth )
        {
            // reload the css
            var loadCssLink = document.createElement( 'link' );
            loadCssLink.setAttribute( 'rel', 'stylesheet' );
            loadCssLink.setAttribute( 'type', 'text/css' );
            loadCssLink.setAttribute( 'href', 'diffusive.css' );
            document.getElementsByTagName( "head" )[ 0 ].appendChild( loadCssLink );
        }
        // set the size of the TOC and article contents
        if( toc ) setElementSize( toc, tocBounds, 30, window );
        if( article ) setElementSize( article, articleBounds, 40, window );
    }
}