package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomWeightedDiffuserStrategy extends AbstractDiffuserStrategy {

	private static final long DEFAULT_SEED = 1;
	
	private Random random;

	/**
	 * Constructor that accepts a list of end-points and a seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 * @param randomSeed The random seed that initializes the pseudo-random number sequence
	 */
	public RandomWeightedDiffuserStrategy( final Map< Double, URI > endpoints, final long randomSeed )
	{
		super( endpoints );
		
		random = new Random( randomSeed );
	}

	/**
	 * Constructor that accepts a list of end-points and a default seed for the pseudo-random number generator
	 * @param endpoints The {@link List} of end-point {@link URI} to which to diffuser methods
	 */
	public RandomWeightedDiffuserStrategy( final Map< Double, URI > endpoints )
	{
		this( endpoints, DEFAULT_SEED );
	}
	
	/**
	 * Constructor that has an empty list of end-points and a default seed for the pseudo-random number generator
	 */
	public RandomWeightedDiffuserStrategy()
	{
		this( new HashMap< Double, URI >() );
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
		final Map< Double, URI > endpoints = new LinkedHashMap<>();
		endpoints.put( 0.5, URI.create( "http://localhost:1" ) );
		endpoints.put( 1.5, URI.create( "http://localhost:2" ) );
		endpoints.put( 3.0, URI.create( "http://localhost:3" ) );
		endpoints.put( 0.1, URI.create( "http://localhost:4" ) );
		
		final RandomWeightedDiffuserStrategy strategy = new RandomWeightedDiffuserStrategy( endpoints );
		
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
			System.out.println( entry.getKey().toString() + ": " + entry.getValue() );
		}
	}
}
