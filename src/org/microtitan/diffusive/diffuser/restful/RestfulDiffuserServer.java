package org.microtitan.diffusive.diffuser.restful;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.utils.Constants;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
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
	
	public static final String DEFAULT_SERVER_URI = "http://localhost:8182";
	
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

//			// set the resource packages that will be searched for RESTful bindings 
//			final ResourceConfig resourceConfig = new PackagesResourceConfig( resourcePackages.toArray( new String[ resourcePackages.size() ] ) );
//			
//			// attempt to start the server
//			server = GrizzlyServerFactory.createHttpServer( serverUri, resourceConfig );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to start Grizzly web server." + Constants.NEW_LINE );
			message.append( "  Server URI: " + serverUri.toString() + Constants.NEW_LINE );
//			message.append( "  Resource Packages: " + Constants.NEW_LINE );
//			for( String resourcePackage : resourcePackages )
//			{
//				message.append( "    " + resourcePackage + Constants.NEW_LINE );
//			}
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		
		if( LOGGER.isDebugEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Started Grizzly web server." + Constants.NEW_LINE );
			message.append( "  Server URI: " + serverUri.toString() + Constants.NEW_LINE );
//			message.append( "  Resource Packages: " + Constants.NEW_LINE );
//			for( String resourcePackage : resourcePackages )
//			{
//				message.append( "    " + resourcePackage + Constants.NEW_LINE );
//			}
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
		Logger.getRootLogger().setLevel( Level.DEBUG );

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
