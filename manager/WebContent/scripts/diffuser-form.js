/**
 * User: rob
 * Date: 1/7/13
 * Time: 9:08 AM
 */
function DiffuserForm( formId, diffusersUri, settings ) {

    var config = {
        containingClassId : "containing-class-input",
        methodNameId : "method-name-input",
        returnTypeId : "return-type-list",
        methodArgListId : "method-argument-list",
        classPathListId : "class-path-list",
        endPointListId : "end-points-list",
        serializerId : "serializer-name-list",
        defaultSerializerName : "persistence_xml",

        addArgTypeButtonId : "add-arg-type-button",

        addReturnTypeButtonId : "add-return-type-button",
        removeReturnTypeButtonId : "remove-return-type-button",

        addClassPathButtonId : "add-class-path-button",

        addEndpointButtonId : "add-end-points-button",

        createDiffuserResetButtonId : "create-diffuser-reset-button",

        successCallback: function( data, textStatus, jqXHR ) {}
    };

    // private constants used only internally

    // class identifiers used for list items, the input text fields contained in the list items, and the buttons for
    // removing a list item. these are used for method argument, return type, class paths, and end points
    var listItemClass = "list-item";
    var listItemInputClass = "item-list-input";
    var listItemRemoveButtonClass = "item-list-remove-button";

    var className = "class-name";
    var methodName = "method-name";
    var variableName = "variable-name";

    // merge the configuration items specified in the function and the default ones
    $.extend( config, settings );

    // set up the validator
    $( "#" + formId ).validate({
        submitHandler: function() {
            $.ajax({
                url: diffusersUri,
                type: "PUT",
                data: asJson(),
                dataType: "xml",
                contentType: "application/json; charset=UTF-8",
                success: function( data, textStatus, jqXHR ) {
                    resetDiffuserForm();
                    config.successCallback( data, textStatus, jqXHR );
                },
                error: function( jqXhr, textStatus, errorThrown ) {
                    alert( textStatus + ": " + errorThrown );
                }
            });
            return false;
        },

        // don't show the validation messages, the fields turn red when
        // invalid anyway
        errorPlacement: function( error, element ) {
            error.appendTo( function() {} );
        }
    });

    // add the handler for resetting the form
    $( "#" + config.createDiffuserResetButtonId ).click( function() { resetDiffuserForm() } );

    // validator override for the class-name
    jQuery.validator.addMethod( className, function( value )
    {
        return value.match( /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/ );
    }, "Invalid Java class name." );

    // validator override for the method-name
    jQuery.validator.addMethod( methodName, function( value ) {
        return value.match( /^[a-zA-Z_$][a-zA-Z\d_$]*$/ );
    }, "Invalid Java method name." );

    // validator override for the method-name
    jQuery.validator.addMethod( variableName, function( value ) {
        return value.match( /^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$/ );
    }, "Invalid Java variable name." );

    // add argument input field and remove-item button to the method's argument list
    $( "#" + config.addArgTypeButtonId ).click( function() {
        $( "#" + config.methodArgListId ).append( '<li class="' + listItemClass + '">' +
            '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
            '<input type="text" class="' + listItemInputClass + ' ' + variableName + '" value="" size="55">' +
            '<input type="button" class="' + listItemRemoveButtonClass + '" value="x">' +
            '</li>' );
    });

    // add class-path input field and remove-item button to the class-path list
    $( "#" + config.addClassPathButtonId ).click( function() {
        $( "#" + config.classPathListId ).append( '<li class="' + listItemClass + '">' +
            '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
            '<input type="text" class="' + listItemInputClass + ' url" value="" size="55">' +
            '<input type="button" class="' + listItemRemoveButtonClass + '" value="x">' +
            '</li>' );
    });

    // add end-point input field and remove-item button to the end-point list
    $( "#" + config.addEndpointButtonId ).click( function() {
        $( "#" + config.endPointListId ).append( '<li class="' + listItemClass + '">' +
            '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
            '<input type="text" class="' + listItemInputClass + ' url" value="" size="55">' +
            '<input type="button" class="' + listItemRemoveButtonClass + '" value="x">' +
            '</li>' );
    });

    // remove the item from the list when the associated button is pressed
    $( "." + listItemRemoveButtonClass ).live( "click", function() {
        $( this ).parent().remove();
    });

    // allows the user to add a return type; once clicked, hides itself
    $( "#" + config.addReturnTypeButtonId ).click( function() {
        $( "#" + config.returnTypeId ).append( '<li>' +
            '<input type="text" class="' + listItemInputClass + ' ' + variableName + '" value=""  size="55">' +
            '<input type="button" id="' + config.removeReturnTypeButtonId + '" value="x">' +
            '</li>' );
        $( "#" + config.addReturnTypeButtonId ).hide();
    });

    // allows the user to remove the return type; once clicked shows the add-return-type button
    $( "#" + config.removeReturnTypeButtonId ).live( "click", function() {
        $( this ).parent().remove();
        $( "#" + config.addReturnTypeButtonId ).show();
    });

    /**
     * resets the create-diffuser form and add the handler
     */
    function resetDiffuserForm() {

        // grab the form used for creating the diffuser
        var $form = $( "#" + formId );

        // sets all the form's text input fields to empty
        $( "#" + config.containingClassId ).val( "" );
        $( "#" + config.methodNameId ).val( "" );
//        $( "#" + config.serializerId ).option( config.defaultSerializerName );

        // removes all the form's input fields from the lists
        $( "." + listItemClass ).remove();

        // adds the add-return-type button back, in case it was gone
        $( "#" + config.addReturnTypeButtonId ).show();
    }

    /**
     * Returns the values from the input elements with class specified in the listItemInputClass
     * for the list element with the specified listId
     * @param listId The ID of the list holding the input elements
     * @return {Array} The values of the input elements
     */
    function getValues( listId ) {
        var argElems = $( "#" + listId ).find( "." + listItemInputClass ).toArray();
        var arguments = [];
        for( var i = 0; i < argElems.length; ++i ) {
            arguments.push( $(argElems[ i ]).val() );
        }
        return arguments;
    }

    /**
     * @return {String} The diffuser signature return type
     */
    function getReturnType() {
        var argElems = $( "#" + config.returnTypeId ).find( "." + listItemInputClass ).toArray();
        var argument = "void";
        if( argElems.length > 0 ) {
            argument = $(argElems[ 0 ]).val()
        }
        return argument;
    }

    /**
     * @return {*|jQuery} The name of the serializer used by the diffusers for serialization and deserialization
     */
    function getSerializerName() {
        return $( "#" + config.serializerId ).val() || config.defaultSerializerName;
    }

    /**
     * @return {*} a JSON representation of the diffuser
     */
    function asJson() {
        var formValues = {
            containingClassName : $( "#" + config.containingClassId ).val(),
            methodName : $( "#" + config.methodNameId ).val(),
            returnTypeClassName : getReturnType(),
            argumentTypes : getValues( config.methodArgListId ),
            classPaths : getValues( config.classPathListId ),
            serializerType : getSerializerName(),
            clientEndpoints : getValues( config.endPointListId )
        };
        return JSON.stringify( formValues );
    };

    // the exposed diffuser form methods
    return {
        asJson : asJson(),
        resetForm : resetDiffuserForm()
    }
}