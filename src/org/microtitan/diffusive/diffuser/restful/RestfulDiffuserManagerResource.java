package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.diffuser.restful.test.RestfulDiffuserManagerClient;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.launcher.DiffusiveLauncher;

/**
 * Use the {@link RestfulDiffuserManagerClient} for testing this resource against the server, which is
 * either started with the {@link DiffusiveLauncher} or the {@link RestfulDiffuserServer}.
 * 
 * @author rob
 *
 */
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
	private String create( final Serializer serializer,
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
	
	/**
	 * 
	 * @param uriInfo
	 * @param request
	 * @return
	 */
	@PUT
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response create( @Context final UriInfo uriInfo, final CreateDiffuserRequest request )
	{
		// create the diffuser
		final String key = create( request.getSerializer(), 
								   request.getClientEndpointsUri(), 
								   request.getContainingClass(), 
								   request.getMethodName(), 
								   request.getArgumentTypes() );

		// create the URI to the newly created diffuser
		final URI diffuserUri = uriInfo.getAbsolutePathBuilder().path( key ).build();
		
		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed
		final Feed feed = Atom.createFeed( diffuserUri, "Create RESTful diffuser: " + key, date, uriInfo.getBaseUri() );
		
		// create the response
		final Response response = Response.created( diffuserUri )
										  .status( Status.OK )
										  .location( diffuserUri )
										  .entity( feed.toString() )
										  .type( MediaType.APPLICATION_ATOM_XML )
										  .build();

		return response;
	}
	
	/**
	 * 
	 * @param uriInfo
	 * @param signature
	 * @return
	 */
	@GET @Path( "{" + SIGNATURE + "}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response get( @Context final UriInfo uriInfo, @PathParam( SIGNATURE ) final String signature )
	{
		// create the URI to the newly created diffuser
		final URI diffuserUri = uriInfo.getAbsolutePathBuilder().build();

		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		if( diffusers.containsKey( signature ) )
		{
			diffusers.remove( signature );
			
			// create the atom feed
			final Feed feed = Atom.createFeed( diffuserUri, "RESTful diffuser: " + signature, date, uriInfo.getBaseUri() );
			
			// create the response
			response = Response.created( diffuserUri )
							   .status( Status.OK )
							   .location( diffuserUri )
							   .entity( feed.toString() )
							   .type( MediaType.APPLICATION_ATOM_XML )
							   .build();
		}
		else
		{
			final Feed feed = AbderaFactory.getInstance().newFeed();
			feed.setId( "tag:" + diffuserUri.toString() );
			feed.setTitle( "Failed to Delete RESTful Diffuser" );
			feed.setSubtitle( signature );
			feed.setUpdated( date );
			feed.addAuthor( "Diffusive by microTITAN" );
			feed.complete();

			response = Response.created( diffuserUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
		return response;
	}

	@POST @Path( "{" + SIGNATURE + "}" )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response execute( @Context final UriInfo uriInfo, 
							 @PathParam( SIGNATURE ) final String signature,
							 final ExecuteDiffuserRequest request )
	{
		// parse the signature into its parts so that we can call the diffuser
		final DiffuserId diffuserId = DiffuserId.parse( signature );
		final List< String > argumentTypes = diffuserId.getArgumentTypes();
		
		// grab the argument types and validate that they are equal
		final List< String > requestArgTypes = request.getArgumentTypes();
		if( !requestArgTypes.equals( argumentTypes ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The RESTful diffuser's argument types do not match those from the request" + Constants.NEW_LINE );
			message.append( "  ( Diffuser Argument Type, Request Argument Type )" + Constants.NEW_LINE );
			for( int i = 0; i < argumentTypes.size(); ++i )
			{
				message.append( "  (" + argumentTypes.get( i ) + ", " + requestArgTypes.get( i ) + ")" + Constants.NEW_LINE );
			}
			throw new IllegalArgumentException( message.toString() );
		}

		// deserialize the arguments
		
		// deserialize the object
		
		// call the method
		
		// TODO fix: this is just a place holder
		final Response response = Response.created( URI.create( "http://localhost" ) )
				  .status( Status.OK )
				  .location( URI.create( "http://localhost" ) )
				  .entity( "" )
				  .type( MediaType.APPLICATION_ATOM_XML )
				  .build();
		
		return response;
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
	public Response getList( @Context final UriInfo uriInfo )
	{
		// grab the base URI builder for absolute paths and build the base URI
		final UriBuilder baseUriBuilder = uriInfo.getAbsolutePathBuilder();
		final URI baseUri = baseUriBuilder.build();

		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed
		final Feed feed = Atom.createFeed( baseUri, "List RESTful diffusers", date, baseUri );

		// add an entry for each diffuser
		for( Map.Entry< String, RestfulDiffuser > entry : diffusers.entrySet() )
		{
			// grab the key for the diffuser
			final String key = entry.getKey();
			
			// create URI that links to the diffuser
			final URI diffuserUri = baseUriBuilder.clone().path( key ).build();

			// create the entry and it to the feed 
			final Entry feedEntry = Atom.createEntry( diffuserUri, key, date );
			feedEntry.setSummaryAsHtml( "<p>RESTful Diffuser for: " + key + "</p>" );
			feed.addEntry( feedEntry );
		}
		
		final Response response = Response.created( baseUriBuilder.build() )
				  .status( Status.OK )
				  .location( baseUri )
				  .entity( feed.toString() )
				  .type( MediaType.APPLICATION_ATOM_XML )
				  .build();
		
		return response;
	}
	
	@DELETE @Path( "{" + SIGNATURE + "}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response delete( @Context final UriInfo uriInfo, @PathParam( SIGNATURE ) final String signature )
	{
		// create the URI to the newly created diffuser
		final URI diffuserUri = uriInfo.getAbsolutePathBuilder().build();

		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		if( diffusers.containsKey( signature ) )
		{
			diffusers.remove( signature );
			
			// create the atom feed
			final Feed feed = Atom.createFeed( diffuserUri, "Delete RESTful Diffuser", date );
			
			// create the response
			response = Response.created( diffuserUri )
							   .status( Status.OK )
							   .location( diffuserUri )
							   .entity( feed.toString() )
							   .type( MediaType.APPLICATION_ATOM_XML )
							   .build();
		}
		else
		{
			// create the atom feed
			final Feed feed = Atom.createFeed( diffuserUri, "Failder to Delete RESTful Diffuser", date );

			// create the error response
			response = Response.created( diffuserUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
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

}
