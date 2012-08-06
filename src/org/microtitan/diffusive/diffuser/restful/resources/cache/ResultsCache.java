package org.microtitan.diffusive.diffuser.restful.resources.cache;

import org.microtitan.diffusive.cache.Cache;

/**
 * Cache for execution results. The keys are {@link String}s and the entry are {@link ResultCacheEntry}s
 * with an {@link Object} parameter type.
 * 
 * @author Robert Philipp
 */
public interface ResultsCache extends Cache< String, ResultCacheEntry< Object > > {

	/**
	 * Returns true if the task is still running; false otherwise
	 * @param signature The signature of the task
	 * @param requestId The ID associated with the request
	 * @return true if the task is still running; false otherwise
	 */
	boolean isRunning( final String key );
	
	/**
	 * @return The number of tasks in this cache that are currently running.
	 */
	long getNumRunning();
}
