package org.microtitan.diffusive.diffuser.restful.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.microtitan.diffusive.classloaders.RestfulClassLoader;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.diffuser.restful.client.RestfulDiffuserManagerClient;
import org.microtitan.diffusive.diffuser.restful.request.CreateDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.request.ExecuteDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.resources.cache.FifoResultsCache;
import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultCacheEntry;
import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache;
import org.microtitan.diffusive.diffuser.restful.server.KeyedDiffusiveStrategyRepository;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoad;
import org.microtitan.diffusive.diffuser.strategy.load.TaskCpuLoad;
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

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );

	// the maximum number of resultsCache cached
//	private static final int MAX_RESULTS_CACHED = 100;
	
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
	
	private final Map< String, DiffuserEntry > diffusers;
	
	// fields to manage the resultsCache cache
	private final ResultsCache resultsCache;
	
	private final ExecutorService executor;
//	private static final int THREAD_POOL_THREADS = 100;
	
	// the strategy that is applied to diffusers created by this resource.
	// recall that the strategy determines the order and number of times an
	// end-point is called.
	private final DiffuserStrategy diffuserStrategy;
	
	// the load calc is ultimately used by the DiffuserStrategy to determine whether to run
	// locally, or to diffuse the request forward to one of its end-points
	private final DiffuserLoad loadCalc;
	private final double loadThreshold;

	/**
	 * Constructs the basic diffuser manager resource that allows clients to interact with the
	 * diffuser created through this resource. 
	 * @param The executor service to which tasks are submitted
	 */
	public RestfulDiffuserManagerResource( final ExecutorService executor, 
										   final ResultsCache resultsCache,
										   final DiffuserLoad loadCalc )
	{
		this.executor = executor;
		
		this.diffusers = new HashMap<>();
		this.resultsCache = resultsCache;
		this.loadCalc = loadCalc;
		
		this.diffuserStrategy = KeyedDiffusiveStrategyRepository.getInstance().getStrategy();
		this.loadThreshold = KeyedDiffusiveStrategyRepository.getInstance().getLoadThreshold();
	}
	
	public static final ExecutorService createExecutorService( final int numThreads )
	{
		return Executors.newFixedThreadPool( numThreads );
	}
	
	public static final ResultsCache createResultsCache( final int maxResultsCached )
	{
		return new FifoResultsCache( maxResultsCached );
	}
	
	public static final DiffuserLoad createLoadCalc( final ResultsCache cache )
	{
		return new TaskCpuLoad( cache );
	}

//	/**
//	 * Constructs the basic diffuser manager resource that allows clients to interact with the
//	 * diffuser created through this resource. 
//	 * @param The executor service to which tasks are submitted
//	 */
//	public RestfulDiffuserManagerResource( final ExecutorService executor )
//	{
//		this( executor, new FifoResultsCache( MAX_RESULTS_CACHED ) );
//	}
//	
//	/**
//	 * Constructs the basic diffuser manager resource that allows clients to interact with the
//	 * diffuser created through this resource. 
//	 * @param numThreads The number of threads for the default fixed thread pool
//	 */
//	public RestfulDiffuserManagerResource( final int numThreads )
//	{
//		this( Executors.newFixedThreadPool( numThreads ) );
//	}
//	
//	/**
//	 * Constructs the basic diffuser manager resource that allows clients to interact with the
//	 * diffuser created through this resource. 
//	 */
//	public RestfulDiffuserManagerResource()
//	{
//		this( THREAD_POOL_THREADS );
//	}
	
	/*
	 * Creates the diffuser and crafts the response. Decouples the way the information is sent from
	 * the creation of the diffuser and the response
	 * @param serializer The {@link Serializer} used to serialize/deserialize objects
	 * @param clientEndpoints The URI of the endpoints to which result requests can be sent
	 * @param containingClassName The class name of the returned type 
	 * @param containingClassName The name of the class containing the method to execute
	 * @param methodName The name of the method to execute
	 * @param argumentTypes The parameter types that form part of the method's signature
	 * @return The diffuser ID (signature) of the diffusive method associated with the newly created diffuser.
	 */
	private String create( final Serializer serializer,
						   final List< URI > clientEndpoints,
						   final List< URI > classPaths,
						   final String returnTypeClassName,
						   final String containingClassName,
						   final String methodName,
						   final List< String > argumentTypes )
	{
		// create the diffuser
		final RestfulDiffuser diffuser = new RestfulDiffuser( serializer, diffuserStrategy, classPaths, loadThreshold );
		
		// create the name/id for the diffuser
		final String key = DiffuserId.createId( returnTypeClassName, containingClassName, methodName, argumentTypes );

		// add the diffuser to the map of diffusers
		diffusers.put( key, new DiffuserEntry( diffuser, classPaths ) );

		return key;
	}

	/**
	 * Creates a {@link RestfulDiffuser} using the information specified in the {@link CreateDiffuserRequest}
	 * object. The basic information needed by the diffusers is:
	 * <ul>
	 * 	<li>The {@link Serializer} used to serialize (marshal) and deserialize (unmarshal) the objects that
	 * 		get flung across the network.</li>
	 * 	<li>Any additional end-points containing {@link RestfulDiffuser}s to which the new {@link RestfulDiffuser}
	 * 		can diffuse method calls. The base endpoints are specified in the configuration.</li>
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
								   request.getClassPathsUri(),
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
	 * This is a non-blocking method. A reference to the result is placed in the results cache, and the status
	 * of the execution can be monitored with the {@link #isRunning(String, String)} method. The results can be
	 * obtained from the blocking {@link #getResult(UriInfo, String, String)} method.
	 * 
	 * @param uriInfo Information about the request URI and the JAX-RS application.
	 * @param signature The signature of the {@link RestfulDiffuser} corresponding to a specific method.
	 * The signatures are created using the {@link DiffuserId} class.
	 * @param request The {@link ExecuteDiffuserRequest} holding the serialized object and method parameters,
	 * the type information, and the {@link Serializer} name. 
	 * @return A {@link Response} containing a string version of an Atom feed that holds the result ID and
	 * a link to the URI representing the result.
	 * @see #isRunning(String, String)
	 * @see #getResult(UriInfo, String, String)
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
			message.append( "  Signature: " + signature + Constants.NEW_LINE );
			message.append( "  Argument types based on DiffuserId (signature)" + Constants.NEW_LINE );
			for( int i = 0; i < argumentTypes.size(); ++i )
			{
				message.append( "    " + argumentTypes.get( i ) + Constants.NEW_LINE );
			}
			message.append( "  Argument types based on the execute diffuser request" + Constants.NEW_LINE );
			for( int i = 0; i < requestArgTypes.size(); ++i )
			{
				message.append( "    " + requestArgTypes.get( i ) + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// ensure that the diffused method's return type are the same in the request and the signature
		if( !request.getReturnType().equals( diffuserId.getReturnTypeClassName() ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error: diffused method's return type from the signature doesn't match the return type from the request" + Constants.NEW_LINE );
			message.append( "  " + ExecuteDiffuserRequest.class.getSimpleName() + "\'s return type: " + request.getReturnType() + Constants.NEW_LINE );
			message.append( "  Signature's return type: " + diffuserId.getReturnTypeClassName() );
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
			try( final InputStream input = new ByteArrayInputStream( argumentValues.get( i ) ) )
			{
				// create the Class result for the argument type (specified as a string)
				final Class< ? > clazz = getClass( argumentTypes.get( i ), signature );

				// deserialize and add to the list of value objects
				arguments.add( serializer.deserialize( input, clazz ) );
			}
			catch( IOException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Error closing the ByteArrayInputStream for argument: " + i + Constants.NEW_LINE );
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
		final Class< ? > signatureDerivedClass = getClass( request.getObjectType(), signature );
		final Object deserializedObject = deserialize( request, signatureDerivedClass );
		
		//
		// call the diffused method using the diffuser with the matching signature
		//
		final DiffuserEntry diffuserEntry = diffusers.get( signature );
		if( diffuserEntry == null )
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
		final RestfulDiffuser diffuser = diffuserEntry.getDiffuser();

		// grab the requstId and use it to create the result ID
		final String requestId = request.getRequestId();
		final ResultId resultId = new ResultId( signature, requestId );
		
		// create the task that will be submitted to the executor service to run
		// TODO add the DiffuserLoad (loadCalc) to the constructor so that it can be passed to the diffuser.
		final DiffuserTask task = new DiffuserTask( diffuserId.getMethodName(), 
													arguments, 
													diffuserId.getReturnTypeClazz(), 
													deserializedObject, 
													diffuser,
													loadCalc );
		
		// submit the task to the executor service to run on a different thread,
		// and put the future result into the results cache with the signature/id as the key
		final Future< Object > future = executor.submit( task );
		resultsCache.add( createResultsCacheId( resultId ), new ResultCacheEntry< Object >( future, serializer ) );
		
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
	
	/**
	 * Returns the {@link Class} for the specified name. The signature comes along for the ride, in case
	 * there is a problem loading the {@link Class} of the specified name.
	 * @param classname The name of the class to load
	 * @param signature The signature of the diffusive method in case the {@link Class} of the specified
	 * name can't be loaded
	 * @return The {@link Class} associated with the specified class name.
	 * @throws IllegalArgumentException if the class of the specified name can't be found
	 */
	private Class< ? > getClass( final String classname, final String signature )
	{
		// attempt to get the class for the specified class name, and if that fails, create and use
		// a URL class loader with the diffuser's specific class paths, and whose parent class loader
		// is the same class loader as loaded this class.
		Class< ? > clazz = null;
        try
        {
	        clazz = Class.forName( classname );
        }
		catch( ClassNotFoundException e )
		{
			// grab the diffuser entry associated with the specified signature, and if an
			// entry exists, then we can grab the class path URI list from it and use it
			// to construct a new URL class loader
			final DiffuserEntry entry = diffusers.get( signature );
			if( entry != null )
			{
				// grab the list of class path URI. if the list is empty or doesn't exist, then
				// there is no further place to look, and so we punt.
				final List< URI > classPaths = entry.getClassPaths();
				if( classPaths != null && !classPaths.isEmpty() )
				{
					// set up the RESTful class loader and attempt to load the class from the remote server 
					// listed in the class paths URI list
					final RestfulClassLoader loader = new RestfulClassLoader( classPaths );
					try
					{
						clazz = Class.forName( classname, true, loader );//loader.loadClass( classname );
					}
					catch( ClassNotFoundException e1 )
					{
						final StringBuffer message = new StringBuffer();
						message.append( "Error loading class:" + Constants.NEW_LINE );
						message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
						message.append( "  Class Name: " + classname + Constants.NEW_LINE );
						message.append( "  Class Loader: " + loader.getClass().getName() );
						LOGGER.error( message.toString(), e1 );
						throw new IllegalArgumentException( message.toString(), e1 );
					}
					
					if( LOGGER.isDebugEnabled() )
					{
						final StringBuffer message = new StringBuffer();
						message.append( "Loaded class:" + Constants.NEW_LINE );
						message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
						message.append( "  Class Name: " + classname + Constants.NEW_LINE );
						message.append( "  Class Loader: " + loader.getClass().getName() );
						LOGGER.debug( message.toString() );
					}
				}
			}
			else
			{
    			final StringBuffer message = new StringBuffer();
    			message.append( "Error occured while attempting to deserialize the method's arguments. The Class for the argument's type not found." + Constants.NEW_LINE );
    			message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
    			message.append( "  Class Name: " + classname );
    			LOGGER.error( message.toString() );
    			throw new IllegalArgumentException( message.toString() );
			}
		}
		return clazz;
	}
	
//	/**
//	 * Deserializes the object in the specified execute diffuser request and returns it. Uses the 
//	 * serializer and the class type specified in the request.
//	 * @param request The execute diffuser request that holds the object
//	 * @param signature The signature of the diffused method
//	 * @return The object deserialized from the specified request
//	 */
//	private Object deserialize( final ExecuteDiffuserRequest request, final String signature )
//	{
//		Object deserializedObject = null;
//		try( final InputStream input = new ByteArrayInputStream( request.getObject() ) )
//		{
//			// create the Class result for the argument type (specified as a string)
//			final Class< ? > clazz = getClass( request.getObjectType(), signature );
//
//			// deserialize the result
//			deserializedObject = request.getSerializer().deserialize( input, clazz );
//		}
//		catch( IOException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Error closing the ByteArrayInputStream for the result." + Constants.NEW_LINE );
//			message.append( "  Signature (Key): " + signature + Constants.NEW_LINE );
//			message.append( "  Object Type: " + request.getObjectType() + Constants.NEW_LINE );
//			LOGGER.error( message.toString() );
//			throw new IllegalArgumentException( message.toString() );
//		}
//		
//		return deserializedObject;
//	}
	
	/**
	 * Deserializes the object in the specified execute diffuser request and returns it. Uses the 
	 * serializer and the class type specified in the request.
	 * @param request The execute diffuser request that holds the object
	 * @param signature The signature of the diffused method
	 * @return The object deserialized from the specified request
	 */
	private < T > T deserialize( final ExecuteDiffuserRequest request, final Class< T > clazz )
	{
		T deserializedObject = null;
		try( final InputStream input = new ByteArrayInputStream( request.getObject() ) )
		{
			// deserialize the result
			deserializedObject = request.getSerializer().deserialize( input, clazz );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error closing the ByteArrayInputStream for the result." + Constants.NEW_LINE );
			message.append( "  Class Type: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Object Type: " + request.getObjectType() + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		return deserializedObject;
	}
	/**
	 * Returns the status for the task for the specified result ID and signature
	 * @param signature The signature of the diffused method
	 * @param resultId The ID associated with the result
	 * @return The status of the result. Returns "ok" if the result is done; "no content" otherwise
	 */
	@HEAD @Path( "{" + SIGNATURE + "}" + "/{" + RESULT_ID + ": [a-zA-Z0-9\\-]*}" )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public Response getResultStatus( @PathParam( SIGNATURE ) final String signature,
							   		 @PathParam( RESULT_ID ) final String resultId )
	{
		// if the result is in the results cache, then we have completed and we 
		// return an OK status, otherwise, we haven't completed the runObject(...)
		// method and we return a NO_CONTENT status.
		Response response = null;
		if( resultsCache.isCached( createResultsCacheId( signature, resultId ) ) )
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
		ResultCacheEntry< Object > result = null;
		final String cacheKey = createResultsCacheId( signature, requestId );
		if( ( result = resultsCache.get( cacheKey ) ) != null )
		{
			Feed feed = null;
			try( final ByteArrayOutputStream output = new ByteArrayOutputStream() )
			{
				// serialize the result result to be used in the response (blocks until the result is done)
				final Serializer serializer = SerializerFactory.getInstance().createSerializer( result.getSerializerType() );
				final Object object = result.getResult();	// blocking call
				serializer.serialize( object, output );
				
				// create the atom feed
				feed = Atom.createFeed( resultUri, cacheKey, date, uriInfo.getBaseUri() );
				
				// create an entry for the feed and set the results as the content
				final Entry entry = Atom.createEntry();
				
				final ByteArrayInputStream input = new ByteArrayInputStream( output.toByteArray() );
				entry.setId( requestId );
				entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
				feed.addEntry( entry );
				
				// create the response
				response = Response.ok()
								   .location( resultUri )
								   .entity( feed.toString() )
								   .type( MediaType.APPLICATION_ATOM_XML )
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
		else if( resultsCache.isRunning( cacheKey ) )
		{
			response = Response.noContent().build();
		}
		else
		{
			final Feed feed = Atom.createFeed( resultUri, cacheKey, date, uriInfo.getBaseUri() );

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
		for( String key : diffusers.keySet() )
		{
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
	 * Creates the results cache ID used as the key into the {@link #resultsCache}.
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return The key for the {@link #resultsCache} {@link Map}.
	 */
	private static final String createResultsCacheId( final String signature, final String requestId )
	{
		return ResultId.create( signature, requestId );
	}
	
	/**
	 * Constructs the results cache ID used as the key into the {@link #resultsCache}. Returns the same
	 * value as a call to:<p>
	 * {@code ResultId.create( resultId.getSignature(), resultId.getRequestId() )}
	 * @param resultId The {@link ResultId} object
	 * @return The key into the results cache.
	 */
	private static final String createResultsCacheId( final ResultId resultId )
	{
		return resultId.getResultId();
	}

	/**
	 * An entry used by the map of diffusers that contains the {@link Diffuser} and the list of class path end-points.
	 *  
	 * @author Robert Philipp
	 */
	private static class DiffuserEntry {
		
		private final RestfulDiffuser diffuser;
		private final List< URI > classPaths;

		/**
		 * Constructs an entry containing the {@link Diffuser} and the list of class path end-points
		 * @param diffuser The {@link Diffuser}
		 * @param classPaths The list of class path endpoints
		 */
		public DiffuserEntry( final RestfulDiffuser diffuser, final List< URI > classPaths )
		{
			this.diffuser = diffuser;
			this.classPaths = classPaths;
		}
		
		/**
		 * @return the diffuser associated with this entry
		 */
		public RestfulDiffuser getDiffuser()
		{
			return diffuser;
		}
		
		/**
		 * @return the list if {@link URI} representing the class path end-points
		 */
		public List< URI > getClassPaths()
        {
        	return classPaths;
        }
	}
	
	/**
	 * {@link Callable} task that can be submitted to the {@link ExecutorService} to run.
	 * 
	 * @author Robert Philipp
	 */
	private static class DiffuserTask implements Callable< Object > {
		
		private final Class< ? > returnType;
		private final Object deserializedObject;
		private final Diffuser diffuser;
		private final String methodName;
		private final Object[] arguments;
		private final DiffuserLoad loadCalc;
		
		/**
		 * Constructs a {@link Callable} task for the {@link ExecutorService}
		 * @param methodName The name of the method to call
		 * @param arguments The arguments/parameters passed to the method
		 * @param returnType The return type of the method call
		 * @param deserializedObject The deserialized object that holds the state
		 * @param diffuser The diffuser that is used to run/diffuser the method call
		 */
		public DiffuserTask( final String methodName,
							 final List< ? super Object > arguments,
							 final Class< ? > returnType,
							 final Object deserializedObject,
							 final Diffuser diffuser,
							 final DiffuserLoad loadCalc )
		{
			this.returnType = returnType;
			this.deserializedObject = deserializedObject;
			this.diffuser = diffuser;
			this.methodName = methodName;
			this.arguments = arguments.toArray( new Object[ 0 ] );
			this.loadCalc = loadCalc;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Object call()
		{
//			return diffuser.runObject( true, returnType, deserializedObject, methodName, arguments );
			return diffuser.runObject( loadCalc.getLoad(), returnType, deserializedObject, methodName, arguments );
		}
	}
}
