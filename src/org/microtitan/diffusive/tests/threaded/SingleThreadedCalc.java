package org.microtitan.diffusive.tests.threaded;

import java.util.ArrayList;
import java.util.List;

public class SingleThreadedCalc {

	public static void main( String[] args )
	{
		// create the list of tasks
		final List< Task > tasks = new ArrayList<>();
		for( int i = 0; i < 20; ++i )
		{
			tasks.add( new Task( i, (int)(Math.random() * 25_000 ) ) );
		}
		
		// calculate each result and print it to the console
		final int numTasks = tasks.size();
		int t = 0;
		for( final Task task : tasks )
		{
			final Result result = task.call();
			System.out.println( "(" + (++t) + "/" + numTasks + ") id=" + result.getId() + ": loops=" + result.getNumLoops() + " => " + result.getResult() );
		}
	}
}
