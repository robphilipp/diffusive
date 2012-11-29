/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.microtitan.diffusive.diffuser.restful.server;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.ext.RuntimeDelegate;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.classloaders.factories.RestfulDiffuserClassLoaderFactory;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserApplication;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulClassPathResource;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache;
import org.microtitan.diffusive.diffuser.restful.server.config.RestfulDiffuserServerConfig;
import org.microtitan.diffusive.diffuser.restful.server.config.ServerMode;
import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoadCalc;
import org.microtitan.diffusive.utils.NetworkUtils;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;

/**
 * The RESTful diffuser server that listens to the specified URI and uses the resources and information
 * contained in the specified JAX-RS application. 
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserServer {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserServer.class );
	
	// default address for server
	private static final int SERVER_PORT = 8182;
	private static final String SERVER_SCHEME = "http";
	public static final String DEFAULT_SERVER_URI = NetworkUtils.createLocalHostServerUri( SERVER_SCHEME, SERVER_PORT );
	
	// configuration file paths
	public static final String DEFAULT_CONFIGURATION_FILE = "restful_server_config.xml";
	public static final String BASE_CONFIGURATION_PATH = "config/";
	public static final String REMOTE_CONFIGURATION_PATH = BASE_CONFIGURATION_PATH + ServerMode.REMOTE.getName() + "/";
	public static final String CLASS_CONFIGURATION_PATH = BASE_CONFIGURATION_PATH + ServerMode.CLASS.getName() + "/";
	
	// class containing the configuration method
	public static final String DEFAULT_CONFIGURATION_CLASS = RestfulDiffuserServerConfig.class.getName();
	
	// the actual server
	private final HttpServer server;

	/**
	 * Creates a starts the RESTful diffuser server listening at the specified server URI and using the 
	 * specified JAX-RS application.
	 * @param serverUri The URI for this RESTful diffuser (i.e. the URI at which others would call this diffuser)
	 * @param application The JAX-RS application that contains information about the resources that contain the JAX-RS bindings
	 */
	public RestfulDiffuserServer( final URI serverUri, 
								  final RestfulDiffuserApplication application )
	{
		this.server = createHttpServer( serverUri, application );
	}
	
	/*
	 * Creates and starts a Grizzly HTTP server with the base URI set to the specified server URI and the resource
	 * packages listed to search for JAX-RS bindings.
	 * @param serverUri The URI for this RESTful diffuser (i.e. the URI at which others would call this diffuser)
	 * @param resourcePackages The packages that contain the resources that contain the JAX-RS bindings
	 * @return a Grizzly HTTP server that is running, or null if it fails to start
	 */
	private static HttpServer createHttpServer( final URI serverUri, final RestfulDiffuserApplication application )
	{
		HttpServer server = null;
		try
		{
			// create an HTTP handler that assigns the specified application  
			final HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint( application, HttpHandler.class );
			
			// attempt to start the server
			server = GrizzlyServerFactory.createHttpServer( serverUri, handler );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to start Grizzly web server." + Constants.NEW_LINE );
			message.append( "  Server URI: " + serverUri.toString() + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		if( LOGGER.isDebugEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Started Grizzly web server." + Constants.NEW_LINE );
			message.append( "  Server URI: " + serverUri.toString() + Constants.NEW_LINE );
			LOGGER.debug( message.toString() );
		}
		return server;
	}
	
	/**
	 * Stops the {@link RestfulDiffuserServer}
	 */
	public void stop()
	{
		server.stop();
	}
	
	/**
	 * Starts the {@link RestfulDiffuserServer}
	 */
	public void start()
	{
		try
		{
			server.start();
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to start Grizzly web server." + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		if( LOGGER.isDebugEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Started Grizzly web server." + Constants.NEW_LINE );
			LOGGER.debug( message.toString() );
		}
	}
	
	/**
	 * Extracts the configuration directory from the command-line arguments
	 * @param serverModeSpec The option specification holding the server mode argument information
	 * @param options The options parsed from the command-line
	 * @return The configuration directory
	 */
	private static final String getConfigDir( final OptionSpec< String > serverModeSpec, final OptionSet options )
	{
		String configDirectory = null;
		final ServerMode mode = ServerMode.getServerType( serverModeSpec.value( options ) );
		if( mode == ServerMode.REMOTE )
		{
			configDirectory = REMOTE_CONFIGURATION_PATH;
		}
		else if( mode == ServerMode.CLASS )
		{
			configDirectory = CLASS_CONFIGURATION_PATH;
		}
		else
		{
			final String message = "Invalid argument for \"server-mode\" option: " + serverModeSpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return configDirectory;
	}
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main( String...args ) throws IOException
	{
		// set up the command-line arguments
		final OptionParser parser = new OptionParser();
		final OptionSpec< String > logLevelSpec = 
				parser.accepts( "log-level" ).withRequiredArg().ofType( String.class ).defaultsTo( Level.WARN.toString() ).
				describedAs( Level.TRACE + "|" + Level.DEBUG + "|" + Level.INFO + "|" + Level.WARN + "|" + Level.ERROR );
		final OptionSpec< String > serverModeSpec = 
				parser.accepts( "server-mode" ).withRequiredArg().ofType( String.class ).defaultsTo( ServerMode.REMOTE.getName() ).
				describedAs( ServerMode.REMOTE.getName() + "|" + ServerMode.CLASS.getName() );
		final OptionSpec< String > serverUriSpec = 
				parser.accepts( "server-uri" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_SERVER_URI );
		final OptionSpec< String > configFileSpec = 
				parser.accepts( "config-file-name" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_CONFIGURATION_FILE );
		final OptionSpec< String > configClassSpec = 
				parser.accepts( "config-class" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_CONFIGURATION_CLASS );
		final OptionSpec< Integer > maxThreadsSpec = 
				parser.accepts( "max-threads" ).withRequiredArg().ofType( Integer.class ).defaultsTo( 100 );
		final OptionSpec< Integer > maxResultsCachedSpec = 
				parser.accepts( "max-results-cached" ).withRequiredArg().ofType( Integer.class ).defaultsTo( 100 );
		parser.accepts( "help" );
		
		// parse the command-line arguments
		OptionSet options = null;
		try
		{
			options = parser.parse( args );
		}
		catch( OptionException e )
		{
			e.printStackTrace();
			LOGGER.error( "Error parsing the command-line options.", e );

			System.out.println( "\nPlease see the usage information below: " );
			parser.printHelpOn( System.out );
			System.exit( -1 );
		}

		// if the user requests help, print out help
		if( options.has( "help" ) )
		{
			parser.printHelpOn( System.out );
			System.exit( 0 );
		}

		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.toLevel( logLevelSpec.value( options ) ) );
		
		// set up the options based on the values from the command-line
		final URI serverUri = URI.create( serverUriSpec.value( options ) );
		final String configDirectory = getConfigDir( serverModeSpec, options );
		final String configFileName = configDirectory + configFileSpec.value( options ); 
		final String configClassName = configClassSpec.value( options );
		final int maxThreads = maxThreadsSpec.value( options );
		final int maxResultsCached = maxResultsCachedSpec.value( options );
		
		// report the options used
		final StringBuffer buffer = new StringBuffer( Constants.NEW_LINE + "Configuration Items" + Constants.NEW_LINE );
		buffer.append( "  Server Mode: " + serverModeSpec.value( options ) + Constants.NEW_LINE );
		buffer.append( "  Server URI: " + serverUri.toString() + Constants.NEW_LINE );
		buffer.append( "  Config File: " + configFileName + Constants.NEW_LINE );
		buffer.append( "  Config Class: " + configClassName + Constants.NEW_LINE );
		buffer.append( "  Max Threads: " + maxThreads + Constants.NEW_LINE );
		buffer.append( "  Max Results Cached: " + maxResultsCached + Constants.NEW_LINE );
		LOGGER.info( buffer.toString() );
		System.out.println( buffer.toString() );

		final Map< String, Object[] > configClasses = new LinkedHashMap<>();
		configClasses.put( configClassName, new Object[] { configFileName } );

		// create and set up the executor service that is used to distribute tasks amongst threads in its thread-pool
		final ExecutorService executor = RestfulDiffuserManagerResource.createExecutorService( maxThreads );
		
		// create and set up the cache that holds the results of executed methods so that they can be retrieved
		final ResultsCache cache = RestfulDiffuserManagerResource.createResultsCache( maxResultsCached );
		
		// create and set up the load calculator that is used to determine if the task should be run on this
		// server, or should be diffused to one of (if any exist) end-points attached to this server.
		final DiffuserLoadCalc loadCalc = RestfulDiffuserManagerResource.createLoadCalc( cache );
		
		// create the manager resource and the web application needed by the web server
		final RestfulDiffuserManagerResource resource = new RestfulDiffuserManagerResource( executor, cache, loadCalc, configClasses, RestfulDiffuserClassLoaderFactory.getInstance() );
		final RestfulDiffuserApplication application = new RestfulDiffuserApplication();
		application.addSingletonResource( resource );
		application.addPerRequestResource( RestfulClassPathResource.class );

		// create the web server
		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, application );
				
		System.out.println( String.format( "Jersy app started with WADL available at %s/application.wadl", serverUri ) );
		System.out.println( String.format( "Try out %s.", serverUri ) );
		System.out.println( "Hit enter to stop it..." );
		try
		{
			System.in.read();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		server.stop();
	}

}
