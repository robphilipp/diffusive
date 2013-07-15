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
package org.microtitan.diffusive.diffuser.strategy;

import java.util.List;

import org.freezedry.persistence.annotations.PersistCollection;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.utils.ConfigUtils;

/**
 * Holds the configuration for the diffuser strategies.
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

	/**
	 * @return The list of end-points from which the strategy can select
	 */
	public List< String > getClientEndpoints()
	{
		return clientEndpoints;
	}

	/**
	 * Sets the list of end-points from which the strategy can select
	 * @param clientEndpoints The list of end-points from which the strategy can select
	 */
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
		final List< String > validEndpoints = ConfigUtils.validateEndpoints( clientEndpoints );
		return new RandomDiffuserStrategy( ConfigUtils.createEndpointList( validEndpoints ), randomSeed );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		if( clientEndpoints != null )
		{
			buffer.append( "Client End-Points: " + Constants.NEW_LINE );
			for( String endpoint : clientEndpoints )
			{
				buffer.append( "  " + endpoint + Constants.NEW_LINE );
			}
		}
		buffer.append( "Random Seed: " + randomSeed );
		return buffer.toString();
	}
}
