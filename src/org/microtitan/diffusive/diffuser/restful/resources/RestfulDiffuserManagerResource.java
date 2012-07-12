package org.microtitan.diffusive.diffuser.restful.resources;

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
import javax.ws.rs.HEAD;
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
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserManagerClient;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.diffuser.restful.request.CreateDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.request.ExecuteDiffuserRequest;
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
@Path( RestfulDiffuserManagerResource.DIFFUSER_PATH )
public class RestfulDiffuserManagerResource {

	static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );

	// the maximum number of resultsCache cached
	private static final int MAX_RESULTS_CACHED = 100;
	
	public static final String DIFFUSER_PATH = "/diffusers";
	
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
	public static final String REQUEST_ID = "request_id";
	
	private final Map< String, RestfulDiffuser > diffusers;
	
	// fields to manage the resultsCache cache
	private final Map< String, ResultCacheEntry > resultsCache;
	private int maxResultsCached;
	
	private final Map< ResultId, Thread > runningTasks;

	/**
	 * Constructs the basic diffuser manager resource that allows clients to interact with the
	 * diffuser created through this resource. 
	 */
	public RestfulDiffuserManagerResource()
	{
		diffusers = new HashMap<>();
		resultsCache = new LinkedHashMap<>();
		maxResultsCached = MAX_RESULTS_CACHED;
		runningTasks = new LinkedHashMap<>();
	}
	
	/**
	 * Adds a result to the resultsCache cache. If the addition of this result causes the
	 * number of cached items to increase beyond the maximum allowable items, then the
	 * item that was first added is removed.
	 * @param key The key for the cache
	 */
	private void addResults( final ResultId key, final Object result, final Serializer serializer )
	{
		// put the new result to the cache
		final Object previousResults = resultsCache.put( key.getResultId(), new ResultCacheEntry( result, serializer ) );
		if( previousResults == null && resultsCache.size() > maxResultsCached )
		{
			final Iterator< Map.Entry< String, ResultCacheEntry > > iter = resultsCache.entrySet().iterator();
			resultsCache.remove( iter.next().getKey() );
		}
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
		return ResultId.create( signature, requestId );
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
		final String key = DiffuserId.createId( returnTypeClassName, containingClassName, methodName, argumentTypes );

		// add the diffuser to the map of diffusers
		/*final RestfulDiffuser oldDiffuser = */diffusers.put( key, diffuser );

		return key;
	}
	
	/**
	 * Creates a {@link RestfulDiffuser} using the information specified in the {@link CreateDiffuserRequest}
	 * object. The basic information needed by the diffusers is:
	 * <ul>
	 * 	<li>The {@link Serializer} used to serialize (marshal) and deserialize (unmarshal) the objects that
	 * 		get flung across the network.</li>
	 * 	<li>The end-points containing {@link RestfulDiffuser}s to which the new {@link RestfulDiffuser} can 
	 * 		diffuse method calls.</li>
	 * 	<li>The return type</li>
	 * 	<li>The {@link Class} containing the diffused method.</li>
	 * 	<li>The name of the diffused method</li>
	 * 	<li>A list of argument types that represent the method's formal parameters.</li>
	 * </ul>
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param request The request object containing the information needed to create a {@link RestfulDiffuser}.
	 * @return A response containing and Atom feed with information about the newly created diffuser. Specifically,
	 * it contains the URI of the newly created diffuser, the key, date created, and the URI representing the
	 * URI that resolved to this create method.
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
		final Feed feed = Atom.createFeed( diffuserUri, key, date, uriInfo.getBaseUri() );
		
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
	 * Returns information about the diffuser represented by the specified signature.
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param signature The signature of the {@link RestfulDiffuser} corresponding to a specific method.
	 * The signatures are created using the {@link DiffuserId} class.
	 * @return A response containing the signature, and the URI of the diffuser with the specified signature.
	 * @see DiffuserId
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
			// create the atom feed
			final Feed feed = Atom.createFeed( diffuserUri, signature, date, uriInfo.getBaseUri() );
			
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
			final Feed feed = Atom.createFeed( diffuserUri, signature, date, uriInfo.getBaseUri() );

			response = Response.created( diffuserUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
		return response;
	}

	/**
	 * Executes the diffuser associated with the specified signature ({@link DiffuserId}) using the
	 * information specified in the {@link ExecuteDiffuserRequest}, which holds the following information:
	 * <ul>
	 * 	<li>A list of class names representing the types of the formal parameters passed to the method.</li>
	 * 	<li>A list of {@code byte[]} representing the serialized parameter objects.</li>
	 * 	<li>The class name of the object containing the method to execute.</li>
	 * 	<li>A {@code byte[]} representing the serialized object containing the method to call.</li>
	 * 	<li>The name of the serializer used to serialize and deserialize (see the enum 
	 * 		{@link SerializerFactory.SerializerType}</li>
	 * </ul>
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param signature The signature of the {@link RestfulDiffuser} corresponding to a specific method.
	 * The signatures are created using the {@link DiffuserId} class.
	 * @param request The {@link ExecuteDiffuserRequest} holding the serialized object and method parameters,
	 * the type information, and the {@link Serializer} name. 
	 * @return A {@link Response} containing a string version of an Atom feed that holds the result ID and
	 * a link to the URI representing the result.
	 */
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
	
		// deserialize the object that contains the method to be called, but first ensure that the class type for 
		// object the specified in the request and the path signature are the same.
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
		
//		Object deserializedObject = null;
//		try
//		{
//			// create an input stream from the byte array
//			final InputStream input = new ByteArrayInputStream( request.getObject() );
//
//			// create the Class result for the argument type (specified as a string)
//			final Class< ? > clazz = Class.forName( request.getObjectType() );
//
//			// deserialize the result
//			deserializedObject = serializer.deserialize( input, clazz );
//		}
//		catch( ClassNotFoundException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Error occured while attempting to deserialize the result. The Class for the Object's type not found." + Constants.NEW_LINE );
//			message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
//			message.append( "  Object Type: " + request.getObjectType() + Constants.NEW_LINE );
//			LOGGER.error( message.toString() );
//			throw new IllegalArgumentException( message.toString() );
//		}
		final Object deserializedObject = deserialize( request, signature );
		
		//
		// call the diffused method using the diffuser with the matching signature
		//
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

		// grab the requstId and use it to create the result ID
		final String requestId = request.getRequestId();
		final ResultId resultId = new ResultId( signature, requestId );

		// throw the running of the object onto another thread, which will add result object to the
		// cache when it completes.
		final Thread task = new Thread( new Runnable() {
			
			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				final Class< ? > returnType = request.getReturnTypeClass();
				final Object resultObject = diffuser.runObject( true, returnType, deserializedObject, diffuserId.getMethodName(), arguments.toArray( new Object[ 0 ] ) );
				
				// put the result into a map with the signature/id as the key
				addResults( resultId, resultObject, serializer );
				runningTasks.remove( resultId );
			}
		} );

		// add the task to the map of running tasks and set it to run
		synchronized( this )
		{
			runningTasks.put( resultId, task );
			task.start();
		}
		
		//
		// create the response
		//
		// create the Atom link to the response
		final URI resultUri = uriInfo.getAbsolutePathBuilder().path( requestId ).build();

		// grab the date for time stamp
		final Date date = new Date();
		
		// create the atom feed and add an entry that holds the result ID and the request ID
		final Feed feed = Atom.createFeed( resultUri, resultId.getResultId(), date, uriInfo.getBaseUri() );
		
		final Entry resultIdEntry = Atom.createEntry( resultUri, RESULT_ID, date );
		resultIdEntry.setContent( resultId.getResultId() );
		feed.addEntry( resultIdEntry );
		
		final Entry requestIdEntry = Atom.createEntry( resultUri, REQUEST_ID, date );
		requestIdEntry.setContent( requestId );
		feed.addEntry( requestIdEntry );

		// create the response
		final Response response = Response.created( resultUri )
				  .status( Status.OK )
				  .location( resultUri )
				  .entity( feed.toString() )
				  .type( MediaType.APPLICATION_ATOM_XML )
				  .build();
		
		return response;
	}
	
	private synchronized boolean isRunning( final String signature, final String requestId )
	{
		return runningTasks.containsKey( new ResultId( signature, requestId ) );
	}
	
	/**
	 * 
	 * @param request
	 * @param signature
	 * @return
	 */
	private Object deserialize( final ExecuteDiffuserRequest request, final String signature )
	{
		Object deserializedObject = null;
		try
		{
			// create an input stream from the byte array
			final InputStream input = new ByteArrayInputStream( request.getObject() );

			// create the Class result for the argument type (specified as a string)
			final Class< ? > clazz = Class.forName( request.getObjectType() );

			// deserialize the result
			deserializedObject = request.getSerializer().deserialize( input, clazz );
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
		
		return deserializedObject;
	}
	
	@HEAD @Path( "{" + SIGNATURE + "}" + "/{" + RESULT_ID + ": [a-zA-Z0-9\\-]*}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response getResultStatus( @Context final UriInfo uriInfo, 
							   		 @PathParam( SIGNATURE ) final String signature,
							   		 @PathParam( RESULT_ID ) final String resultId )
	{
		// if the result is in the results cache, then we have completed and we 
		// return an OK status, otherwise, we haven't completed the runObject(...)
		// method and we return a NO_CONTENT status.
		Response response = null;
		if( resultsCache.containsKey( createResultsCacheId( signature, resultId ) ) )
		{
			// create the response
			response = Response.ok().build();
		}
		else
		{
			response = Response.noContent().build();
		}
		return response;
	}


	/**
	 * Returns the result of {@link #execute(UriInfo, String, ExecuteDiffuserRequest)} method call against a 
	 * specific diffuser. The result is referenced through the result ID which was generated and passed back 
	 * to the client when the {@link #execute(UriInfo, String, ExecuteDiffuserRequest)} method was called. The
	 * result ID is created in the {@link #createResultsCacheId(String, String)} based on the signature and
	 * the request ID.
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param signature The signature of the {@link RestfulDiffuser} corresponding to a specific method.
	 * The signatures are created using the {@link DiffuserId} class.
	 * @param requestId The result ID corresponding to the result.
	 * @return An {@link Response} object that contains a string version of the Atom feed holding the result.
	 * The {@code content} of the Atom feed contains the {@code byte[]} version of the serialized result object. 
	 */
	@GET @Path( "{" + SIGNATURE + "}" + "/{" + RESULT_ID + ": [a-zA-Z0-9\\-]*}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response getResult( @Context final UriInfo uriInfo, 
							   @PathParam( SIGNATURE ) final String signature,
							   @PathParam( RESULT_ID ) final String requestId )
	{
		// create the URI to the newly created diffuser
		final URI resultUri = uriInfo.getAbsolutePathBuilder().build();

		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		ResultCacheEntry result = null;
		if( ( result = getResultFromCache( signature, requestId ) ) != null )
		{
			Feed feed = null;
			try( final ByteArrayOutputStream output = new ByteArrayOutputStream() )
			{
				// serialize the result result to be used in the response.
				final Serializer serializer = SerializerFactory.getInstance().createSerializer( result.getSerializerType() );
				serializer.serialize( result.getResult(), output );
				
				// create the atom feed
				final String resultKey = createResultsCacheId( signature, requestId );
				feed = Atom.createFeed( resultUri, resultKey, date, uriInfo.getBaseUri() );
				
				// create an entry for the feed and set the results as the content
				final Entry entry = Atom.createEntry();
				
				final ByteArrayInputStream input = new ByteArrayInputStream( output.toByteArray() );
				entry.setId( requestId );
				entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
				feed.addEntry( entry );
				
				// create the response
				response = Response.ok( output.toByteArray() )
								   .status( Status.OK )
								   .location( resultUri )
								   .entity( feed.toString() )
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
		else if( isRunning( signature, requestId ) )
		{
			response = Response.noContent().build();
		}
		else
		{
			final String resultKey = createResultsCacheId( signature, requestId );
			final Feed feed = Atom.createFeed( resultUri, resultKey, date, uriInfo.getBaseUri() );

			final Entry entry = Atom.createEntry();
			entry.setId( requestId );
			entry.setContent( "Failded to retrieve result.", MediaType.TEXT_PLAIN );
			feed.addEntry( entry );
			
			response = Response.created( resultUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
		return response;
	}

	/**
	 * Returns an Atom feed as a string, whose entries each represent a registered diffuser
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @return an Atom feed as a string, whose entries each represent a registered diffuser
	 */
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
		final Feed feed = Atom.createFeed( baseUri, "get-diffuser-list", date, baseUri );

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
	
	/**
	 * Deletes the diffuser at the specified URI
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param signature The signature of the {@link RestfulDiffuser} corresponding to a specific method.
	 * The signatures are created using the {@link DiffuserId} class.
	 * @return A responses that holds the URI of the diffuser that was deleted.
	 */
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
			final Feed feed = Atom.createFeed( diffuserUri, "delete-diffuser", date );
			
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
			final Feed feed = Atom.createFeed( diffuserUri, "error-delete-diffuser", date );

			// create the error response
			response = Response.created( diffuserUri )
							   .status( Status.BAD_REQUEST )
							   .entity( feed.toString() )
							   .build();
		}
		return response;
	}
	
	/**
	 * The entry into the results cache. Each entry holds the results object and the serializer
	 * name used for serializing and deserializing the result object.
	 *  
	 * @author Robert Philipp
	 */
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
