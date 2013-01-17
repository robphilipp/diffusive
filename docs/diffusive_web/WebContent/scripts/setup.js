function setup( tocDepth ) {
    $(document).ready( function() {

        // create the table of contents
        tocDepth = tocDepth || 3;
        createToc( "sidebar-nav", tocDepth, "Contents" );

        $(function() {
            $( "#tabs" ).tabs();
            $( "#accordion" ).accordion( {
                autoHeight: false,
                header: "h3",
                collapsible: true,
                active: false
            } );
            $( "#whats-new-accordion" ).accordion( { autoHeight: false } );
        });

        // syntax highlighting for the code
        SyntaxHighlighter.all( {
            "gutter": false,
            "toolbar": false
        } );

        $( "a.scroll-offset" ).click( function() {
            var element = $( this ).attr( "href" );
            if( element ) {
                scrollToDiv( $( element ), 81 );
            }
            return false;
        });

        $( ".sidebar-nav" ).affix( {
            offset: {
                top: function() {
                    var width = $( window ).width();
                    if( width > 768 && width < 979 ) {
                        return $( ".navbar-fixed-top" ).height();
                    } else {
                        return 0;
                    }
                },
                bottom: 0
            }
        } );

    }, false );
}