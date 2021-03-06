/**
 * User: rob
 * Date: 1/2/13
 * Time: 11:49 AM
 *
 * Request the list of diffusers (atom feed) from the diffuser server. The atom feed has the following
 * format.
 *
 * <feed xmlns="http://www.w3.org/2005/Atom">
 *     <id>get-diffuser-list</id>
 *     <title type="text">get-diffuser-list</title>
 *     <updated>2013-01-03T00:34:42.976Z</updated>
 *     <link href="http://192.168.1.5:8182/diffusers" rel="self"></link>
 *     <generator uri="http://192.168.1.5:8182/diffusers" version="0.1">org.microtitan.diffusive.diffuser.restful.RestfulDiffuser</generator>
 *     <entry>
 *         <id>org.microtitan.tests.threaded.task:call()-org.microtitan.tests.threaded.Result</id>
 *         <title type="text">org.microtitan.tests.threaded.Task:call()-org.microtitan.tests.threaded.Result</title>
 *         <updated>2013-01-03T00:34:42.976Z</updated>
 *         <published>2013-01-03T00:34:42.976Z</published>
 *         <link href="http://192.168.1.5:8182/diffusers/org.microtitan.tests.threaded.Task:call()-org.microtitan.tests.threaded.Result" rel="self"></link>
 *         <content type="application/xml">
 *             <RestfulDiffuserInfo xmlns="">
 *                 <serializer>persistence_xml</serializer>
 *                 <strategy>org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy</strategy>
 *                 <endPoints></endPoints>
 *                 <classPaths></classPaths>
 *                 <loadThreshold>0.75</loadThreshold>
 *                 <maxRedundancy>20</maxRedundancy>
 *                 <pollingTimeout>50</pollingTimeout>
 *                 <pollingTimeUnit>MILLISECONDS</pollingTimeUnit>
 *             </RestfulDiffuserInfo>
 *         </content>
 *     </entry>
 * </feed>
 *
 * @param diffusersUri The URI to the diffusers. For example, http://192.168.1.5:8182/diffusers.
 * @param diffuserListId The ID of the DOM element holding the list of diffusers
 */
function getDiffuserList( diffusersUri, diffuserListId ) {
    $.get( diffusersUri, function( xml ) {

        // remove the old list and replace it with the new one
        var diffuserList = $( "#" + diffuserListId );
        if( diffuserList ) {
            diffuserList.empty();
        }

        // create the new list
        $( xml ).find( "entry" ).each( function( i ) {

            // the ID of the div holding the information that can be hidden (this is part of a window shade)
            var collapseId = "collapse" + (+i);

            // the ID of the icon on on the left hand side that changes when the diffuser information is hidden
            // or when it is visible
            var collapseIconId = "icon" + collapseId;

            // create the header that holds the diffuser signature with:
            // 1. window shade for the details
            // 2. diffuser delete button on the right-hand side
            // 3. alternately shaded from light gray
            var header = $(
                '<div class="diffuser-list-header accordion-heading">' +
                    '<i id="' + collapseIconId + '" class="icon-chevron-right"></i>' +
                    '<a class="diffuser-signature-title" data-toggle="collapse" data-parent="#accordion2" href="#' + collapseId + '">' +
                        $( this ).find( "title" ).text() +
                    '</a>' +
                    '<a class="diffuser-remove-button pull-right" href="#"><i class="icon-remove"></i></a>' +
                '</div>'
            );
            if( (+i) % 2 == 0 ) {
                header.css( 'background-color', '#ececec' );    // light gray
            } else {
                header.css( 'background-color', 'white' );
            }
            diffuserList.append( header );

            // create the information that is hidden
            diffuserList.append(
                '<div id="' + collapseId + '" class="collapse">' +
                    '<div class="accordion-inner">' +
                        '<p><a href=' + $( this ).find( "link" ).prop( "href" ) + ' >Diffuser</a></p>' +
                        '<p>Strategy: ' + $( this ).find( "strategy" ).text() + '</p>' +
                        '<p>End Points: ' + $( this ).find( "endPoints" ).text() + '</p>' +
                        '<p>Serializer: ' + $( this ).find( "serializer" ).text() + '</p>' +
                        '<p>Load Threshold: ' + $( this ).find( "loadThreshold" ).text() + '</p>' +
                        '<p>Maximum Redundancy: ' + $( this ).find( "maxRedundancy" ).text() + '</p>' +
                        '<p>Polling Time-Out: ' + $( this ).find( "pollingTimeout" ).text() + ' ms</p>' +
                        '<p>Class Paths: ' + $( this ).find( "classPaths" ).text() + '</p>' +
                        '<p>Information Request Timestamp: ' + $( this ).find( "published" ).text() + '</p>' +
                    '</div>' +
                '</div>'
            );

            // change the chevron to indicate the information is now shown
            $( '#' + collapseId ).on( 'show', function () {
                $( "#" + collapseIconId ).prop( 'class', 'icon-chevron-down' );
            })

            // change the chevron to indicate that the information is now hidden
            $( '#' + collapseId ).on( 'hide', function () {
                $( "#" + collapseIconId ).prop( 'class', 'icon-chevron-right' );
            })
        });

        // toggle to remove-diffuser icon when the user hovers over it to show it is active
        $( ".diffuser-remove-button" ).hover( function() {
            $( "i", $( this ) ).toggleClass( 'icon-remove-sign' );
        });

        // set up the callback to delete diffusers when the user clicks on the remove-diffuser icon
        $( ".diffuser-remove-button" ).click( function( e ) {
            $.ajax({
                url: diffusersUri + "/" + $( ".diffuser-signature-title", $( this ).parent() ).text(),
                type: "DELETE",
                dataType: "xml",
                success: function( data, textStatus, jqXHR ) {
                    getDiffuserList( diffusersUri, diffuserListId );
                },
                error: function( jqXhr, textStatus, errorThrown ) {
                    alert( textStatus + ": " + errorThrown + " (URL=" + diffusersUri + "/" + $( ".diffuser-remove-button", $( this ).parent() ).text() + ")" );
                }
            });
            e.stopPropagation();
        })

    }, "xml" ).error( function() { alert( 'failed to load diffuser list' ) } );
}
