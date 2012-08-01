package org.microtitan.diffusive.tests.threaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiThreadedCalc {

	public static void main( String[] args )
	{
		final ExecutorService executor = Executors.newFixedThreadPool( 4 );
		
		final Random random = new Random( 1 );
		
		// create the list of tasks
		final List< Task > tasks = new ArrayList<>();
		for( int i = 0; i < 20; ++i )
		{
			tasks.add( new Task( i, (int)(random.nextFloat() * 25_000 ) ) );
		}
		
		final long start = System.currentTimeMillis();

		// create the completion service that allows us to poll for the results
		final CompletionService< Result > completionService = new ExecutorCompletionService<>( executor );
		for( final Task task : tasks )
		{
			completionService.submit( task );
		}
		
		// grab the results as they complete, and print them out to the console
		try
		{
			final int numTasks = tasks.size();
			for( int t = 0; t < numTasks; t++ )
			{
				final Future< Result > resultFuture = completionService.take();
				final Result result = resultFuture.get();
				System.out.println( "(" + (t+1) + "/" + numTasks + ") id=" + result.getId() + ": loops=" + result.getNumLoops() + " => " + result.getResult() );
			}
		}
		catch( InterruptedException e )
		{
			Thread.currentThread().interrupt();
		}
		catch( ExecutionException e )
		{
			throw new IllegalStateException( e );
		}
		finally
		{
			System.out.println( "done: " + (double)(System.currentTimeMillis() - start)/1000 + " s" );

			// done, shutdown the thread pool (program won't exit until the thread pool resources are shut down.
			System.out.println( "Shutting down the threads" );
			executor.shutdown();
		}
	}
}
