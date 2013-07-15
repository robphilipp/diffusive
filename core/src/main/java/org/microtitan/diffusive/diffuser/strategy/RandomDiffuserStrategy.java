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
import java.util.List;
import java.util.Random;

/**
 * Random diffuser strategy that selects an end-point based on a uniform pseudo-random number that spans
 * the end-points specified in this strategy.
 * 
 * @author Robert Philipp
 */
public class RandomDiffuserStrategy extends AbstractDiffuserStrategy {
	
	private static final long DEFAULT_SEED = 1;
	
	private Random random;

	/**
	 * Constructor that accepts a list of end-points and a seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 * @param randomSeed The random seed that initializes the pseudo-random number sequence
	 */
	public RandomDiffuserStrategy( final List< URI > endpoints, final long randomSeed )
	{
		super( endpoints );
		
		random = new Random( randomSeed );
	}

	/**
	 * Constructor that accepts a list of end-points and a default seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 */
	public RandomDiffuserStrategy( final List< URI > endpoints )
	{
		this( endpoints, DEFAULT_SEED );
	}
	
	/**
	 * Constructor that has an empty list of end-points and a default seed for the pseudo-random number generator
	 */
	public RandomDiffuserStrategy()
	{
		this( new ArrayList< URI >() );
	}
	
	/**
	 * Copy constructor
	 * @param strategy The {@link RandomDiffuserStrategy} to copy
	 */
	public RandomDiffuserStrategy( final RandomDiffuserStrategy strategy )
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
		// grab the next value in [0.0, 1.0)
		final double value = random.nextDouble();
		
		// calculate the index based on the random number
		final int index = (int)( value * getNumEndpoints() );
		
		// return the URI that is at that index
		return Arrays.asList( getEndpoint( index ) );
	}

    /**
     * @return a cony of the {@link RandomDiffuserStrategy}
     */
	@Override
	public RandomDiffuserStrategy getCopy()
	{
		return new RandomDiffuserStrategy( this );
	}

}
