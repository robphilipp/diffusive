/**
 * User: rob
 * Date: 1/2/13
 * Time: 11:49 AM
 */
    // request the list of diffusers (atom feed) from the diffuser server
    // <feed xmlns="http://www.w3.org/2005/Atom">
    //     <id>get-diffuser-list</id>
    //     <title type="text">get-diffuser-list</title>
    //     <updated>2012-12-27T21:04:12.694Z</updated>
    //     <link href="http://192.168.1.5:8182/diffusers" rel="self"></link>
    //     <generator uri="http://192.168.1.5:8182/diffusers" version="0.1">org.microtitan.diffusive.diffuser.restful.RestfulDiffuser</generator>
    //     <entry>
    //          <id>org.microtitan.tests.threaded.task:call()-org.microtitan.tests.threaded.Result</id><
    //          title type="text">org.microtitan.tests.threaded.Task:call()-org.microtitan.tests.threaded.Result</title>
    //          <updated>2012-12-27T21:04:12.694Z</updated>
    //          <published>2012-12-27T21:04:12.694Z</published>
    //          <link href="http://192.168.1.5:8182/diffusers/org.microtitan.tests.threaded.Task:call()-org.microtitan.tests.threaded.Result" rel="self"></link>
    //          <summary type="html">&lt;html&gt;&lt;p&gt;RESTful Diffuser for: org.microtitan.tests.threaded.Task:call()-org.microtitan.tests.threaded.Result&lt;/p&gt;&lt;/html&gt;</summary>
    //     </entry>
    //  </feed>
    // <serializer>persistence_xml</serializer>
    // <strategy>org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy</strategy>
    // <classPaths></classPaths>
    // <loadThreshold>0.75</loadThreshold>
    // <maxRedundancy>20</maxRedundancy>
    // <pollingTimeout>50</pollingTimeout>
    // <pollingTimeUnit>MILLISECONDS</pollingTimeUnit>
function getDiffuserList( diffusersUri, diffuserListId ) {
    $.get( diffusersUri, function( xml ) {
        $(xml).find( "entry").each( function( i ) {
            $( diffuserListId ).append( '<dt>' + $( this ).find( "title" ).text() + '</dt>' );
            $( diffuserListId ).append( '<dd><p>' +
                '<a href=' + $( this ).find( "link" ).attr( "href" ) + ' >Diffuser</a></p>' +
                '<p>Strategy: ' + $( this ).find( "strategy" ).text() + '</p>' +
                '<p>End Points: ' + $( this ).find( "endPoints" ).text() + '</p>' +
                '<p>Serializer: ' + $( this ).find( "serializer" ).text() + '</p>' +
                '<p>Load Threshold: ' + $( this ).find( "loadThreshold" ).text() + '</p>' +
                '<p>Maximum Redundancy: ' + $( this ).find( "maxRedundancy" ).text() + '</p>' +
                '<p>Polling Time-Out: ' + $( this ).find( "pollingTimeout" ).text() + ' ms</p>' +
                '<p>Class Paths: ' + $( this ).find( "classPaths" ).text() + '</p>' +
                '<p>Information Request Timestamp: ' + $( this ).find( "published" ).text() + '</p>' +
                '</dd>' );
        })
    }, "xml" );//.error( function() { alert( 'failed to load diffuser list' ) } );
}
