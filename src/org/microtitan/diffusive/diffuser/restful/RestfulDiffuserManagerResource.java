package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

@Path( "/diffusers" )
public class RestfulDiffuserManagerResource {

	static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );
	
	// resource paths
	private static final String	DIFFUSER = "/diffusers";
	private static final String DIFFUSER_CREATE = "/create";
	private static final String DIFFUSER_FORM = "/form";
	private static final String DIFFUSER_LIST = "/list";
	private static final String DIFFUSER_EXECUTE = "/execute";
	private static final String DIFFUSER_DELETE = "/delete";
	private static final String DIFFUSER_GET = "/get";
	
	// parameters for creating a diffuser
	public static final String SERIALIZER_NAME = "serializer_name";
	public static final String CLIENT_ENDPOINT = "client_endpoint";
	public static final String CLASS_NAME = "class_name";
	public static final String METHOD_NAME = "method_name";
	public static final String ARGUMENT_TYPE = "argument_type";
	
	// parameters for retrieving a diffuser
	public static final String SIGNATURE = "signature";
	public static final String ARGUMENT_VALUES = "argument_values";
	
	private Map< String, RestfulDiffuser > diffusers;
	private URI baseUri;
	
	
//	private Serializer serializer;
//	private List< URI > clientEndpoints;
//	
//	public RestfulDiffuserManagerResource( final Serializer serializer, final List< URI > clientEndpoints )
//	{
//		if( clientEndpoints == null )
//		{
//			this.clientEndpoints = new ArrayList<>();
//		}
//		else
//		{
//			this.clientEndpoints = clientEndpoints;
//		}
//		
//		Require.notNull( serializer );
//		this.serializer = serializer;
//	}
	
	public RestfulDiffuserManagerResource( final URI baseUri )
	{
		diffusers = new HashMap<>();
		this.baseUri = createBaseUri( baseUri );
	}
	
	/**
	 * Constructs the base URI from which all the resource URIs start
	 * @param uri The base URI to which we add {@link #DIFFUSER} (={@value #DIFFUSER}) to the end
	 * @return The base URI 
	 */
	private static URI createBaseUri( final URI uri )
	{
		return UriBuilder.fromUri( uri ).path( DIFFUSER ).build();
	}
	
	/*
	 * Creates the diffuser and crafts the response. Decouples the way the information is sent from
	 * the creation of the diffuser and the response
	 * @param serializer The {@link Serializer} used to serialize/deserialize objects
	 * @param clientEndpoints The URI of the endpoints to which object requests can be sent 
	 * @param containingClassName The name of the class containing the method to execute
	 * @param methodName The name of the method to execute
	 * @param argumentTypes The parameter types that form part of the method's signature
	 * @return A {@link Response} containing the link to the newly created diffuser.
	 */
	private String createDiffuser( final Serializer serializer,
								   final List< URI > clientEndpoints,
								   final String containingClassName,
								   final String methodName,
								   final List< String > argumentTypes )
	{
		// create the diffuser
		final RestfulDiffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		
		// create the name/id for the diffuser
		final String key = DiffuserId.create( containingClassName, methodName, argumentTypes );

		// add the diffuser to the map of diffusers
		final RestfulDiffuser oldDiffuser = diffusers.put( key, diffuser );

		return key;
		
//		DiffuserCreateRequest request = requestElement.getValue();
		
//		// create the new diffuser and pass back the link to this new resource.
//		final RestfulDiffuser diffuser = new RestfulDiffuser( request.getSerializer(), request.getClientEndpoints() );
//		
//		// create the name/id for the diffuser
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( request.getContainingClass().getName() + "." );
//		buffer.append( request.getMethodName() + ":" );
//		for( Class< ? > type : request.getArgumentTypes() )
//		{
//			buffer.append( type.getName() + ";" );
//		}
//
//		final String key = buffer.toString();
//		final RestfulDiffuser oldDiffuser = diffusers.put( key, diffuser );
		
//		DiffuserCreateResponse response = new DiffuserCreateResponse();
//		response.status( Status.CREATED );
//		response.
		
//		return new JAXBElement< DiffuserCreateResponse >( null, DiffuserCreateResponse.class, response );
//		final Response response = Response.created( URI.create( "http://localhost:8182/diffuser/" + classDef ) )
//										  .status( Status.CREATED )
//										  .entity( classDef )
//										  .build();
//		return response;
	}

	/*
	 * Constructs the URI for the diffuser with the specified base URI and specified key
	 * @param baseUri The base URI from which the diffuser was created
	 * @param key The key representing the diffuser
	 * @return The URI for the diffuser
	 */
	private static URI createDiffuserUri( final URI baseUri, final String key )
	{
		return UriBuilder.fromUri( baseUri ).path( key ).build();
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@POST @Path( DIFFUSER_CREATE )
//	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
//	@Produces( MediaType.APPLICATION_XML )
//	public JAXBElement< DiffuserCreateResponse > createDiffuser( final JAXBElement< DiffuserCreateRequest > requestElement )
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	public Response createDiffuserFromForm( @FormParam( SERIALIZER_NAME ) final String serializerName,
											@FormParam( CLIENT_ENDPOINT ) final String clientEndpoint,
											@FormParam( CLASS_NAME ) final String containingClassName,
											@FormParam( METHOD_NAME ) final String methodName,
											@FormParam( ARGUMENT_TYPE ) final String argType )
	{
		// create the diffuser
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( serializerName );
		final List< URI > clientEndpoints = Arrays.asList( URI.create( clientEndpoint ) );
		final List< String > arguments = Arrays.asList( argType );
		final String key = createDiffuser( serializer, clientEndpoints, containingClassName, methodName, arguments );

		// call the form based get method to present to the user
//		return getDiffuserAsForm( key );
		return getDiffuserListAsForm();
	}
	
	@GET @Path( DIFFUSER_CREATE + DIFFUSER_FORM )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
	public String createCreateDiffuserForm()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Create Diffuser</h1>" );
		final String formUri = UriBuilder.fromUri( baseUri ).path( DIFFUSER_CREATE ).build().toString();
		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + formUri + "\" >" );
		buffer.append( "Serializer name: <input type=\"text\" name=\"" + SERIALIZER_NAME + "\" value=\"persistence_xml\" /><br />" );
		buffer.append( "Client Endpoint: <input type=\"text\" name=\"" + CLIENT_ENDPOINT + "\" value=\"http://localhost:8183/diffusers\" /><br />" );
		buffer.append( "Class name: <input type=\"text\" name=\"" + CLASS_NAME + "\" value=\"class1\" /><br />" );
		buffer.append( "Method name: <input type=\"text\" name=\"" + METHOD_NAME + "\" value=\"method1\" /><br />" );
		buffer.append( "Argument type: <input type=\"text\" name=\"" + ARGUMENT_TYPE + "\" value=\"arg1\" />" );
		buffer.append( "<input type=\"submit\" value=\"Submit\" />" );
		buffer.append( "</form>" );
		buffer.append( "</body></html>" );
		return buffer.toString();
	}
	
	@GET @Path( "{" + SIGNATURE + "}" + DIFFUSER_EXECUTE + DIFFUSER_FORM )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
	public String createExecuteDiffuserForm( @PathParam( SIGNATURE ) final String signature )
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Execute Diffuser: " + signature + "</h1>" );
		final String formUri = UriBuilder.fromUri( baseUri ).path( signature ).path( DIFFUSER_EXECUTE ).path( DIFFUSER_FORM ).build().toString();
		buffer.append( "Arguments:" );
		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + formUri + "\" >" );
		buffer.append( "Argument Values: <input type=\"text\" name=\"" + ARGUMENT_VALUES + "\" value=\"test\" />" );
		buffer.append( "<input type=\"submit\" value=\"Submit\" />" );
		buffer.append( "</form>" );
		buffer.append( "</body></html>" );
		return buffer.toString();
	}
	
	@POST @Path( "{" + SIGNATURE + "}" + DIFFUSER_EXECUTE + DIFFUSER_FORM )
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	@Produces( MediaType.TEXT_HTML )
	public String executeDiffuserFromForm( @PathParam( SIGNATURE ) final String signature,
										   @FormParam( ARGUMENT_VALUES ) final String value )
	{
		// parse the signature into its parts so that we can call the diffuser
		final DiffuserId diffuserId = DiffuserId.parse( signature );
		final List< String > argumentTypes = diffuserId.getArgumentTypes();
		
		// split the argument values
		final List< String > argumentValues = Arrays.asList( value.split( Pattern.quote( "," ) ) );
		
		// ensure that for each argument type, there is an argument value
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Execute Diffuser: " + signature + "</h1>" );
		if( argumentTypes.size() == argumentValues.size() )
		{
			for( int i = 0; i < argumentTypes.size(); ++i )
			{
				buffer.append( "<p>" + argumentTypes.get( i ) + " = " + argumentValues.get( i ) + "</p>" );
			}

			// grab the diffuser based on the signature
			final RestfulDiffuser diffuser = diffusers.get( signature );
//			diffuser.runObject( object, methodName, arguments );
		}
		else
		{
			buffer.append( "The number argument types and values do not match." );
		}
		buffer.append( "</body></html>" );
		return buffer.toString();
	}

	@GET @Path( DIFFUSER_LIST )
	@Produces( MediaType.APPLICATION_XML )
	public String getDiffuserList()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		buffer.append( "<diffusers>" );
		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
		{
			buffer.append( "<key>" + entry.getKey() + "</key>" );
			buffer.append( "<class>" + entry.getValue().getClass().getName() + "</class>" );
			buffer.append( "<details>" + entry.getValue().toString() + "</details>" );
		}
		buffer.append( "</diffusers>" );
		return buffer.toString();
	}
	
	@GET @Path( DIFFUSER_LIST + DIFFUSER_FORM )
	@Produces( MediaType.APPLICATION_XML )
	public Response getDiffuserListAsForm()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Diffusers</h1>" );
		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
		{
			final URI diffuserUri = createDiffuserUri( baseUri, entry.getKey() );
			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_GET + DIFFUSER_FORM + "\" >" );
			buffer.append( entry.getKey() + "<input type=\"submit\" value=\"Get\" />" );
			buffer.append( "</form>" );
		}
		buffer.append( "</body></html>" );
		
		return Response.created( baseUri )
					   .status( Status.CREATED )
					   .location( baseUri )
					   .entity( buffer.toString() )
					   .build();
		
	}
	
	@GET @Path( "{" + SIGNATURE + "}" + DIFFUSER_GET + DIFFUSER_FORM )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
	public Response getDiffuserAsForm( @PathParam( SIGNATURE ) final String signature )
	{
		Response response = null;
		if( diffusers.containsKey( signature ) )
		{
			final URI diffuserUri = createDiffuserUri( baseUri, signature );
			
			final RestfulDiffuser diffuser = diffusers.get( signature );

			final StringBuffer buffer = new StringBuffer();
			buffer.append( "<html><body>" );
			buffer.append( "<h1>Diffuser Key: " + signature + "</h1>" );
			buffer.append( "Diffuser Class: " + diffuser.getClass().getName() );
			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_EXECUTE + DIFFUSER_FORM + "\" >" );
			buffer.append( "<input type=\"submit\" value=\"Execute Method\" />" );
			buffer.append( "</form>" );
			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_DELETE + "\" >" );
			buffer.append( "<input type=\"submit\" value=\"Delete\" />" );
			buffer.append( "</form>" );
			buffer.append( "</body></html>" );

			response = Response.created( diffuserUri )
					  .status( Status.CREATED )
					  .location( diffuserUri )
					  .entity( buffer.toString() )
					  .build();
		}
		else
		{
			response = Response.created( baseUri ).status( Status.BAD_REQUEST ).build();
		}
		
		return response;
	}

	@GET @Path( "{" + SIGNATURE + "}" + DIFFUSER_DELETE )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_XML )
	public Response deleteDiffuser( @PathParam( SIGNATURE ) final String signature )
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		buffer.append( "<diffuser>" + signature + "</diffuser>" );

		Response response = null;
		if( diffusers.containsKey( signature ) )
		{
			diffusers.remove( signature );
			response = Response.created( baseUri )
							   .status( Status.OK )
							   .contentLocation( UriBuilder.fromUri( baseUri ).path( DIFFUSER_LIST ).build() )
							   .entity( buffer.toString() )
							   .build();
		}
		else
		{
			buffer.append( "<message>failed</message>" );
			response = Response.created( baseUri ).status( Status.BAD_REQUEST ).entity( buffer.toString() ).build();
		}
		return response;
	}
	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		System.out.println( DiffuserId.parse( "java.lang.String:concat(java.lang.String,java.lang.String)" ).toString() );
		System.out.println( DiffuserId.parse( "java.lang.String:concat(java.lang.String)" ).toString() );
		System.out.println( DiffuserId.parse( "java.lang.String:concat()" ).toString() );
		System.out.println( DiffuserId.parse( "java.lang.String:concat( java.lang.String, java.lang.String )" ).toString() );
		System.out.println( DiffuserId.parse( "java.lang.String:concat.test(java.lang.String,java.lang.String)" ).toString() );
//		System.out.println( DiffuserId.parse( "java.lang.String:concat(java.lang.String,java.lang.String)" ).toString() );
	}
}
