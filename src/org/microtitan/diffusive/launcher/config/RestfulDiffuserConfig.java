package org.microtitan.diffusive.launcher.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulClassPathResource;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;
import org.microtitan.diffusive.launcher.DiffusiveLauncher;

/**
 * Configuration class used to configure the Restful Diffuser framework. The method annotated
 * with {@link DiffusiveConfiguration} is the method that will be called by the {@link DiffusiveLauncher} 
 * to configure the framework. Any static method can be used in this manner. For example, one could
 * write a static method that reads from a configuration file and then performs the necessary configuration.
 * 
 * @author Robert Philipp
 * Jul 18, 2012
 */
public class RestfulDiffuserConfig {
	
	// holds the list of client endpoints to which diffused methods are sent. the end-point must
	// have a restful diffusive server running that can accept requests.
//	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( RestfulDiffuserServer.DEFAULT_SERVER_URI );
	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( "http://192.168.1.4:8182" );//, "http://192.168.1.8:8182" );
	
	// holds the base URI of the class path that gets passed to the remote diffuser manager when
	// creating a diffuser. this allows the remote code to load classes from a remote server. This
	// URI typically points to the host running that is launching the code to be diffused (since it
	// is that code that has the classes), or some other host that holds all the required classes (that
	// have been deployed there) and is running a restful diffuser server
	public static final List< String > CLASSPATH_URI = Arrays.asList( RestfulDiffuserServer.DEFAULT_SERVER_URI );
	
	// the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	// unless of course, there are no client end-points specified. When the threshold is below the 
	// load threshold, the diffuser will call the local diffuser to execute the tasks.
	public static final double LOAD_THRESHOLD = 0.5;
	
	/**
	 * Method that is called to configure the Diffusive framework. In particular, creates a 
	 * {@link RestfulDiffuser} with the specified diffusion end-points and the class path URI
	 * passed to the remote restful diffuser so that it can load classes for which it needs to 
	 * run diffused methods.
	 * 
	 * Sets the default diffuser into the {@link KeyedDiffuserRepository}. This is a required step
	 * because the byte code engineering needs to know which diffuser to use, and the repository is
	 * the location holding that diffuser.
	 */
	@DiffusiveConfiguration
	public static final void configure()
	{
		// create a default diffuser, load the diffuser repository, and set the default diffuser
		// into the repository (needed by the Javassist diffuser method replacement)
//		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.PERSISTENCE_XML.getName() );
		final DiffuserStrategy strategy = createStrategy();
		final List< URI > classPaths = createClassPathList();
		final Diffuser diffuser = new RestfulDiffuser( serializer, strategy, classPaths, LOAD_THRESHOLD );
		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );
	}
	
	/**
	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
	 * local {@link RestfulDiffuser} can diffuse method calls.
	 */
	private static List< URI > createEndpointList()
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : CLIENT_ENDPOINTS )
		{
			endpoints.add( URI.create( client + RestfulDiffuserManagerResource.DIFFUSER_PATH ) );
		}
		return endpoints;
	}
	
	/**
	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
	 * local {@link RestfulDiffuser} can diffuse method calls.
	 */
	private static DiffuserStrategy createStrategy()
	{
		return new RandomDiffuserStrategy( createEndpointList() );
	}
	
	/**
	 * @return a {@link List} of {@link URI} holding the location from which a {@link RestfulClassLoader}
	 * can load local classes. To each URI in the list, the resource path is appended.
	 */
	private static List< URI > createClassPathList()
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : CLASSPATH_URI )
		{
			endpoints.add( URI.create( client + RestfulClassPathResource.CLASSPATH_PATH ) );
		}
		return endpoints;
	}
}
