package org.microtitan.diffusive.diffuser.restful;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
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

	// the maximum number of resultsCache cached
	private static final int MAX_RESULTS_CACHED = 100;
	
	// parameters for creating a diffuser
	public static final String SERIALIZER_NAME = "serializer_name";
	public static final String CLIENT_ENDPOINT = "client_endpoint";
	public static final String CLASS_NAME = "class_name";
	public static final String METHOD_NAME = "method_name";
	public static final String ARGUMENT_TYPE = "argument_type";
	
	// parameters for retrieving a diffuser
	public static final String SIGNATURE = "signature";
	public static final String ARGUMENT_VALUES = "argument_values";
	
	// parameters for retrieving results of an execute
	public static final String RESULT_ID = "result_id";
	
	private final Map< String, RestfulDiffuser > diffusers;
	
	// fields to manage the resultsCache cache
	private final Map< String, ResultCacheEntry > resultsCache;
	private int maxResultsCached;

	/**
	 * 
	 * @param baseUri
	 */
	public RestfulDiffuserManagerResource()
	{
		diffusers = new HashMap<>();
		resultsCache = new LinkedHashMap<>();
		maxResultsCached = MAX_RESULTS_CACHED;
	}
	
	/*
	 * Adds a result to the resultsCache cache. If the addition of this result causes the
	 * number of cached items to increase beyond the maximum allowable items, then the
	 * item that was first added is removed.
	 * @param signature The signature of the diffuser (class_name.method_name(arg_type1, arg_type2))
	 * @param requestId The ID associated with the request
	 * @param result The result result to be added to the results cache
	 */
	private String addResults( final String signature, final String requestId, final Object result, final Serializer serializer )
	{
		// put the new result to the cache
		final String key = createResultsCacheId( signature, requestId );
		final Object previousResults = resultsCache.put( key, new ResultCacheEntry( result, serializer ) );
		if( previousResults == null && resultsCache.size() > maxResultsCached )
		{
			final Iterator< Map.Entry< String, ResultCacheEntry > > iter = resultsCache.entrySet().iterator();
			resultsCache.remove( iter.next().getKey() );
		}
		return key;
	}

	/*
	 * Returns the result from the {@link #resultsCache} by generating the cache key from the
	 * specified signature and request ID
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return the result from the {@link #resultsCache} associated with the specified signature 
	 * and request ID 
	 */
	private ResultCacheEntry getResultFromCache( final String signature, final String requestId )
	{
		return resultsCache.get( createResultsCacheId( signature, requestId ) );
	}

	/*
	 * Creates the results cache ID used as the key into the {@link #resultsCache} {@link Map}.
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return The key for the {@link #resultsCache} {@link Map}.
	 */
	private static String createResultsCacheId( final String signature, final String requestId )
	{
		return signature + "/" + requestId;
	}
	
	/*
	 * Creates the diffuser and crafts the response. Decouples the way the information is sent from
	 * the creation of the diffuser and the response
	 * @param serializer The {@link Serializer} used to serialize/deserialize objects
	 * @param clientEndpoints The URI of the endpoints to which result requests can be sent
	 * @param containingClassName The class name of the returned type 
	 * @param containingClassName The name of the class containing the method to execute
	 * @param methodName The name of the method to execute
	 * @param argumentTypes The parameter types that form part of the method's signature
	 * @return A {@link Response} containing the link to the newly created diffuser.
	 */
	private String create( final Serializer serializer,
						   final List< URI > clientEndpoints,
						   final String returnTypeClassName,
						   final String containingClassName,
						   final String methodName,
						   final List< String > argumentTypes )
	{
		// create the diffuser
		final RestfulDiffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		
		// create the name/id for the diffuser
		final String key = DiffuserId.create( returnTypeClassName, containingClassName, methodName, argumentTypes );

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
								   request.getReturnTypeClass(),
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
	public Response getDiffuser( @Context final UriInfo uriInfo, @PathParam( SIGNATURE ) final String signature )
	{
		// create the URI to the newly created diffuser
		final URI diffuserUri = uriInfo.getAbsolutePathBuilder().build();

		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		if( diffusers.containsKey( signature ) )
		{
//			diffusers.remove( signature );
			
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
			final Feed feed = Atom.createFeed( diffuserUri, "Failed to retrieve RESTful diffuser: " + signature, date, uriInfo.getBaseUri() );

			response = Response.created( diffuserUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
		return response;
	}

	/**
	 * 
	 * @param uriInfo
	 * @param signature
	 * @return
	 */
	@GET @Path( "{" + SIGNATURE + "}" + "/{" + RESULT_ID + ": [a-zA-Z0-9\\-]*}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
//	@Produces( MediaType.APPLICATION_OCTET_STREAM )
	public Response getResult( @Context final UriInfo uriInfo, 
							   @PathParam( SIGNATURE ) final String signature,
							   @PathParam( RESULT_ID ) final String resultId )
	{
		// create the URI to the newly created diffuser
		final URI resultUri = uriInfo.getAbsolutePathBuilder().build();

		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		ResultCacheEntry result = null;
		if( ( result = getResultFromCache( signature, resultId ) ) != null )
		{
			Feed feed = null;
			try( final ByteArrayOutputStream output = new ByteArrayOutputStream() )
			{
				// serialize the result result to be used in the response.
				final Serializer serializer = SerializerFactory.getInstance().createSerializer( result.getSerializerType() );
				serializer.serialize( result.getResult(), output );
				
				// create the atom feed
				feed = Atom.createFeed( resultUri, "RESTful diffuser: " + signature, date, uriInfo.getBaseUri() );
				
				// create an entry for the feed and set the results as the content
				final Entry entry = Atom.createEntry();
				
				final ByteArrayInputStream input = new ByteArrayInputStream( output.toByteArray() );
				entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
				feed.addEntry( entry );
				
				// create the response
				response = Response.ok( output.toByteArray() )
								   .status( Status.OK )
								   .location( resultUri )
								   .entity( feed.toString() )
//								   .entity( output.toByteArray() )
								   .type( MediaType.APPLICATION_OCTET_STREAM )
								   .build();
			}
			catch( IOException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Error occured while attempting to close the byte array output stream for the serialized result result." );
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to retrieve result" + Constants.NEW_LINE );
			message.append( "  Signature: " + signature + Constants.NEW_LINE );
			message.append( "  Result ID: " + resultId );
			final Feed feed = Atom.createFeed( resultUri, message.toString(), date, uriInfo.getBaseUri() );

			response = Response.created( resultUri )
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
		final List< String > argumentTypes = diffuserId.getArgumentTypeNames();
		
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
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}

		// grab the serializer for used for the argument and the result 
		final Serializer serializer = request.getSerializer();

		// deserialize the arguments
		final List< ? super Object > arguments = new ArrayList<>();
		final List< byte[] > argumentValues = request.getArgumentValues();
		for( int i = 0; i < argumentValues.size(); ++i )
		{
			try
			{
				// create an input stream from the byte array
				final InputStream input = new ByteArrayInputStream( argumentValues.get( i ) );

				// create the Class result for the argument type (specified as a string)
				final Class< ? > clazz = Class.forName( argumentTypes.get( i ) );

				// deserialize and add to the list of value objects
				arguments.add( serializer.deserialize( input, clazz ) );
			}
			catch( ClassNotFoundException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Error occured while attempting to deserialize the method's arguments. The Class for the argument's type not found." + Constants.NEW_LINE );
				message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
				message.append( "  Argument Number: " + i + Constants.NEW_LINE );
				message.append( "  Argument Type: " + argumentTypes.get( i ) + Constants.NEW_LINE );
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
		}
	
		// deserialize the result, but first ensure that the class type for the result specified in
		// the request and the path signature are the same.
		final String objectType = request.getObjectType();
		if( !objectType.equals( diffuserId.getClassName() ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error occured while attempting to deserialize the result. The result's type specified in the request" + Constants.NEW_LINE );
			message.append( "does not match the result's type specified in the path signature." + Constants.NEW_LINE );
			message.append( "  Path Signature's Object Type: " + diffuserId.getClassName() + Constants.NEW_LINE );
			message.append( "  Request Object Type: " + request.getObjectType() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		Object deserializedObject = null;
		try
		{
			// create an input stream from the byte array
			final InputStream input = new ByteArrayInputStream( request.getObject() );

			// create the Class result for the argument type (specified as a string)
			final Class< ? > clazz = Class.forName( request.getObjectType() );

			// deserialize the result
			deserializedObject = serializer.deserialize( input, clazz );
		}
		catch( ClassNotFoundException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error occured while attempting to deserialize the result. The Class for the Object's type not found." + Constants.NEW_LINE );
			message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
			message.append( "  Object Type: " + request.getObjectType() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// call the diffused method using the diffuser with the matching signature
		final RestfulDiffuser diffuser = diffusers.get( signature );
		if( diffuser == null )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Could not find a RESTful diffuser with the specified key." + Constants.NEW_LINE );
			message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
			message.append( "  Available diffusers:" + Constants.NEW_LINE );
			for( String key : diffusers.keySet() )
			{
				message.append( "  " + key + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		final Object resultObject = diffuser.runObject( deserializedObject, diffuserId.getMethodName(), arguments.toArray( new Object[ 0 ] ) );
		
//		// serialize the result result to be used in the response.
//		try( final ByteArrayOutputStream output = new ByteArrayOutputStream() )
//		{
//			serializer.serialize( resultObject, output );
//		}
//		catch( IOException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Error occured while attempting to close the byte array output stream for the serialized result result." );
//			LOGGER.error( message.toString() );
//			throw new IllegalArgumentException( message.toString() );
//		}
//
		// grab the requstId
		final String requestId = request.getRequestId();

		// put the result into a map with the signature/id as the key
		final String resultID = addResults( signature, requestId, resultObject, serializer );
		
		// create the Atom link to the response
		final URI resultUri = uriInfo.getAbsolutePathBuilder().path( requestId ).build();

		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed and add an entry that holds the result ID
		final Feed feed = Atom.createFeed( resultUri, resultID, date, uriInfo.getBaseUri() );
		final Entry entry = Atom.createEntry( resultUri, RESULT_ID, date );
		entry.setContent( resultID );
		feed.addEntry( entry );

		// create the response
		final Response response = Response.created( resultUri )
				  .status( Status.OK )
				  .location( resultUri )
				  .entity( feed.toString() )
				  .type( MediaType.APPLICATION_ATOM_XML )
				  .build();
		
		return response;
	}
	
	// TODO have to add the result representation...probably this will only work 
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
//			// grab the class name and the method name and create instantiate the result
//			try
//			{
//				final String className = diffuserId.getClassName();
//				final Class< ? > clazz = Class.forName( className );		// can also specify the class loader, which we may have to do
//				final Object result = clazz.newInstance();		// TODO need to reconstruct the class from the serialized version here
//				
//				// grab the diffuser based on the signature
//				final RestfulDiffuser diffuser = diffusers.get( signature );
//				final Object result = diffuser.runObject( result, diffuserId.getMethodName(), argumentValues.toArray( new Object[ 0 ] ) );
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

	private static class ResultCacheEntry {
		
		private final Object result;
		private final String serializerType;
		
		public ResultCacheEntry( final Object result, final String serializerType )
		{
			this.result = result;
			this.serializerType = serializerType;
		}
		
		public ResultCacheEntry( final Object result, final Serializer serializer )
		{
			this( result, SerializerFactory.getSerializerName( serializer.getClass() ) );
		}
		
		public String getSerializerType()
		{
			return serializerType;
		}
		
		public Object getResult()
		{
			return result;
		}
	}
}
