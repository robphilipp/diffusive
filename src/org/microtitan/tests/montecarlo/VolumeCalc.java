/*
 * Copyright 2013 Robert Philipp
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
package org.microtitan.tests.montecarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.annotations.Diffusive;

/**
 * Monte Carlo volume calculator for {@link Cube} objects.
 *  
 * @author Robert Philipp
 */
public class VolumeCalc {

	private static final Logger LOGGER = Logger.getLogger( VolumeCalc.class );
	
	private final Cube cube;
	private final Cube boundingShape;
	
	/**
	 * Constructor that takes a {@link Cube} for which to calculate the volume, and a cube that
	 * completely contains it, and for which the volume is known.
	 * @param cube The cube for which to calculate the volume
	 * @param boundingShape The cube of known volume that contains the cube for which to calculate the volume
	 */
	public VolumeCalc( final Cube cube, final Cube boundingShape )
	{
		this.cube = cube;
		this.boundingShape = boundingShape;
	}

	/**
	 * Calculates the volume of the {@link #cube} using Monte Carlo.
	 * @param seed The seed for the random number generator
	 * @param maxIterations The maximum number of iterations
	 * @return The volume of the {@link #cube} from the simulation
	 */
	@Diffusive
	public double calcVolume( final Long seed, final Long maxIterations )
	{
		final long start = System.currentTimeMillis();

		// calculate the amount of space you want around the cube
		double volume = 0;
		if( boundingShape.isContained( cube ) )
		{
			long numInCube = 0;
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
			volume = boundingShape.getVolume() * (double)numInCube / maxIterations;
			if( LOGGER.isDebugEnabled() )
			{
				final double elapsedTime = (double)(System.currentTimeMillis() - start) / 1000;
				LOGGER.debug( "Monte Carlo Volume: " + volume + " (seed=" + seed + "; " + elapsedTime + " s)" );
			}
		}
		return volume;
	}
	
	/**
	 * Calculates the volume of the {@link #cube} a specified number of times, each time
	 * with a new random number seed and returns the volume for each iteration.
	 * @param numSimulations The number of times to calculate the volume
	 * @param maxIterations The maximum number of iterations for each calculation
	 * @return a list of volume
	 */
	public List< Double > calcVolumes( final int numSimulations, final long maxIterations )
	{
		final List< Double > volumes = new ArrayList<>();
		for( int i = 0; i < numSimulations; ++i )
		{
			volumes.add( calcVolume( (long)i, maxIterations ) );
		}
		return volumes;
	}
	
	/**
	 * Calculates the mean value for the specified list of numbers
	 * @param values The list of values.
	 * @return the mean value for the specified list of numbers
	 */
	public static double mean( final List< Double > values )
	{
		// calc the mean
		double mean = 0;
		for( double value : values )
		{
			mean += value;
		}
		return mean / values.size();
	}
	
	/**
	 * Calculates the variance value for the specified list of numbers
	 * @param values The list of values.
	 * @return the variance value for the specified list of numbers
	 */
	public static double variance( final List< Double > values )
	{
		final double mean = mean( values );
		
		double variance = 0;
		for( double value : values )
		{
			variance += ( value - mean ) * ( value - mean );
		}
		return variance / values.size();
	}
	
	public static void main( final String...args )
	{
		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		final Cube cube = new Cube( 2.0, 2.0, 2.0, 2.0 );
		
		final VolumeCalc calc = new VolumeCalc( cube, new Cube( 4.0, 4.0, 4.0, 4.0 ) );

		final long start = System.currentTimeMillis();
		final List< Double > volumes = calc.calcVolumes( 100, 10_000_000 );
		final double elapsedTime = (double)(System.currentTimeMillis() - start) / 1000;

		System.out.println( "Volume: " + mean( volumes ) + " +- " + variance( volumes ) + " (" + elapsedTime + " s)" );
	}
	
}
