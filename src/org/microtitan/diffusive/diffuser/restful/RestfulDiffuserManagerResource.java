package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.diffuser.serializer.Serializer;

@Path( "/diffusers" )
public class RestfulDiffuserManagerResource {

	static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );
	
	// resource paths
	private static final String	DIFFUSER = "/diffusers";
//	private static final String DIFFUSER_CREATE = "/create";
//	private static final String DIFFUSER_FORM = "/form";
//	private static final String DIFFUSER_LIST = "/list";
//	private static final String DIFFUSER_EXECUTE = "/execute";
//	private static final String DIFFUSER_DELETE = "/delete";
//	private static final String DIFFUSER_GET = "/get";
	
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
	
	private final UriBuilder listUriBuilder;
	
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
		
		this.listUriBuilder = createDiffuserListBuilder();
	}
	
	private static UriBuilder createDiffuserListBuilder()
	{
		final UriBuilder builder = UriBuilder.fromResource( RestfulDiffuserManagerResource.class )
				 .host( "{hostname}" )
				 .path( RestfulDiffuserManagerResource.class, "getDiffuserList" );
		
		return builder;
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
//	@POST @Path( DIFFUSER_CREATE )
//	@Produces( MediaType.TEXT_HTML )
//	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
//	public Response createDiffuserFromForm( @FormParam( SERIALIZER_NAME ) final String serializerName,
//											@FormParam( CLIENT_ENDPOINT ) final String clientEndpoint,
//											@FormParam( CLASS_NAME ) final String containingClassName,
//											@FormParam( METHOD_NAME ) final String methodName,
//											@FormParam( ARGUMENT_TYPE ) final String argType )
//	{
//		// create the diffuser
//		final Serializer serializer = SerializerFactory.getInstance().createSerializer( serializerName );
//		final List< URI > clientEndpoints = Arrays.asList( URI.create( clientEndpoint ) );
//		final List< String > arguments = Arrays.asList( argType );
//		final String key = createDiffuser( serializer, clientEndpoints, containingClassName, methodName, arguments );
//
//		// moves the user to the list of diffusers (navigation shouldn't be here)
//		return getDiffuserListAsForm();
//	}
	
	@PUT //@Path( DIFFUSER_CREATE )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_XML )
	public Response createDiffuser( final DiffuserCreateRequest request )
	{
		// create the diffuser
		final String key = createDiffuser( request.getSerializer(), 
										   request.getClientEndpointsUri(), 
										   request.getContainingClass(), 
										   request.getMethodName(), 
										   request.getArgumentTypes() );

		
		final URI diffuserUri = createDiffuserUri( baseUri, key );
		final Response response = Response.created( diffuserUri )
										  .status( Status.OK )
										  .entity( diffuserUri )
										  .build();

		return response;
	}
	
//	@GET @Path( DIFFUSER_CREATE + DIFFUSER_FORM )
//	@Consumes( MediaType.APPLICATION_XML )
//	@Produces( MediaType.TEXT_HTML )
//	public String createCreateDiffuserForm()
//	{
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "<html><body>" );
//		buffer.append( "<h1>Create Diffuser</h1>" );
//		final String formUri = UriBuilder.fromUri( baseUri ).path( DIFFUSER_CREATE ).build().toString();
//		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + formUri + "\" >" );
//		buffer.append( "Serializer name: <input type=\"text\" name=\"" + SERIALIZER_NAME + "\" value=\"persistence_xml\" /><br />" );
//		buffer.append( "Client Endpoint: <input type=\"text\" name=\"" + CLIENT_ENDPOINT + "\" value=\"http://localhost:8183/diffusers\" /><br />" );
//		buffer.append( "Class name: <input type=\"text\" name=\"" + CLASS_NAME + "\" value=\"org.microtitan.diffusive.tests.Bean\" /><br />" );
//		buffer.append( "Method name: <input type=\"text\" name=\"" + METHOD_NAME + "\" value=\"setA\" /><br />" );
//		buffer.append( "Argument type: <input type=\"text\" name=\"" + ARGUMENT_TYPE + "\" value=\"java.lang.String\" />" );
//		buffer.append( "<input type=\"submit\" value=\"Submit\" />" );
//		buffer.append( "</form>" );
//		buffer.append( "</body></html>" );
//		return buffer.toString();
//	}
	
//	@GET @Path( "{" + SIGNATURE + "}" + DIFFUSER_EXECUTE + DIFFUSER_FORM )
//	@Consumes( MediaType.APPLICATION_XML )
//	@Produces( MediaType.TEXT_HTML )
//	public String createExecuteDiffuserForm( @PathParam( SIGNATURE ) final String signature )
//	{
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "<html><body>" );
//		buffer.append( "<h1>Execute Diffuser: " + signature + "</h1>" );
//		final String formUri = UriBuilder.fromUri( baseUri ).path( signature ).path( DIFFUSER_EXECUTE ).path( DIFFUSER_FORM ).build().toString();
//		buffer.append( "Arguments:" );
//		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + formUri + "\" >" );
//		buffer.append( "Argument Values: <input type=\"text\" name=\"" + ARGUMENT_VALUES + "\" value=\"test\" />" );
//		buffer.append( "<input type=\"submit\" value=\"Submit\" />" );
//		buffer.append( "</form>" );
//		buffer.append( "</body></html>" );
//		return buffer.toString();
//	}
	
	// TODO have to add the object representation...probably this will only work 
	// from the non-form version
//	@POST @Path( "{" + SIGNATURE + "}" + DIFFUSER_EXECUTE + DIFFUSER_FORM )
//	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
//	@Produces( MediaType.TEXT_HTML )
//	public String executeDiffuserFromForm( @PathParam( SIGNATURE ) final String signature,
//										   @FormParam( ARGUMENT_VALUES ) final String values )
//	{
//		// parse the signature into its parts so that we can call the diffuser
//		final DiffuserId diffuserId = DiffuserId.parse( signature );
//		final List< String > argumentTypes = diffuserId.getArgumentTypes();
//		
//		// split the argument values
//		final List< String > argumentValues = new ArrayList<>();
//		if( !values.isEmpty() )
//		{
//			argumentValues.addAll( Arrays.asList( values.split( Pattern.quote( "," ) ) ) );
//		}
//		
//		// ensure that for each argument type, there is an argument value
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "<html><body>" );
//		buffer.append( "<h1>Execute Diffuser: " + signature + "</h1>" );
//		if( argumentTypes.size() == argumentValues.size() )
//		{
//			for( int i = 0; i < argumentTypes.size(); ++i )
//			{
//				buffer.append( "<p>" + argumentTypes.get( i ) + " = " + argumentValues.get( i ) + "</p>" );
//			}
//
//			// grab the class name and the method name and create instantiate the object
//			try
//			{
//				final String className = diffuserId.getClassName();
//				final Class< ? > clazz = Class.forName( className );		// can also specify the class loader, which we may have to do
//				final Object object = clazz.newInstance();		// TODO need to reconstruct the class from the serialized version here
//				
//				// grab the diffuser based on the signature
//				final RestfulDiffuser diffuser = diffusers.get( signature );
//				final Object result = diffuser.runObject( object, diffuserId.getMethodName(), argumentValues.toArray( new Object[ 0 ] ) );
//				
//				buffer.append( "<p>Result: " + result );
//			}
//			catch( ClassNotFoundException | InstantiationException | IllegalAccessException e )
//			{
//				throw new IllegalArgumentException( "class not found or couldn't be instantiated", e );
//			}
//		}
//		else
//		{
//			buffer.append( "The number argument types (" + argumentTypes.size() + ") and values (" + argumentValues.size() + ") do not match." );
//		}
//		buffer.append( "</body></html>" );
//		return buffer.toString();
//	}

	@GET
	@Produces( MediaType.APPLICATION_XML )
	public Response getDiffuserList( @Context UriInfo uriInfo )
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		buffer.append( "<diffusers>" );
		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
		{
//			final UriBuilder builder = listUriBuilder.clone().build( uriInfo. );
			buffer.append( "<link>" + createDiffuserUri( baseUri, entry.getKey() ) + "</link>" );
		}
		buffer.append( "</diffusers>" );
		
		final Response response = Response.created( baseUri )
										  .status( Status.OK )
										  .entity( buffer.toString() )
										  .build();
		
		return response;
	}
	
//	@GET @Path( "{" + SIGNATURE + "}" )
//	@Consumes( MediaType.APPLICATION_XML )
//	@Produces( MediaType.APPLICATION_XML )
//	public Response getDiffuserAsForm( @PathParam( SIGNATURE ) final String signature )
//	{
//		Response response = null;
//		if( diffusers.containsKey( signature ) )
//		{
//			final URI diffuserUri = createDiffuserUri( baseUri, signature );
//			
//			final RestfulDiffuser diffuser = diffusers.get( signature );
//
//			final StringBuffer buffer = new StringBuffer();
//			buffer.append( "<html><body>" );
//			buffer.append( "<h1>Diffuser Key: " + signature + "</h1>" );
//			buffer.append( "Diffuser Class: " + diffuser.getClass().getName() );
//			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_EXECUTE + DIFFUSER_FORM + "\" >" );
//			buffer.append( "<input type=\"submit\" value=\"Execute Method\" />" );
//			buffer.append( "</form>" );
//			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_DELETE + "\" >" );
//			buffer.append( "<input type=\"submit\" value=\"Delete\" />" );
//			buffer.append( "</form>" );
//			buffer.append( "</body></html>" );
//
//			response = Response.created( diffuserUri )
//					  .status( Status.CREATED )
//					  .location( diffuserUri )
//					  .entity( buffer.toString() )
//					  .build();
//		}
//		else
//		{
//			response = Response.created( baseUri ).status( Status.BAD_REQUEST ).build();
//		}
//		
//		return response;
//	}

//	@GET @Path( /*DIFFUSER_LIST +*/ DIFFUSER_FORM )
//	@Produces( MediaType.APPLICATION_XML )
//	public Response getDiffuserListAsForm()
//	{
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "<html><body>" );
//		buffer.append( "<h1>Diffusers</h1>" );
//		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
//		{
//			final URI diffuserUri = createDiffuserUri( baseUri, entry.getKey() );
//			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() /*+ DIFFUSER_GET*/ + DIFFUSER_FORM + "\" >" );
//			buffer.append( entry.getKey() + "<input type=\"submit\" value=\"Get\" />" );
//			buffer.append( "</form>" );
//		}
//		buffer.append( "</body></html>" );
//		
//		return Response.created( baseUri )
//					   .status( Status.CREATED )
//					   .location( baseUri )
//					   .entity( buffer.toString() )
//					   .build();
//		
//	}
	
//	@GET @Path( "{" + SIGNATURE + "}" + DIFFUSER_GET + DIFFUSER_FORM )
//	@Consumes( MediaType.APPLICATION_XML )
//	@Produces( MediaType.TEXT_HTML )
//	public Response getDiffuserAsForm( @PathParam( SIGNATURE ) final String signature )
//	{
//		Response response = null;
//		if( diffusers.containsKey( signature ) )
//		{
//			final URI diffuserUri = createDiffuserUri( baseUri, signature );
//			
//			final RestfulDiffuser diffuser = diffusers.get( signature );
//
//			final StringBuffer buffer = new StringBuffer();
//			buffer.append( "<html><body>" );
//			buffer.append( "<h1>Diffuser Key: " + signature + "</h1>" );
//			buffer.append( "Diffuser Class: " + diffuser.getClass().getName() );
//			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_EXECUTE + DIFFUSER_FORM + "\" >" );
//			buffer.append( "<input type=\"submit\" value=\"Execute Method\" />" );
//			buffer.append( "</form>" );
//			buffer.append( "<form name=\"input\" method=\"get\" action=\"" + diffuserUri.toString() + DIFFUSER_DELETE + "\" >" );
//			buffer.append( "<input type=\"submit\" value=\"Delete\" />" );
//			buffer.append( "</form>" );
//			buffer.append( "</body></html>" );
//
//			response = Response.created( diffuserUri )
//					  .status( Status.CREATED )
//					  .location( diffuserUri )
//					  .entity( buffer.toString() )
//					  .build();
//		}
//		else
//		{
//			response = Response.created( baseUri ).status( Status.BAD_REQUEST ).build();
//		}
//		
//		return response;
//	}

	@DELETE @Path( "{" + SIGNATURE + "}" /*+ DIFFUSER_DELETE*/ )
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
							   .contentLocation( baseUri )//UriBuilder.fromUri( baseUri ).path( DIFFUSER_LIST ).build() )
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
