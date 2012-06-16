package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

@Path( "/diffuser" )
public class RestfulDiffuserManagerResource {

	private Map< String, RestfulDiffuser > diffusers;
	private int numCalls = 0;
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
	
	public RestfulDiffuserManagerResource()
	{
		diffusers = new HashMap<>();
	}
	
	public static final String SERIALIZER_NAME = "serializer_name";
	public static final String CLIENT_ENDPOINT = "client_endpoint";
	public static final String CLASS_NAME = "class_name";
	public static final String METHOD_NAME = "method_name";
	public static final String ARGUMENT_TYPE = "argument_type";
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@POST @Path( "/create" )
//	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_PLAIN )
//	@Produces( MediaType.APPLICATION_XML )
//	public JAXBElement< DiffuserCreateResponse > createDiffuser( final JAXBElement< DiffuserCreateRequest > requestElement )
	@Consumes( "application/x-www-form-urlencoded" )
	public String createDiffuser( @FormParam( SERIALIZER_NAME ) final String serializerName,
								  @FormParam( CLIENT_ENDPOINT ) final String clientEndpoint,
								  @FormParam( CLASS_NAME ) final String className,
								  @FormParam( METHOD_NAME ) final String methodName,
								  @FormParam( ARGUMENT_TYPE ) final String argType )
	{
		// create the diffuser
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( serializerName );
		final List< URI > clientEndpoints = Arrays.asList( URI.create( clientEndpoint ) );
		final RestfulDiffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		
		// create the name/id for the diffuser
		final StringBuffer buffer = new StringBuffer();
		buffer.append( className + "." );
		buffer.append( methodName + ":" );
		buffer.append( argType + ";" );
//		for( Class< ? > type : request.getArgumentTypes() )
//		{
//			buffer.append( type.getName() + ";" );
//		}

		// add the diffuser to the map of diffusers
		final String key = buffer.toString();
		final RestfulDiffuser oldDiffuser = diffusers.put( key, diffuser );
		
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
		return key;
	}
	
	@GET @Path( "/create" )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.TEXT_HTML )
	public String testCreate()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "<html><body>" );
		buffer.append( "<h1>Create Calculator</h1>" );
		buffer.append( "<form name=\"input\" method=\"post\">" );
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
}
