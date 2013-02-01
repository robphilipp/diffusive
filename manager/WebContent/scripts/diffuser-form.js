/**
 * User: Robert Philipp
 * Date: 1/7/13
 * Time: 9:08 AM
 */
/**
 * Creates a diffuser form object for creating diffusers. This object manages all aspects
 * of communicating with the RESTful diffuser server to create the diffuser.
 * @param parent The jQuery element into which the form should be placed
 * @param formId The ID of the form element to be created. If you need more than one of these
 * forms in one page, then you should assign a unique name to the form. All the elements in
 * this form use this ID to ensure that its elements have unique IDs
 * @param serverUri The base URI to the RESTful diffuser server
 * @param settings Overrides to the default settings found in the config field
 * @constructor
 */
function DiffuserForm( parent, serverUri, formId, settings ) {

    if( !formId ) {
        formId = "create-diffuser-form-";
    } else {
        formId += "-";
    }

    var config = {
        containingClassId : formId + "containing-class-input",
        methodNameId : formId + "method-name-input",
        returnTypeId : formId + "return-type-list",
        methodArgListId : formId + "method-argument-list",
        classPathListId : formId + "class-path-list",
        endPointListId : formId + "end-points-list",
        serializerId : formId + "serializer-name-list",
        defaultSerializerName : "persistence_xml",

        addArgTypeButtonId : formId + "add-arg-type-button",

        addReturnTypeButtonId : formId + "add-return-type-button",
        removeReturnTypeButtonId : formId + "remove-return-type-button",

        addClassPathButtonId : formId + "add-class-path-button",

        addEndpointButtonId : formId + "add-end-points-button",

        submitButtonId: formId + "create-diffuser-submit-button",
        resetButtonId : formId + "create-diffuser-reset-button",

        diffuserPath: "/diffusers",
        serializerPath: "/serializers",

        successCallback: function() {}
    };

    // private constants used only internally

    // class identifiers used for list items, the input text fields contained in the list items, and the buttons for
    // removing a list item. these are used for method argument, return type, class paths, and end points
    var listItemClass = formId + "list-item";
    var listItemInputClass = formId + "item-list-input";
    var listItemRemoveButtonClass = formId + "item-list-remove-button";

    var className = formId + "class-name";
    var methodName = formId + "method-name";
    var variableName = formId + "variable-name";
    var serializerNameId = formId + "serializer-name";

    var returnTypeId = formId + "return-type";
    var classPathsId = formId + "class-paths";
    var endPointsId = formId + "end-points";

    var formElementName = "form-element";

    // merge the configuration items specified in the function and the default ones
    $.extend( config, settings );

    var diffuserUri = serverUri + config.diffuserPath;
    var serializerUri = diffuserUri + config.serializerPath;

    //
    // create the diffuser form
    //
    parent.append( createDiffuserForm( formId ) );

    // add the list of the serializer names from the server and place them in the select UI element
    getSerializerList( serializerUri, $( "#" + config.serializerId ), config.defaultSerializerName );

    // make the item-lists (arguments, end-points, class-paths) sortable through drag and drop
    $( ".sortable" ).sortable();

    // set up the validator
    $( "#" + formId ).validate({
        submitHandler: function() {
            $.ajax({
                url: diffuserUri,
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
       $( "#" + config.methodArgListId ).append( createListItem( variableName, "parameter type (e.g. double, java.lang.String)" ) );
    });
    $( "#" + config.addArgTypeButtonId ).hover( function() {
        $( "i", $( this ) ).toggleClass( 'icon-plus-sign' );
    });

    // add class-path input field and remove-item button to the class-path list
//    $( "#" + config.addClassPathButtonId ).click( function() { addClasspathItem( config.classPathListId ) } );
    $( "#" + config.addClassPathButtonId ).click( function() {
        $( "#" + config.classPathListId ).append( createClasspathItem() );
    });

    // add end-point input field and remove-item button to the end-point list
//    $( "#" + config.addEndpointButtonId ).click( function() { addEndpointItem( config.endPointListId ) } );
    $( "#" + config.addEndpointButtonId ).click( function() {
        $( "#" +  config.endPointListId ).append( createEndpointItem() );
    });

    // remove the item from the list when the associated button is pressed
    $( "." + listItemRemoveButtonClass ).live( "click", function() {
        $( this ).parent().parent().remove();
    });

    $( "." + listItemRemoveButtonClass ).live( "hover", function() {
        $( "i", this ).toggleClass( 'icon-remove-sign' );
    });

    // allows the user to add a return type; once clicked, disables itself
    $( "#" + config.addReturnTypeButtonId ).click( function() {
        if( !$( this ).attr( "disabled" ) ) {
            $( "#" + config.returnTypeId ).append( '<li>' +
                '<div class="input-append">' +
                    '<input type="text" class="' + listItemInputClass + ' ' + variableName + '">' +
                    '<button id="' + config.removeReturnTypeButtonId + '" class="btn"><i class="icon-remove"></i></button>' +
                '</div>' +
                '</li>' );
            $( "i", this ).attr( 'class', 'icon-plus icon-white' );
            $( "#" + config.removeReturnTypeButtonId ).hover( function() {
                $( "i", this ).toggleClass( 'icon-remove-sign' );
            });
        }
        $( this ).attr( "disabled", "disabled" );
    });

    // allows the user to remove the return type; once clicked enables the add-return-type button
    $( "#" + config.removeReturnTypeButtonId ).live( "click", function() {
        $( "i", "#" + config.addReturnTypeButtonId ).attr( 'class', 'icon-plus' );
        $( this ).parent().parent().remove();
        $( "#" + config.addReturnTypeButtonId ).removeAttr( "disabled" );
    });

    //
    // METHODS
    //

    /**
     * Creates the diffuser form HTML code and adds the div specified as the method argument to the containing function
     */
    function createDiffuserForm( formId ) {

        var $form = $( "<form></form>", { "id": formId } );

        // create the fist div containing the signature information
        var $div = $( "<div></div>" ).addClass( formElementName );

        // containing class name and note
        var $para = $( "<p>Containing Class Name: </p>", { id: config.containingClassId } );
        $( "<input>", { id: config.containingClassId,
                        type: "text",
                        class: "span8",
                        placeholder: "The fully-qualified name of the Java class containing the method"
                      } ).addClass( "required " + className ).appendTo( $para );
        $para.appendTo( $div );

        // method name
        $para = $( "<p>Method Name: </p>", { id: config.methodNameId } );
        $( "<input>", { id: config.methodNameId,
                        type: "text",
                        class: "span5",
                        placeholder: "The name of the method to diffuse"
                      } ).addClass( "required " + methodName ).appendTo( $para );
        $para.appendTo( $div );

        // method arguments
        $para = $( "<p></p>", { id: config.methodNameId } );
        $( '<a id="' + config.addArgTypeButtonId + '" href="#"><i class="icon-plus"></i></a>' ).appendTo( $para );
        $para.append( "Method Arguments" );
        $( "<ul></ul>", { id: config.methodArgListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );

        // return type
        $para = $( "<p></p>", { id: returnTypeId } );
        $( '<a id="' + config.addReturnTypeButtonId + '" href="#"><i class="icon-plus"></i></a>' ).appendTo( $para );
        $para.append( "Return Type" );
        $( "<ul></ul>", { id: config.returnTypeId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );

        $div.appendTo( $form );

        // div holding the class paths
        $div = $( "<div></div>" ).addClass( formElementName );
        $para = $( "<p></p>", { id: classPathsId } );
        $( '<a id="' + config.addClassPathButtonId + '" href="#"><i class="icon-plus"></i></a>' ).appendTo( $para );
        $para.append( "Class-Path" );
        $( "<ul></ul>", { id: config.classPathListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );
        $div.appendTo( $form );

        // div holding the additional endpoints
        $div = $( "<div></div>" ).addClass( formElementName );
        $para = $( "<p></p>", { id: endPointsId } );
        $( '<a id="' + config.addEndpointButtonId + '" href="#"><i class="icon-plus"></i></a>' ).appendTo( $para );
        $para.append( "Additional Diffuser End-Points" );
        $( "<ul></ul>", { id: config.endPointListId } ).addClass( "sortable" ).appendTo( $para );
        $para.appendTo( $div );
        $div.appendTo( $form );

        // div containing the serializer name and option selection
        $div = $( "<div></div>" ).addClass( formElementName );
        $( "<p>Serializer Name: </p>", { id: serializerNameId } ).appendTo( $div );
        var serializerListElem = $( "<select></select>", { id: config.serializerId } )
        $div.append( serializerListElem );
        $div.appendTo( $form );

        // div containing the buttons
        $div = $( "<div></div>" );
        $( "<input>", { type: "submit", id: config.submitButtonId, value: "Create" } ).appendTo( $div );
        $( "<input>", { type: "button", id: config.resetButtonId, value: "Reset" } ).appendTo( $div );
        $div.appendTo( $form );

        return $form;
    }

    /**
     * resets the create-diffuser form and add the handler
     */
    function resetDiffuserForm() {

        // removes the return-type text field and adds the add-return-type button back, in case it was gone
        $( "#" + config.removeReturnTypeButtonId ).parent().parent().remove();
        $( "#" + config.addReturnTypeButtonId ).removeAttr( "disabled" );
        $( "i", "#" + config.addReturnTypeButtonId ).attr( 'class', 'icon-plus' );

        // sets all the form's text input fields to empty
        $( "#" + config.containingClassId ).val( "" );
        $( "#" + config.methodNameId ).val( "" );

        // removes all the form's input fields from the lists
        $( "." + listItemClass ).remove();
        $( "." + listItemInputClass ).parent().remove();
    }

    /**
     * Returns the values from the input elements with class specified in the listItemInputClass
     * for the list element with the specified list
     * @param list The jQuery element representing the list holding the input elements
     * @return {Array} The values of the input elements
     */
    function getValues( list ) {
        var argElems = list.find( "." + listItemInputClass ).toArray();
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
            argument = $( argElems[ 0 ] ).val()
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
     * Adds an end-point item to the list of end-points
     * @param selector The class selector into which the new list item will be appended
     * @return {*|jQuery|HTMLElement}
     */
    function createEndpointItem() {
        return createListItem( "url", "http://remote.ip.address:8182/diffuser" );
    }

    /**
     * Adds an end-point item to the list of end-points
     * @param selector The class selector into which the new list item will be appended
     * @return {*|jQuery|HTMLElement}
     */
    function createClasspathItem() {
        return createListItem( "url", "http://remote.ip.address:8182/classpath" );
    }

    /**
     * Creates a jQuery element representing the list item with a sortable handle and a remove button.
     * Intended for the method argument type list, the class path list, and the end-point list.
     * @param selector The class selector into which the new list item will be appended
     * @param validationClass The validation type for the text field
     * @param placeholder The place holder value in the text field
     * @return {*|jQuery|HTMLElement}
     */
    function createListItem( validationClass, placeholder ) {
        if( !placeholder ) {
            placeholder = " ";
        }
        var item =
            '<li class="' + listItemClass + '">' +
                '<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>' +
                '<div class="input-append">' +
                    '<input type="text" class="' + listItemInputClass + ' ' + validationClass + ' span8" placeholder="' + placeholder + '">' +
                    '<button class="btn '+ listItemRemoveButtonClass + '" type="button"><i class="icon-remove"></i></button>' +
                '</div>' +
            '</li>';
        return $( item );
    }

    /**
     * @return {*} a JSON representation of the diffuser
     */
    function asJson() {
        var formValues = {
            containingClassName : $( "#" + config.containingClassId ).val(),
            methodName : $( "#" + config.methodNameId ).val(),
            returnTypeClassName : getReturnType(),
            argumentTypes : getValues( $( "#" + config.methodArgListId ) ),
            classPaths : getValues( $( "#" + config.classPathListId ) ),
            serializerType : getSerializerName(),
            clientEndpoints : getValues( $( "#" + config.endPointListId ) )
        };
        return JSON.stringify( formValues );
    }
}