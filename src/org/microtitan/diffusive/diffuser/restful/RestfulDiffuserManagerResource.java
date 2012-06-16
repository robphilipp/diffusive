package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

@Path( "/diffuser" )
public class RestfulDiffuserManagerResource {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );
	
	// resource paths
	private static final String	DIFFUSER = "/diffuser";
	private static final String DIFFUSER_CREATE = "/create";
	private static final String DIFFUSER_FORM = "/form";
	private static final String DIFFUSER_LIST = "/list";
	
	public static final String SERIALIZER_NAME = "serializer_name";
	public static final String CLIENT_ENDPOINT = "client_endpoint";
	public static final String CLASS_NAME = "class_name";
	public static final String METHOD_NAME = "method_name";
	public static final String ARGUMENT_TYPE = "argument_type";
	
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
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@POST @Path( DIFFUSER_FORM + DIFFUSER_CREATE )
//	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_PLAIN )
//	@Produces( MediaType.APPLICATION_XML )
//	public JAXBElement< DiffuserCreateResponse > createDiffuser( final JAXBElement< DiffuserCreateRequest > requestElement )
	@Consumes( "application/x-www-form-urlencoded" )
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
		return createDiffuser( serializer, clientEndpoints, containingClassName, methodName, arguments );
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
	private Response createDiffuser( final Serializer serializer,
									 final List< URI > clientEndpoints,
									 final String containingClassName,
									 final String methodName,
									 final List< String > argumentTypes )
	{
		// create the diffuser
		final RestfulDiffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		
		// create the name/id for the diffuser
		final StringBuffer buffer = new StringBuffer();
		buffer.append( containingClassName + "." );
		buffer.append( methodName + ":" );
		for( String type : argumentTypes )
		{
			buffer.append( type + ";" );
		}

		// add the diffuser to the map of diffusers
		final String key = buffer.toString();
		final RestfulDiffuser oldDiffuser = diffusers.put( key, diffuser );
		
//		final URI diffuserUri = URI.create( baseUri.toString() + "/" + key );
		final URI diffuserUri = UriBuilder.fromUri( baseUri ).path( key ).build();
		final Response response = Response.created( diffuserUri )
										  .status( Status.CREATED )
										  .location( diffuserUri )
										  .entity( key )
										  .build();
		
		return response;
		
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
	
	@GET @Path( DIFFUSER_FORM )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
	public String createForm()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Create Calculator</h1>" );
		final String formUri = UriBuilder.fromUri( baseUri ).path( DIFFUSER_FORM ).path( DIFFUSER_CREATE ).build().toString();
		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + formUri + "\" >" );
//		buffer.append( "<form name=\"input\" method=\"post\" action=\"" + baseUri + DIFFUSER_FORM + DIFFUSER_CREATE + "\" >" );
		buffer.append( "Serializer name: <input type=\"text\" name=\"" + SERIALIZER_NAME + "\" value=\"persistence_xml\" /><br />" );
		buffer.append( "Client Endpoint: <input type=\"text\" name=\"" + CLIENT_ENDPOINT + "\" value=\"http://localhost:8183/diffuser\" /><br />" );
		buffer.append( "Class name: <input type=\"text\" name=\"" + CLASS_NAME + "\" value=\"class1\" /><br />" );
		buffer.append( "Method name: <input type=\"text\" name=\"" + METHOD_NAME + "\" value=\"method1\" /><br />" );
		buffer.append( "Argument type: <input type=\"text\" name=\"" + ARGUMENT_TYPE + "\" value=\"arg1\" />" );
		buffer.append( "<input type=\"submit\" value=\"Submit\" />" );
		buffer.append( "</form>" );
		buffer.append( "</body></html>" );
		return buffer.toString();
	}
	
	@GET @Path( DIFFUSER_LIST )
	@Produces( MediaType.APPLICATION_XML )
	public String getDiffusers()
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
}
