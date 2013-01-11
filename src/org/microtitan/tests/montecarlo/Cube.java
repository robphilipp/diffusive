package org.microtitan.tests.montecarlo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cube whose "lower left-hand side" sits on the origin.
 * 
 * @author Robert Philipp
 */
public class Cube {

	private final List< Double > dimensions;
	
	public Cube( final List< Double > dimensions )
	{
		this.dimensions = new ArrayList<>();
		for( double dimension : dimensions )
		{
			this.dimensions.add( Math.abs( dimension ) );
		}
	}
	
	public Cube( final Double...dimensions )
	{
		this( Arrays.asList( dimensions ) );
	}
	
	public int getDimension()
	{
		return dimensions.size();
	}
	
	public double getDimension( final int dimension )
	{
		return dimensions.get( dimension );
	}
	
	public List< Double > getDimensions()
	{
		return new ArrayList< Double >( dimensions );
	}
	
	public boolean isInside( final Double...point )
	{
		return isInside( Arrays.asList( point ) );
	}
	
	public boolean isInside( final List< Double > point )
	{
		boolean isInside = false;
		if( point.size() == dimensions.size() && !dimensions.isEmpty() )
		{
			isInside = true;
			for( int i = 0; i < dimensions.size(); ++i )
			{
				if( point.get( i ) > dimensions.get( i ) )
				{
					isInside = false;
					break;
				}
			}
		}
		return isInside;
	}
	
	public boolean isContained( final Cube cube )
	{
		return isInside( cube.dimensions );
	}
	
	public double getVolume()
	{
		double volume = ( dimensions.size() > 0 ? 1 : 0 );
		for( Double dimension : dimensions )
		{
			volume *= dimension;
		}
		return volume;
	}
}
