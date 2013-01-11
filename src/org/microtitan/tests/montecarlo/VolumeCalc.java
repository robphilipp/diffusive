package org.microtitan.tests.montecarlo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class VolumeCalc {

	private final Cube cube;
	private final Cube boundingShape;
	
	public VolumeCalc( final Cube cube, final Cube boundingShape )
	{
		this.cube = cube;
		this.boundingShape = boundingShape;
	}
	
	public double calcVolume( final long maxIterations )
	{
		return calcVolume( 0, maxIterations );
	}
	
	public double calcVolume( final long seed, final long maxIterations )
	{
		long numInCube = 0;

		// calculate the amount of space you want around the cube
		if( boundingShape.isContained( cube ) )
		{
			final Random random = new Random( seed );
			for( long i = 0; i < maxIterations; ++i )
			{
				// create the point randomly
				final List< Double > point = new ArrayList<>();
				for( int j = 0; j < cube.getDimension(); ++j )
				{
					point.add( random.nextDouble() * boundingShape.getDimension( j ) );
				}
				
				// check if the point is in the cube
				if( cube.isInside( point ) )
				{
					++numInCube;
				}
			}
		}
		return boundingShape.getVolume() * (double)numInCube / maxIterations;
	}
	
	public double variance( final List< Double > values )
	{
		// calc the mean
		double mean = 0;
		for( double value : values )
		{
			mean += value;
		}
		mean /= values.size();
		
		// calc the variance
		double variance = 0;
		for( double value : values )
		{
			variance += ( value - mean ) * ( value - mean );
		}
		variance /= values.size();
		
		return variance;
	}
	
	public static void main( final String...args )
	{
		final Cube cube = new Cube( 2.0, 2.0, 2.0, 2.0 );
		System.out.println( "Is (2.0, 2.0, 2.0, 2.0) in cube? " + cube.isInside( 2.0, 2.0, 2.0, 2.0 ) );
		System.out.println( "Is (2.0, 2.0, 2.0, 1.0) in cube? " + cube.isInside( 2.0, 2.0, 2.0, 1.0 ) );
		System.out.println( "Is (2.0, 2.0, 2.0, 3.0) in cube? " + cube.isInside( 2.0, 2.0, 2.0, 3.0 ) );
		System.out.println( "Analytical Volume: " + cube.getVolume() );
		
		final VolumeCalc calc = new VolumeCalc( cube, new Cube( 4.0, 4.0, 4.0, 4.0 ) );

		final List< Double > volumes = new ArrayList<>();
		for( int i = 0; i < 10; ++i )
		{
			final long start = System.currentTimeMillis();
			final double volume = calc.calcVolume( i, 10_000_000 );
			final double elapsedTime = (double)(System.currentTimeMillis() - start)/1000;
			System.out.println( (i+1) + ". Monte Carlo Volume: " + volume + " (" + elapsedTime + " s)" );
			volumes.add( volume );
		}

		System.out.println( "Variance: " + calc.variance( volumes ) );
	}
	
}
