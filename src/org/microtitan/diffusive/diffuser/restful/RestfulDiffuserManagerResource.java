package org.microtitan.diffusive.diffuser.restful;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import javax.xml.bind.JAXBElement;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.readers.ReaderInputStream;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.tests.Bean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path( "/diffusers" )
public class RestfulDiffuserManagerResource {

	static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );
	
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

	/**
	 * 
	 * @param baseUri
	 */
	public RestfulDiffuserManagerResource()
	{
		diffusers = new HashMap<>();
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
		/*final RestfulDiffuser oldDiffuser = */diffusers.put( key, diffuser );

		return key;
	}
	
	@PUT
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response createDiffuser( @Context final UriInfo uriInfo, final DiffuserCreateRequest request )
	{
		// create the diffuser
		final String key = createDiffuser( request.getSerializer(), 
										   request.getClientEndpointsUri(), 
										   request.getContainingClass(), 
										   request.getMethodName(), 
										   request.getArgumentTypes() );

		// create the URI to the newly created diffuser
		final URI diffuserUri = uriInfo.getAbsolutePathBuilder().path( key ).build();
		
		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed
		final Abdera abdera = AbderaFactory.getInstance();
		final Feed feed = abdera.newFeed();
		feed.setId( "tag:" + diffuserUri.getHost() + "," + UUID.randomUUID() + ":" + diffuserUri.getPath() );
		feed.setTitle( "RESTful Diffuser" );
		feed.setSubtitle( "Create" );
		feed.setUpdated( date );
		feed.addAuthor( "Diffusive by microTITAN" );
		feed.addLink( diffuserUri.toString(), "self" );
		feed.complete();
		
		// create the response
		final Response response = Response.created( diffuserUri )
										  .status( Status.OK )
										  .location( diffuserUri )
										  .entity( feed.toString() )
										  .type( MediaType.APPLICATION_ATOM_XML )
										  .build();

		return response;
	}
	
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
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response getDiffuserList( @Context final UriInfo uriInfo )
	{
		// grab the base URI builder for absolute paths and build the base URI
		final UriBuilder baseUriBuilder = uriInfo.getAbsolutePathBuilder();
		final URI baseUri = baseUriBuilder.build();

		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed
		final Abdera abdera = AbderaFactory.getInstance();
		final Feed feed = abdera.newFeed();
		feed.setId( "tag:" + baseUri.getHost() + "," + UUID.randomUUID() + ":" + baseUri.getPath() );
		feed.setTitle( "RESTful Diffusers" );
		feed.setSubtitle( "List" );
		feed.setUpdated( date );
		feed.addAuthor( "Diffusive by microTITAN" );
		feed.addLink( baseUri.toString(), "self" );

		// add an entry for each diffuser
		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
		{
			// grab the key for the diffuser
			final String key = entry.getKey();
			
			// create URI that links to the diffuser
			final URI diffuserUri = baseUriBuilder.clone().path( entry.getKey() ).build();

			final Entry feedEntry = feed.addEntry();
			feedEntry.setId( "tag:" + diffuserUri.getHost() + "," + UUID.randomUUID() + ":" + diffuserUri.getPath() );
			feedEntry.setTitle( key );
			feedEntry.setSummaryAsHtml( "<p>RESTful Diffuser for: " + key + "</p>" );
			feedEntry.setUpdated( date );
			feedEntry.setPublished( date );
			feedEntry.addLink( diffuserUri.toString(), "self" );
		}
		
		final Response response = Response.created( baseUriBuilder.build() )
				  .status( Status.OK )
				  .location( baseUri )
				  .entity( feed.toString() )
				  .type( MediaType.APPLICATION_ATOM_XML )
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

	@DELETE @Path( "{" + SIGNATURE + "}" )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_XML )
	public Response deleteDiffuser( @Context final UriInfo uriInfo, @PathParam( SIGNATURE ) final String signature )
	{
		// create the URI to the newly created diffuser
		final URI baseUri = uriInfo.getAbsolutePathBuilder().build();
		
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
//		Logger.getRootLogger().setLevel( Level.DEBUG );

		// atom parser/create
		final Abdera abdera = AbderaFactory.getInstance();

		//
		// create a diffuser
		//
		final Client client = Client.create();
		WebResource resource = client.resource( "http://localhost:8182/diffusers" );
		
		final DiffuserCreateRequest request = DiffuserCreateRequest.create( Bean.class.getName(), "getA" );
		final ClientResponse createDiffuserResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).put( ClientResponse.class, request );
		try( InputStream response = createDiffuserResponse.getEntity( InputStream.class ) )
		{
			final Document< Feed > document = abdera.getParser().parse( response );
			final Feed feed = document.getRoot();
			System.out.println( feed.getTitle() );
			System.out.println( feed.getLink( "self" ) );
			for( Entry entry : feed.getEntries() )
			{
				System.out.println( "\t" + entry.getTitle() );
				System.out.println( "\t" + entry.getLink( "self" ) );
			}
			System.out.println( feed.getAuthor() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
		//
		// list the diffusers
		//
		final ClientResponse listDiffusersResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );
		try( InputStream response = listDiffusersResponse.getEntity( InputStream.class ) )
		{
			final Document< Feed > document = abdera.getParser().parse( response );
			final Feed feed = document.getRoot();
			System.out.println( feed.getTitle() );
			System.out.println( feed.getLink( "self" ) );
			for( Entry entry : feed.getEntries() )
			{
				System.out.println( "\t" + entry.getTitle() );
				System.out.println( "\t" + entry.getLink( "self" ) );
			}
			System.out.println( feed.getAuthor() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
	}
}
