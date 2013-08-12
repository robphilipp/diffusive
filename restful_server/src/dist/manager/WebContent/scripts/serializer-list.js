/**
 * Loads the list of serializer names into the selection specified by the serializerListId.
 * If there is an error, the default name is used.
 * @param serializersUri The URI from which to load the list of serializer names
 * @param serializerListElement The jQuery element to which to add the serializer name options
 * @param defaultSerializer The name of the default serializer.
 */
function getSerializerList( serializersUri, serializerListElement, defaultSerializer ) {
    if( !defaultSerializer ) {
        defaultSerializer = "persistence_xml";
    }
    $.get( serializersUri, function( xml ) {
        $(xml).find( "entry" ).each( function() {
            var option = $( this ).find( "title" ).text();
            if( option === defaultSerializer ) {
                serializerListElement.append( '<option selected>' + option + '</option>' );
            } else {
                serializerListElement.append( '<option>' + option + '</option>' );
            }
        });
    }, "xml" ).error( function() {
            serializerListElement.append( '<option>' + defaultSerializer + '</option>' );
            alert( 'failed to load serializer names' )
    });
}