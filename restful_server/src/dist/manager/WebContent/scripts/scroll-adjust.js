function scrollToDiv( element, navheight ) {

    var offset = element.offset();
    var offsetTop = offset.top;
    var totalScroll = offsetTop-navheight;

    $( "body,html" ).animate( {
        scrollTop: totalScroll
    }, 250 );
}
