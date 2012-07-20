package org.microtitan.diffusive.diffuser.restful;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.utils.Constants;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.microtitan.diffusive.classloaders.RestfulClassLoader;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulClassPathResource;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;

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
	public static final String DEFAULT_SERVER_URI = createDefaultServerUri( SERVER_SCHEME, SERVER_PORT );
	
	private HttpServer server;
	
	/**
	 * Creates a starts the RESTful diffuser server listening at the specified server URI and using the 
	 * specified JAX-RS application.
	 * @param serverUri The URI for this RESTful diffuser (i.e. the URI at which others would call this diffuser)
	 * @param application The JAX-RS application that contains information about the resources that contain the JAX-RS bindings
	 */
	public RestfulDiffuserServer( final URI serverUri, final RestfulDiffuserApplication application )
	{
		this.server = createHttpServer( serverUri, application );
	}
	
	/*
	 * @return grabs the ip address for the local host
	 */
	private static String getLocalIpAddress()
	{
		String ip = "localhost";
		try
        {
	        ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch( UnknownHostException e )
        {
	        e.printStackTrace();
        }
		return ip;
	}
	
	/*
	 * Constructs a string representation of the URI from the local host's IP address, the specified scheme and port
	 * @param scheme The scheme (i.e. http, https, etc)
	 * @param port The port at which the server listens
	 * @return a string representation of the URI
	 */
	private static String createDefaultServerUri( final String scheme, final int port )
	{
		return scheme + "://" + getLocalIpAddress() + String.format( ":%d", port );
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
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.WARN );

		// ensure that a class has been specified (the class must have a main)
		if( args.length < 1 )
		{
			System.out.println();
			System.out.println( "+-------------------------------------+" );
			System.out.println( "|  Usage: name_of_class_to_run [arg]* |" );
			System.out.println( "|                                     |" );
			System.out.println( "|  **** Running simple test code **** |" );
			System.out.println( "+-------------------------------------+" );
			System.out.println();
			args = new String[] { DEFAULT_SERVER_URI };
		}
		
		// TODO this needs to be set up through a configuration or programatically. Probably best through a RESTfulDiffusiveLauncher,
		// a LocalDiffusiveLauncher, a NullDiffusiveLauncher, etc..
		// run and set up the local RESTful Diffuser server
		final RestfulDiffuserManagerResource resource = new RestfulDiffuserManagerResource();
		final RestfulDiffuserApplication application = new RestfulDiffuserApplication();
		application.addSingletonResource( resource );
		application.addPerRequestResource( RestfulClassPathResource.class );

		final URI serverUri = URI.create( args[ 0 ] );
		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, application );
				
		System.out.println( String.format( "Jersy app start with WADL available at %sapplication.wadl", serverUri ) );
		System.out.println( String.format( "Try out %s.", serverUri ) );
		System.out.println( "Hit enter to stop it..." );
		try
		{
			System.in.read();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
	}

}
