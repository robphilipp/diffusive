/**
 * 
 */
package org.microtitan.diffusive.diffuser.resolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;

/**
 * @author Robert Philipp
 * 
 * End-Point resolver based on a simple one-to-one mapping from non-physical to 
 * physical end-points.
 *
 */
public class MappedEndPointResolver implements EndPointResolver {
	
	private static final Logger LOGGER = Logger.getLogger( MappedEndPointResolver.class );
	
	private final Map< URI, URI > mapping;
	
	/**
	 * Constructor that accepts a mapping from the non-physical end-point to the physical end-point
	 * @param mapping The {@link Map} whose keys represent the {@link URI} for the non-physical end-points
	 * and the values represent the corresponding {@link URI} for the physical end-points.
	 */
	public MappedEndPointResolver( final Map< URI, URI > mapping )
	{
		this.mapping = new LinkedHashMap<>( mapping );
	}

	/* (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.resolver.EndPointResolver#resolve(org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy)
	 */
	@Override
	public void resolve( final DiffuserStrategy strategy )
	{
		Map< URI, URI > mapped = null;
		List< URI > unmapped = null;
		if( LOGGER.isInfoEnabled() )
		{
			mapped = new LinkedHashMap<>();
			unmapped = new ArrayList<>();
		}
		
		final List< URI > nonPhysicalEndpoints = strategy.getEndpointList();
		final List< URI > endPoints = new ArrayList<>( nonPhysicalEndpoints.size() );
		for( final URI nonPhysicalEndpoint : nonPhysicalEndpoints )
		{
			final URI endpoint = mapping.get( nonPhysicalEndpoint );
			if( endpoint != null )
			{
				endPoints.add( endpoint );
				if( LOGGER.isInfoEnabled() )
				{
					mapped.put( nonPhysicalEndpoint, endpoint );
				}
			}
			else
			{
				// warn about unmapped end points
				final StringBuffer message = new StringBuffer();
				message.append( "Failed to map non-physical end-point [" + nonPhysicalEndpoint.toString() + "]" );
				LOGGER.warn( message.toString() );
				if( LOGGER.isInfoEnabled() )
				{
					unmapped.add( nonPhysicalEndpoint );
				}
			}
		}
		
		// log the endpoints that were and were not mapped
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			for( Map.Entry< URI, URI > entry : mapped.entrySet() )
			{
				message.append( "Non-physical end-point [" + entry.getKey().toString() );
				message.append( "] --> physical end-point [" + entry.getValue().toString() + Constants.NEW_LINE );
			}
			for( URI endpoint : unmapped )
			{
				message.append( "Non-physical end-point [" + endpoint.toString() + "] --> null" + Constants.NEW_LINE );
			}
			LOGGER.info( message.toString() );
		}
	}
}
