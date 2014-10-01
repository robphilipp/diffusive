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
package org.microtitan.tests.threaded;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SingleThreadedCalc {

	public static void main( String[] args )
	{
		final Random random = new Random( 1 );
		
		// create the list of tasks
		final List< Task > tasks = new ArrayList<>();
		for( int i = 0; i < 20; ++i )
		{
			tasks.add( new Task( i, (int)(random.nextFloat() * 25_000 ) ) );
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
