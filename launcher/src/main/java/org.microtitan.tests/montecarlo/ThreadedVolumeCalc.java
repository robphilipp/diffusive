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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;

/**
 * Monte Carlo volume calculator for {@link org.microtitan.tests.montecarlo.Cube} objects that threads the calculation
 *  
 * @author Robert Philipp
 */
public class ThreadedVolumeCalc extends VolumeCalc {

	private static final Logger LOGGER = Logger.getLogger( ThreadedVolumeCalc.class );
	
	private final int numThreads;

	/**
	 * Constructor that takes a {@link org.microtitan.tests.montecarlo.Cube} for which to calculate the volume, and a cube that
	 * completely contains it, and for which the volume is known.
	 * @param cube The cube for which to calculate the volume
	 * @param boundingShape The cube of known volume that contains the cube for which to calculate the volume
	 * @param numThreads The maximum number of threads running
	 */
	public ThreadedVolumeCalc( final Cube cube, final Cube boundingShape, final int numThreads )
	{
		super( cube, boundingShape );
		
		this.numThreads = numThreads;
	}

	/**
	 * Constructor that takes a {@link org.microtitan.tests.montecarlo.Cube} for which to calculate the volume, and a cube that
	 * completely contains it, and for which the volume is known.
	 * @param cube The cube for which to calculate the volume
	 * @param boundingShape The cube of known volume that contains the cube for which to calculate the volume
	 */
	public ThreadedVolumeCalc( final Cube cube, final Cube boundingShape )
	{
		this( cube, boundingShape, Integer.MAX_VALUE );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.tests.montecarlo.VolumeCalc#calcVolumes(int, long)
	 */
	@Override
	public List< Double > calcVolumes( final int numSimulations, final long maxIterations )
	{
		// create the executor service to which we submit our calculations
		final ExecutorService executor = Executors.newFixedThreadPool( Math.min( numThreads, Math.min( numSimulations, 1000 ) ) );
		final CompletionService< Double > completionService = new ExecutorCompletionService<>( executor );

		// submit the tasks to the execution service. notice that we need to wrap our tasks in a
		// Callable< return_type > object so that the executor can call it.
		for( int i = 0; i < numSimulations; ++i )
		{
			final long iteration = i;	// the iteration number must be final to be seen by inner class
			completionService.submit( new Callable< Double >() {
				/*
				 * (non-Javadoc)
				 * @see java.util.concurrent.Callable#call()
				 */
				@Override
				public Double call() throws Exception
				{
					return ThreadedVolumeCalc.this.calcVolume( iteration, maxIterations );
				}
			} );
		}

		// wait for the tasks to complete and then add the result to the list of volumes
		final List< Double > volumes = new ArrayList<>();
		try
		{
			for( int i = 0; i < numSimulations; i++ )
			{
				final Double result = completionService.take().get();
				if( result != null )
				{
					volumes.add( result );
				}
				else
				{
					LOGGER.error( "Failed task" );
				}
			}
		}
		catch( InterruptedException e )
		{
			Thread.currentThread().interrupt();
		}
		catch( ExecutionException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error: volumes=" + Constants.NEW_LINE );
			for( double volume : volumes )
			{
				message.append( "  " + volume );
			}
			throw new IllegalStateException( message.toString(), e );
		}
		finally
		{
			// done, shutdown the thread pool (program won't exit until the thread pool resources are shut down.
			executor.shutdown();
		}
		return volumes;
	}

	public static void main( final String...args )
	{
		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		final Cube cube = new Cube( 2.0, 2.0, 2.0, 2.0 );
		
		final ThreadedVolumeCalc calc = new ThreadedVolumeCalc( cube, new Cube( 4.0, 4.0, 4.0, 4.0 ), 50 );

		final long start = System.currentTimeMillis();
		final List< Double > volumes = calc.calcVolumes( 10, 10_000_000 );
		final double elapsedTime = (double)(System.currentTimeMillis() - start) / 1000;

		System.out.println( "Volume: " + mean( volumes ) + " +- " + variance( volumes ) + " (n=" + volumes.size() + "; " + elapsedTime + " s)" );
	}

}
