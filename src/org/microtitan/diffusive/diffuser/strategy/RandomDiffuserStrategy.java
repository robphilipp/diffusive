package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.ArrayList;
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
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#getEndpoint()
	 */
	@Override
	public URI getEndpoint()
	{
		// grab the next value in [0.0, 1.0)
		final double value = random.nextDouble();
		
		// calculate the index based on the random number
		final int index = (int)( value * getNumEndpoints() );
		
		// return the URI that is at that index
		return getEndpoint( index );
	}

}
