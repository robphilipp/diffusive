package org.microtitan.diffusive.diffuser.resolver.config.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.annotations.PersistMap;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.resolver.EndPointResolver;
import org.microtitan.diffusive.diffuser.resolver.MappedEndPointResolver;

/**
 * Immutable object that holds the mapping from the non-physical to physical end-points
 * 
 * @author Robert Philipp
 */
public class FileEndpoingMappingConfigXml implements EndpointMappingConfigXml {

	private static final Logger LOGGER = Logger.getLogger( FileEndpoingMappingConfigXml.class );
	
	@PersistMap( keyPersistName="nonPhysicalEndpoint", valuePersistName="physicalEndpoint" )
	private Map< String, String > mapping;

	// holds the mapping where the string representations of the URI have been converted
	// to URI objects. Recall that this object is immutable.
	private Map< URI, URI > map = null;

	/**
	 * @return The mapping from non-physical to physical end-points.
	 */
	public Map< URI, URI > getMapping()
	{
		return convertMap();
	}
	
	/**
	 * Converts the mappings URI from a string representation to a URI object and
	 * logs all invalid URI. 
	 * @return A {@link Map} containing {@link URI} objects based on their string represenations
	 */
	private synchronized Map< URI, URI > convertMap()
	{
		if( map == null )
		{
			map = new LinkedHashMap<>();
			final Map< String, String > unmapped = new LinkedHashMap<>();
			
			// convert the map of strings to a map of uri
			for( Map.Entry< String, String > entry : mapping.entrySet() )
			{
				URI nonPhysicalUri = null;
				URI physicalUri = null;
				try
				{
					nonPhysicalUri = new URI( entry.getKey() );
				}
				catch( URISyntaxException e ) {}
				try
				{
					physicalUri = new URI( entry.getValue() );
				}
				catch( URISyntaxException e ) {}
				
				if( nonPhysicalUri != null && physicalUri != null )
				{
					map.put( nonPhysicalUri, physicalUri );
				}
				else
				{
					unmapped.put( (nonPhysicalUri == null ? "*" : "") + entry.getKey(), 
								  (physicalUri == null ? "*" : "") + entry.getValue() );
				}
			}
	
			// log any specified URI that had an invalid format
			if( !unmapped.isEmpty() )
			{
				final StringBuffer message = new StringBuffer();
				for( Map.Entry< String, String > entry : unmapped.entrySet() )
				{
					message.append( "Invalid URI in mapping (invalid URI are marked with a \"*\":" + Constants.NEW_LINE );
					message.append( "  [" + entry.getKey() + "] --> [" + entry.getValue() + "]" + Constants.NEW_LINE );
				}
				LOGGER.warn( message.toString() );
			}
		}
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.resolver.config.xml.EndpointMappingConfigXml#createResolver()
	 */
	@Override
	public EndPointResolver createResolver()
	{		
		return new MappedEndPointResolver( convertMap() );
	}

}
