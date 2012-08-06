package org.microtitan.diffusive.diffuser.strategy.load;

import java.util.concurrent.ExecutorService;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache;

/**
 * Calculates the load based on the number of running tasks (in the specified cache) divided
 * by the number of threads available in the thread-pool (usually for the {@link ExecutorService}.
 * The idea is that the number of threads in the thread pool is specified to match the performance
 * of the machine on which they are being run.
 * 
 * @author Robert Philipp
 */
public class TaskThreadLoad implements DiffuserLoad {
	
	private final long numThreads;
	private final ResultsCache cache;
	
	/**
	 * Constructs a load calculator based on the maximum number of threads in the thread pool
	 * and the number of running tasks in the cache.
	 * @param numThreads The maximum number of threads in the thread pool.
	 * @param cache The cache holding the task futures
	 */
	public TaskThreadLoad( final long numThreads, final ResultsCache cache )
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
