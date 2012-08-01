package org.microtitan.diffusive.diffuser.restful.resources.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.diffuser.restful.resources.ResultId;

/**
 * Basic cache for holding execution results. Manages the items cached, but doesn't know the details
 * of the cache entries. Those details are left to the CacheEntry object.
 * 
 * @author Rob
 *
 * @param <T> Cache entry object
 */
public class BasicResultsCache< T > implements ResultsCache< T > {

	private static final Logger LOGGER = Logger.getLogger( BasicResultsCache.class );
	
	private static final int MAX_RESULTS = 100;
	
	// fields to manage the resultsCache cache
	private final Map< String, T > resultsCache;
	private int maxResultsCached;
	
	/**
	 * Constructs a basic cache that holds the specified number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * @param maxResults The maximum number of cache entries allowed.
	 */
	public BasicResultsCache( final int maxResults )
	{
		resultsCache = new LinkedHashMap<>();
		maxResultsCached = maxResults;
	}
	
	/**
	 * Constructs a basic cache that holds the default number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * 
	 * The maximum number of cached items ({@link #MAX_RESULTS}) defaults to a value of {@value #MAX_RESULTS}.
	 */
	public BasicResultsCache()
	{
		this( MAX_RESULTS );
	}
	
	/**
	 * Adds a result to the resultsCache cache. If the addition of this result causes the
	 * number of cached items to increase beyond the maximum allowable items, then the
	 * item that was first added is removed.
	 * @param key The key for the cache
	 * @param cacheEntry The result entry holding the result object and the serializer used to
	 * serialize and deserialize it
	 * @return the entry that was previously stored in the cache with this key
	 */
	public synchronized T cache( final String key, final T cacheEntry )
	{
		// put the new result to the cache
		final T previousResults = resultsCache.put( key, cacheEntry );
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Cached result" + Constants.NEW_LINE );
			message.append( "  Cache Key: " + key.toString() + Constants.NEW_LINE );
			message.append( "  Cache Entry: " + Constants.NEW_LINE );
			message.append( cacheEntry.toString() );
			LOGGER.info( message.toString() );
		}
		
		if( previousResults == null && resultsCache.size() > maxResultsCached )
		{
			final Iterator< Map.Entry< String, T > > iter = resultsCache.entrySet().iterator();
			resultsCache.remove( iter.next().getKey() );
		}
		return previousResults;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#getResultFromCache(java.lang.String)
	 */
	@Override
	public T get( String key )
	{
		return resultsCache.get( key );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#isResultCached(java.lang.String)
	 */
	@Override
	public synchronized boolean isCached( final String key )
	{
		return resultsCache.containsKey( key );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#remove(org.microtitan.diffusive.diffuser.restful.resources.ResultId)
	 */
	@Override
	public void remove( ResultId key )
	{
		resultsCache.remove( key );
	}
}
