package org.microtitan.diffusive.launcher.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;

public class ConfigUtils {

	private static final Logger LOGGER = Logger.getLogger( ConfigUtils.class );

	/**
	 * @return a {@link List} of {@link URI} that hold the location of end-points to which the
	 * local {@link RestfulDiffuser} can diffuse method calls.
	 */
	public static List< URI > createEndpointList( final List< String > clientEndpoints )
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : clientEndpoints )
		{
			endpoints.add( URI.create( client ) );
		}
		return endpoints;
	}
	
	/**
	 * Validates each end-point specified in the list. If the end-point is valid, then it adds it the 
	 * return list, otherwise it logs a warning with the invalid end-point address.
	 * @param clientEndpoints The list of end-points of the client
	 * @return A validated list of end-points.
	 */
	public static List< String > validateEndpoints( final List< String > clientEndpoints )
	{
		final List< String > endpoints = new ArrayList<>();
		for( String client : clientEndpoints )
		{
			URI uri;
			try
			{
				uri = new URI( client );
				endpoints.add( uri.getScheme() + "://" + uri.getHost() +":" + uri.getPort() + uri.getPath() );
			}
			catch( URISyntaxException e )
			{
				final String message = "Invalid URI for end-point: " + client;
				LOGGER.warn( message, e );
			}
		}
		return endpoints;
	}
}
