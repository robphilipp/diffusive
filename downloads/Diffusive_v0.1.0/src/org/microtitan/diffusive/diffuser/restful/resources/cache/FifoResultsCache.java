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
package org.microtitan.diffusive.diffuser.restful.resources.cache;

import org.microtitan.diffusive.cache.FifoCache;

/**
 * Basic cache for holding execution results. Manages the items cached, but doesn't know the details
 * of the cache entries. Those details are left to the CacheEntry object.
 * 
 * @author Rob
 *
 * @param <T> Cache entry object
 */
public class FifoResultsCache extends FifoCache< String, ResultCacheEntry< Object > > implements ResultsCache {

//	private static final Logger LOGGER = Logger.getLogger( BasicResultsCache.class );
	
	private static final int MAX_RESULTS = 100;
	
	/**
	 * Constructs a basic cache that holds the specified number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * @param maxResults The maximum number of cache entries allowed.
	 */
	public FifoResultsCache( final int maxResults )
	{
		super( maxResults );
	}
	
	/**
	 * Constructs a basic cache that holds the default number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * 
	 * The maximum number of cached items ({@link #MAX_RESULTS}) defaults to a value of {@value #MAX_RESULTS}.
	 */
	public FifoResultsCache()
	{
		this( MAX_RESULTS );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache#isRunning(java.lang.String)
	 */
	@Override
	public synchronized boolean isRunning( final String key )
	{
		boolean isRunning = false;
		final ResultCacheEntry< Object > entry = get( key );
		if( entry != null )
		{
			isRunning = !entry.isDone();
		}
		return isRunning;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache#numRunning()
	 */
	@Override
	public synchronized long getNumRunning()
	{
		long numRunning = 0;
		for( final String key : getKeys() )
		{
			if( isRunning( key ) )
			{
				numRunning++;
			}
		}
		return numRunning;
	}
}
