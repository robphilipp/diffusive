package org.microtitan.diffusive.diffuser.restful.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.microtitan.diffusive.annotations.DiffusiveServerConfiguration;
import org.microtitan.diffusive.classloaders.RestfulClassLoader;
import org.microtitan.diffusive.classloaders.RestfulDiffuserClassLoader;
import org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory;
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
import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoadCalc;
import org.microtitan.diffusive.diffuser.strategy.load.TaskCpuLoadCalc;
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

	// the executor service holds the thread pool for managing concurrent diffusions
	private final ExecutorService executor;
	
	// the strategy that is applied to diffusers created by this resource.
	// recall that the strategy determines the order and number of times an
	// end-point is called.
	private final DiffuserStrategy diffuserStrategy;
	
	// the load calc is ultimately used by the DiffuserStrategy to determine whether to run
	// locally, or to diffuse the request forward to one of its end-points
	private final DiffuserLoadCalc loadCalc;
	private final double loadThreshold;
	
	private final ClassLoaderFactory classLoaderFactory;

	/**
	 * Constructs the basic diffuser manager resource that allows clients to interact with the
	 * diffuser created through this resource. 
	 * @param executor The executor service to which tasks are submitted
	 * @param resultsCache The cache holding the execution results in a {@link ResultCacheEntry}.
	 * @param loadCalc The calculator that is used to calculate the load on this machine, and is
	 * used to determine whether the diffuser will execute the task locally or diffuse it to a 
	 * remote diffuser.
	 * @param configurationClasses A {@link List} of fully qualified class names. Each class should
	 * have methods annotated with {@link DiffusiveServerConfiguration}, which are called and used
	 * to configure this resource. These annotated methods should be {@code static}.
	 * @param classLoaderFactory The factory for creating class loaders needed for the diffusers. This
	 * allows using different class loaders for the server. For example, {@link RestfulClassLoader}
	 * for non-nested diffusion or a {@link RestfulDiffuserClassLoader} for nested diffusion.
	 */
	public RestfulDiffuserManagerResource( final ExecutorService executor, 
										   final ResultsCache resultsCache,
										   final DiffuserLoadCalc loadCalc,
										   final List< String > configurationClasses,
										   final ClassLoaderFactory classLoaderFactory )
	{
		this.executor = executor;
		
		this.diffusers = new HashMap<>();
		this.resultsCache = resultsCache;
		this.loadCalc = loadCalc;
		
		// call the configuration classes used to configure this resource (strategy, load threshold)
		invokeConfigurationClasses( configurationClasses );
		
		// set the values based on the configuration
		this.diffuserStrategy = KeyedDiffusiveStrategyRepository.getInstance().getStrategy();
		this.loadThreshold = KeyedDiffusiveStrategyRepository.getInstance().getLoadThreshold();
		
		// set the class-loader factory (RESTful class loader with or without nested diffusion)
		this.classLoaderFactory = classLoaderFactory;
	}
	
	/**
	 * Creates a default {@link ExecutorService} (fixed thread pool) with the specified number
	 * of threads. The number of threads must be greater than 0.
	 * @param numThreads The number of threads (must be greater than 0)
	 * @return The newly create fixed thread pool {@link ExecutorService}.
	 */
	public static final ExecutorService createExecutorService( final int numThreads )
	{
		return Executors.newFixedThreadPool( numThreads );
	}
	
	/**
	 * Creates a default {@link ResultsCache} (a {@link FifoResultsCache}) with the specified
	 * maximum number of cached items.
	 * @param maxResultsCached The maximum number of cached items.
	 * @return a newly created {@link FifoResultsCache}
	 * @see FifoResultsCache
	 */
	public static final ResultsCache createResultsCache( final int maxResultsCached )
	{
		return new FifoResultsCache( maxResultsCached );
	}
	
	/**
	 * Creates a default {@link DiffuserLoadCalc} ({@link TaskCpuLoadCalc}) with the specified
	 * {@link ResultsCache} used to determine how many tasks are currently executing.
	 * @param cache The {@link ResultsCache} used to determine how many tasks are currently executing
	 * @return a newly created {@link TaskCpuLoadCalc}
	 * @see TaskCpuLoadCalc
	 */
	public static final DiffuserLoadCalc createLoadCalc( final ResultsCache cache )
	{
		return new TaskCpuLoadCalc( cache );
	}
	
	/**
	 * Creates the diffuser and crafts the response. Decouples the way the information is sent from
	 * the creation of the diffuser and the response
	 * @param serializer The {@link Serializer} used to serialize/deserialize objects
	 * @param clientEndpoints The URI of the end-points to which result requests can be sent
	 * @param classPaths The URI of the end-points which can act as a class path. In other words,
	 * these end-points can return a {@link Class} object.
	 * @param returnTypeClassName The class name of the returned type 
	 * @param containingClassName The name of the {@link Class} containing the method to execute
	 * @param methodName The name of the method to execute
	 * @param argumentTypes The parameter types (fully qualified class names) that form part of the method's signature
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
		final ClassLoader classLoader = classLoaderFactory.create( classPaths );
		diffusers.put( key, new DiffuserEntry( diffuser, classLoader ) );
//		diffusers.put( key, new DiffuserEntry( diffuser, classPaths ) );

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
				final ClassLoader loader = entry.getClassLoader();
				if( loader != null )
				{
					// set up the RESTful class loader and attempt to load the class from the remote server 
					// listed in the class paths URI list
					try
					{
						clazz = Class.forName( classname, true, loader );
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
	 * Invokes the methods of the classes specified in the {@link #configurationClasses} list
	 * that are annotated with @{@link DiffusiveServerConfiguration}.
	 *  
	 * @throws Throwable
	 */
	private static void invokeConfigurationClasses( final List< String > configurationClasses )
	{
		// run through the class names, load the classes, and then invoke the configuration methods
		// (that have been annotated with @DiffusiveConfiguration)
		for( String className : configurationClasses )
		{
			Method configurationMethod = null;
			try
			{
				// attempt to load the class...if it isn't found, then a warning will be issued in
				// the class not found exception, and the loop will continue to attempt to load any
				// other configuration classes.
				final Class< ? > setupClazz = RestfulDiffuserServer.class.getClassLoader().loadClass( className );
				
				// grab the methods that have an annotation @DiffusiveServerConfiguration and invoke them
				for( final Method method : setupClazz.getMethods() )
				{
					if( method.isAnnotationPresent( DiffusiveServerConfiguration.class ) )
					{
						// hold on the the method in case there is an invocation exception
						// and to warn the user if no configuration method was found
						configurationMethod = method;
						method.invoke( null/*setupClazz.newInstance()*/ );
					}
				}
				if( configurationMethod == null )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Error finding a method annotated with @Configure" + Constants.NEW_LINE );
					message.append( "  Configuration Class: " + className + Constants.NEW_LINE );
					LOGGER.warn( message.toString() );
				}
			}
			catch( InvocationTargetException | IllegalAccessException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Error invoking target method." + Constants.NEW_LINE );
				message.append( "  Class Name: " + className + Constants.NEW_LINE );
				message.append( "  Method Name: " + configurationMethod.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
			catch( ClassNotFoundException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Unable to load the configuration class. " + RestfulDiffuserServer.class.getName() );
				message.append( " may not have been configured properly." + Constants.NEW_LINE );
				message.append( "  Configuration Class: " + className + Constants.NEW_LINE );
				LOGGER.warn( message.toString() );
			}
		}
	}
	


	/**
	 * An entry used by the map of diffusers that contains the {@link Diffuser} and the list of class path end-points.
	 *  
	 * @author Robert Philipp
	 */
	private static class DiffuserEntry {
		
		private final RestfulDiffuser diffuser;
		private final ClassLoader classLoader;

		/**
		 * Constructs an entry containing the {@link Diffuser} and the list of class path end-points
		 * @param diffuser The {@link Diffuser}
		 * @param classPaths The list of class path endpoints
		 */
//		public DiffuserEntry( final RestfulDiffuser diffuser, final List< URI > classPaths )
		public DiffuserEntry( final RestfulDiffuser diffuser, final ClassLoader classLoader )
		{
			this.diffuser = diffuser;
			this.classLoader = classLoader;
//			if( classPaths == null || classPaths.isEmpty() )
//			{
//				this.classLoader = null;
//			}
//			else
//			{
//				this.classLoader = new RestfulClassLoader( classPaths );
//			}
		}
		
		/**
		 * @return the diffuser associated with this entry
		 */
		public RestfulDiffuser getDiffuser()
		{
			return diffuser;
		}

		/**
		 * @return The class loader associated with the diffuser
		 */
		public ClassLoader getClassLoader()
		{
			return classLoader;
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
		private final DiffuserLoadCalc loadCalc;
		
		/**
		 * Constructs a {@link Callable} task for the {@link ExecutorService}
		 * @param methodName The name of the method to call
		 * @param arguments The arguments/parameters passed to the method
		 * @param returnType The return type of the method call
		 * @param deserializedObject The deserialized object that holds the state
		 * @param diffuser The diffuser that is used to run/diffuser the method call
		 * @param loadCalc The {@link DiffuserLoadCalc} that is used to determine the load which
		 * allows the diffuser to determine whether to compute locally, or diffuser forward.
		 */
		public DiffuserTask( final String methodName,
							 final List< ? super Object > arguments,
							 final Class< ? > returnType,
							 final Object deserializedObject,
							 final Diffuser diffuser,
							 final DiffuserLoadCalc loadCalc )
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
			return diffuser.runObject( loadCalc.getLoad(), returnType, deserializedObject, methodName, arguments );
		}
	}
}
