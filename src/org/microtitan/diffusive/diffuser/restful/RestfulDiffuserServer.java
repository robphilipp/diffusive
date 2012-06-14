package org.microtitan.diffusive.diffuser.restful;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;

public class RestfulDiffuserServer {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserServer.class );
	
	private HttpServer server;
	
	/**
	 * 
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
	
	public void stop()
	{
		server.stop();
	}
	
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
}
