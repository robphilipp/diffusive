package org.microtitan.diffusive.launcher.config.xml;

import java.util.List;

import org.freezedry.persistence.annotations.PersistCollection;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;
import org.microtitan.diffusive.launcher.config.ConfigUtils;

/**
 * 
 * @author Robert Philipp
 */
public class RandomDiffuserStrategyConfigXml implements DiffuserStrategyConfigXml {

	/**
	 * The list of end-points from which the strategy may choose 
	 */
	@PersistCollection(elementPersistName="endPoint")
	private List< String > clientEndpoints;
	
	private long randomSeed;

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.launcher.config.xml.DiffuserStrategyConfigXml#getClientEndpoints()
	 */
	@Override
	public List< String > getClientEndpoints()
	{
		return clientEndpoints;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.launcher.config.xml.DiffuserStrategyConfigXml#setClientEndpoints(java.util.List)
	 */
	@Override
	public void setClientEndpoints( final List< String > clientEndpoints )
	{
		this.clientEndpoints = clientEndpoints;
	}
	
	/**
	 * @return The seed for the random number generator
	 */
	public long getRandomSeed()
	{
		return randomSeed;
	}
	
	/**
	 * Sets the seed for the random number generator
	 * @param randomSeed the seed for the random number generator
	 */
	public void setRandomSeed( long randomSeed )
	{
		this.randomSeed = randomSeed;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.launcher.config.xml.DiffuserStrategyConfigXml#createStrategy()
	 */
	@Override
	public DiffuserStrategy createStrategy()
	{
		final List< String > validEnpoints = ConfigUtils.validateEndpoints( clientEndpoints );
		return new RandomDiffuserStrategy( ConfigUtils.createEndpointList( validEnpoints ), randomSeed );
	}
}
