/**
 * User: rob
 * Date: 1/7/13
 * Time: 9:08 AM
 */
function DiffuserForm( parentId, diffusersUri, settings ) {

    var config = {
        formId : "create-diffuser-form",
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

        submitButtonId: "create-diffuser-submit-button",
        resetButtonId : "create-diffuser-reset-button",

        successCallback: function() {}
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
    var serializerName = "serializer-name";

    var returnType = "return-type";
    var classPaths = "class-paths";
    var endPoints = "end-points";

    var formElementName = "form-element";

    // merge the configuration items specified in the function and the default ones
    $.extend( config, settings );

    //
    // create the diffuser form
    //
    var $form = $( "<form></form>", { "id": config.formId } );

    createDiffuserForm();

    // make the item-lists (arguments, end-points, class-paths) sortable through drag and drop
    $( ".sortable" ).sortable();

    // set up the validator
    $( "#" + config.formId ).validate({
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
        errorPlacement: function( error ) {
            error.appendTo( function() {} );
        }
    });

    // add the handler for resetting the form
    $( "#" + config.resetButtonId ).click( function() { resetDiffuserForm() } );

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

    // allows the user to add a return type; once clicked, disables itself
    $( "#" + config.addReturnTypeButtonId ).click( function() {
        $( "#" + config.returnTypeId ).append( '<li>' +
            '<input type="text" class="' + listItemInputClass + ' ' + variableName + '" value=""  size="55">' +
            '<input type="button" id="' + config.removeReturnTypeButtonId + '" value="x">' +
            '</li>' );
        $( "#" + config.addReturnTypeButtonId ).attr( "disabled", "disabled" );
    });

    // allows the user to remove the return type; once clicked enables the add-return-type button
    $( "#" + config.removeReturnTypeButtonId ).live( "click", function() {
        $( this ).parent().remove();
        $( "#" + config.addReturnTypeButtonId ).removeAttr( "disabled" );
    });


    //
    // METHODS
    //

    /**
     * Creates the diffuser form HTML code and adds the div specified as the method argument to the containing function
     */
    function createDiffuserForm() {
        // create the fist div containing the signature information
        var $div = $( "<div></div>" ).addClass( formElementName );

        // containing class name and note
        var $para = $( "<p>Containing Class Name: </p>", { id: config.containingClassId } );
        $( "<input>", { id: config.containingClassId, type: "text" } ).addClass( "required " + className ).appendTo( $para );
        $para.appendTo( $div );
        $( "<p>This should be the fully-qualified name of the Java class containing the method.</p>" ).addClass( "note" ).appendTo( $div );

        // method name
        $para = $( "<p>Method Name: </p>", { id: config.methodNameId } );
        $( "<input>", { id: config.methodNameId, type: "text" } ).addClass( "required " + methodName ).appendTo( $para );
        $para.appendTo( $div );

        // method arguments
        $para = $( "<p></p>", { id: config.methodNameId } );
        $( "<input>", { type: "button", id: config.addArgTypeButtonId, value: "+" } ).appendTo( $para );
        $para.append( "Method Arguments" );
        $( "<ul></ul>", { id: config.methodArgListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );

        // return type
        $para = $( "<p></p>", { id: returnType } );
        $( "<input>", { type: "button", id: config.addReturnTypeButtonId, value: "+" } ).appendTo( $para );
        $para.append( "Return Type" );
        $( "<ul></ul>", { id: config.returnTypeId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );

        $div.appendTo( $form );

        // div holding the class paths
        $div = $( "<div></div>" ).addClass( formElementName );
        $para = $( "<p></p>", { id: classPaths } );
        $( "<input>", { type: "button", id: config.addClassPathButtonId, value: "+" } ).appendTo( $para );
        $para.append( "Class-Path" );
        $( "<ul></ul>", { id: config.classPathListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );
        $div.appendTo( $form );

        // div holding the additional endpoints
        $div = $( "<div></div>" ).addClass( formElementName );
        $para = $( "<p></p>", { id: endPoints } );
        $( "<input>", { type: "button", id: config.addEndpointButtonId, value: "+" } ).appendTo( $para );
        $para.append( "Additional Diffuser End-Points" );
        $( "<ul></ul>", { id: config.endPointListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );
        $div.appendTo( $form );

        // div containing the serializer name and option selection
        $div = $( "<div></div>" ).addClass( formElementName );
        $( "<p>Serializer Name: </p>", { id: serializerName } ).appendTo( $div );
        $( "<select></select>", { id: config.serializerId } ).appendTo( $div );
        $div.appendTo( $form );

        // div containing the buttons
        $div = $( "<div></div>" );
        $( "<input>", { type: "submit", id: config.submitButtonId, value: "Create" } ).appendTo( $div );
        $( "<input>", { type: "button", id: config.resetButtonId, value: "Reset" } ).appendTo( $div );
        $div.appendTo( $form );

        $form.appendTo( "#" + parentId );
    }

    /**
     * resets the create-diffuser form and add the handler
     */
    function resetDiffuserForm() {

        // grab the form used for creating the diffuser
//        var $form = $( "#" + config.formId );

        // sets all the form's text input fields to empty
        $( "#" + config.containingClassId ).val( "" );
        $( "#" + config.methodNameId ).val( "" );
//        $( "#" + config.serializerId ).option( config.defaultSerializerName );

        // removes all the form's input fields from the lists
        $( "." + listItemClass ).remove();
        $( "." + listItemInputClass ).parent().remove();

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
    }

    // the exposed diffuser form methods
    return {
        asJson : asJson(),
        resetForm : resetDiffuserForm()
    }
}