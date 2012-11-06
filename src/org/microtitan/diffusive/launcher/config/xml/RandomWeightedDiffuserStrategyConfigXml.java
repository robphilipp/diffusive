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
package org.microtitan.diffusive.launcher.config.xml;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.freezedry.persistence.annotations.PersistMap;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomWeightedDiffuserStrategy;
import org.microtitan.diffusive.launcher.config.ConfigUtils;

public class RandomWeightedDiffuserStrategyConfigXml implements DiffuserStrategyConfigXml {

	/**
	 * Holds the client end-points and their associated weights
	 */
	@PersistMap(entryPersistName="client",keyPersistName="endPoint", valuePersistName="weight")
	private Map< String, Double > endpoints;
	
	private long randomSeed;

	/**
	 * @return
	 */
	public Map< String, Double > getClientEndpoints()
	{
		return endpoints;
	}

	/**
	 * Sets the list of end-points from which the strategy can select
	 * @param clientEndpoints The list of end-points from which the strategy can select
	 */
	public void setClientEndpoints( final Map< String, Double > clientEndpoints )
	{
		this.endpoints = clientEndpoints;
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
		final List< String > validEndpoints = ConfigUtils.validateEndpoints( endpoints.keySet() );
		final Map< URI, Double > urlEndpoints = new LinkedHashMap<>();
		for( String endpoint : validEndpoints )
		{
			final Double weight = endpoints.get( endpoint );
			if( weight != null )
			{
				urlEndpoints.put( URI.create( endpoint ), weight );
			}
		}
		return new RandomWeightedDiffuserStrategy( urlEndpoints, randomSeed );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		if( endpoints != null )
		{
			buffer.append( "Client End-Points: " + Constants.NEW_LINE );
			for( Map.Entry< String, Double > entry : endpoints.entrySet() )
			{
				buffer.append( "  " + entry.getKey() + ": " + String.format( "%6.4f", entry.getValue() ) + Constants.NEW_LINE );
			}
		}
		buffer.append( "Random Seed: " + randomSeed );
		return buffer.toString();
	}
}
