package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

/**
 * 
 * @author desktop
 *
 */
public class RestfulDiffuserInfo {

	// used to serialize objects for making requests across the network
	private final String serializer;
	private final String strategy;
	private final List< String > endPoints;
	private final List< String > classPaths;
	private final double loadThreshold;
	
	// the maximum number of threads in the thread pool (ExecutorService) to account 
	// for redundant diffusion
	private final int maxRedundancy;
	private final int pollingTimeout;
	private final String pollingTimeUnit;

	/**
	 * Constructor that takes a {@link org.microtitan.diffusive.diffuser.restful.RestfulDiffuser} and converts it into an information object that
	 * can be used to represent the diffuser.
	 * @param diffuser The {@link org.microtitan.diffusive.diffuser.restful.RestfulDiffuser} on which to base the information object
	 */
	public RestfulDiffuserInfo( final RestfulDiffuser diffuser )
	{
		this.serializer = SerializerFactory.getSerializerName( diffuser.getSerializer().getClass() );
		this.strategy = diffuser.getStrategy().getClass().getName();
		this.endPoints = convertUri( diffuser.getStrategy().getEndpointList() );
		this.classPaths = convertUri( diffuser.getClassPaths() );
		this.loadThreshold = diffuser.getLoadThreshold();
		
		this.maxRedundancy = diffuser.getMaxRedundancy();
		this.pollingTimeout = diffuser.getPollingTimeout();
		this.pollingTimeUnit = diffuser.getPollingTimeUnit().name();
	}
	
	/**
	 * Converts the {@code {@link java.util.List}< {@link java.net.URI} > to a {@code {@link java.util.List}< {@link String} >
	 * @param uris the {@code {@link java.util.List}< {@link java.net.URI} > to convert
	 * @return a {@code {@link java.util.List}< {@link String} > representing the addresses
	 */
	private static List< String > convertUri( final List< URI > uris )
	{
		final List< String > addresses = new ArrayList<>();
		for( URI uri : uris )
		{
			addresses.add( uri.toString() );
		}
		return addresses;
	}

	/**
	 * @return the serializer
	 */
	public String getSerializer()
	{
		return serializer;
	}

	/**
	 * @return the strategy
	 */
	public String getStrategy()
	{
		return strategy;
	}

	/**
	 * @return the endPoints
	 */
	public List< String > getEndPoints()
	{
		return endPoints;
	}

	/**
	 * @return the classPaths
	 */
	public List< String > getClassPaths()
	{
		return classPaths;
	}

	/**
	 * @return the loadThreshold
	 */
	public double getLoadThreshold()
	{
		return loadThreshold;
	}

	/**
	 * @return the maxRedundancy
	 */
	public int getMaxRedundancy()
	{
		return maxRedundancy;
	}

	/**
	 * @return the pollingTimeout
	 */
	public int getPollingTimeout()
	{
		return pollingTimeout;
	}

	/**
	 * @return the pollingTimeUnit
	 */
	public String getPollingTimeUnit()
	{
		return pollingTimeUnit;
	}
}
