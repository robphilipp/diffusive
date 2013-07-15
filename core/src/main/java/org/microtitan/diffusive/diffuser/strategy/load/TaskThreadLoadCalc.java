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
package org.microtitan.diffusive.diffuser.strategy.load;

import java.util.concurrent.ExecutorService;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.resources.cache.ResultsCache;

/**
 * Calculates the load based on the number of running tasks (in the specified cache) divided
 * by the number of threads available in the thread-pool (usually for the {@link ExecutorService}.
 * The idea is that the number of threads in the thread pool is specified to match the performance
 * of the machine on which they are being run.
 * 
 * @author Robert Philipp
 */
public class TaskThreadLoadCalc implements DiffuserLoadCalc {
	
	private final long numThreads;
	private final ResultsCache cache;
	
	/**
	 * Constructs a load calculator based on the maximum number of threads in the thread pool
	 * and the number of running tasks in the cache.
	 * @param numThreads The maximum number of threads in the thread pool.
	 * @param cache The cache holding the task futures
	 */
	public TaskThreadLoadCalc( final long numThreads, final ResultsCache cache )
	{
		if( numThreads <= 0 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Number of threads must be greater than zero" + Constants.NEW_LINE );
			message.append( "  Specified Number of Threads: " + numThreads );
			throw new IllegalArgumentException( message.toString() );
		}
		this.numThreads = numThreads;
		this.cache = cache;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoad#getLoad()
	 */
	@Override
	public double getLoad()
	{
		final long numRunningTasks = cache.getNumRunning();
		return numRunningTasks / (double)numThreads;
	}

}
