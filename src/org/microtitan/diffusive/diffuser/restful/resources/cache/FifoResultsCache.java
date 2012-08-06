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
