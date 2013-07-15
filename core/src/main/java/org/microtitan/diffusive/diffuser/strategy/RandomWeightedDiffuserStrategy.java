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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Selects an end-point based on a weighted distribution. This allows, for example, one 
 * end-point to be picked more often than another. By specifying a map of 
 * @author Robert Philipp
 *
 */
public class RandomWeightedDiffuserStrategy extends AbstractDiffuserStrategy {

	private static final long DEFAULT_SEED = 1;
	
	private Random random;

	/**
	 * Constructor that accepts a list of end-points and a seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 * @param randomSeed The random seed that initializes the pseudo-random number sequence
	 */
	public RandomWeightedDiffuserStrategy( final Map< URI, Double > endpoints, final long randomSeed )
	{
		super( endpoints );
		
		random = new Random( randomSeed );
	}

	/**
	 * Constructor that accepts a list of end-points and a default seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 */
	public RandomWeightedDiffuserStrategy( final Map< URI, Double > endpoints )
	{
		this( endpoints, DEFAULT_SEED );
	}
	
	/**
	 * Constructor that has an empty list of end-points and a default seed for the pseudo-random number generator
	 */
	public RandomWeightedDiffuserStrategy()
	{
		this( new HashMap< URI, Double >() );
	}
	
	/**
	 * Copy constructor
	 * @param strategy The {@link RandomWeightedDiffuserStrategy} to copy
	 */
	public RandomWeightedDiffuserStrategy( final RandomWeightedDiffuserStrategy strategy )
	{
		super( strategy );
		this.random = new Random( DEFAULT_SEED );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#getEndpoint()
	 */
	@Override
	public List< URI > getEndpoints()
	{
		// grab the next value in [0.0, sum of weights)
		final double value = random.nextDouble() * getWeightSum();
		
		// find the interval in which the random number falls
		double lowerBound = 0.0;
		URI endpoint = null;
		for( int i = 0; i < getNumEndpoints(); ++i )
		{
			final double weight = getWeight( i );
			if( value >= lowerBound && value < lowerBound + weight )
			{
				// we found the interval, we're done looping, set the end-point
				endpoint = getEndpoint( i );
				break;
			}
			lowerBound += weight;
		}
		
		// return the URI that is at that index
		return Arrays.asList( endpoint );
	}
	
	public static void main( String[] args )
	{
		final Map< URI, Double > endpoints = new LinkedHashMap<>();
		endpoints.put( URI.create( "http://localhost:1" ), 0.5 );
		endpoints.put( URI.create( "http://localhost:2" ), 1.5 );
		endpoints.put( URI.create( "http://localhost:3" ), 3.0 );
		endpoints.put( URI.create( "http://localhost:4" ), 0.1 );
		
		final RandomWeightedDiffuserStrategy strategy = new RandomWeightedDiffuserStrategy( endpoints );
		System.out.println( strategy.toString() );
		
		final int size = 1_000_000;
		final List< URI > selected = new ArrayList<>( size );
		for( int i = 0; i < size; ++i )
		{
			selected.add( strategy.getEndpoints().get( 0 ) );
		}
		
		final Map< URI, Integer > results = new LinkedHashMap<>();
		for( URI uri : selected )
		{
			if( results.containsKey( uri ) )
			{
				final int num = results.get( uri ) + 1;
				results.put( uri, num );
			}
			else
			{
				results.put( uri, 1 );
			}
		}
		
		for( Map.Entry< URI, Integer > entry : results.entrySet() )
		{
			final long num = entry.getValue();
			final double frac = (double)num / size;
			System.out.println( entry.getKey().toString() + ": " + 
								String.format( "%8d", num ) + 
								" (" + String.format( "%6.4f", frac ) + 
								" versus " + 
								String.format( "%6.4f", strategy.getWeight( entry.getKey() ) / strategy.getWeightSum() ) + 
								")" );
		}
	}

    /**
     * @return a copy of the {@link RandomWeightedDiffuserStrategy}
     */
	@Override
	public RandomWeightedDiffuserStrategy getCopy()
	{
		return new RandomWeightedDiffuserStrategy( this );
	}
}
