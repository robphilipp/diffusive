/**
 * User: rob
 * Date: 1/2/13
 * Time: 7:31 PM
 */
function getSerializerList( serializersUri, serializerListId, defaultSerializer ) {
    var defaultSerializer = "persistence_xml" || defaultSerializer;
    $.get( serializersUri, function( xml ) {
        $(xml).find( "entry" ).each( function() {
            var option = $( this ).find( "title" ).text();
            if( option === defaultSerializer ) {
                $( serializerListId ).append( '<option selected>' + option + '</option>' );
            } else {
                $( serializerListId ).append( '<option>' + option + '</option>' );
            }
        });
    }, "xml" ).error( function() {
            $( serializerListId ).append( '<option>' + defaultSerializer + '</option>' );
            alert( 'failed to load serializer names' )
        });
}