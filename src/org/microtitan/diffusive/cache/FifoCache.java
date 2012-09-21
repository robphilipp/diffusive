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
package org.microtitan.diffusive.cache;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;

/**
 * Cache that holds a maximum specified elements. When the maximum number of elements is
 * exceeded, then the cache removes the oldest element. In other words, this is first-in, first-out
 * (FIFO) cache.
 * 
 * @author Robert Philipp
 *
 * @param <T> Cache entry object
 */
public class FifoCache< K, T > implements Cache< K, T > {

	private static final Logger LOGGER = Logger.getLogger( FifoCache.class );
	
	private static final int MAX_ITEMS = 100;
	
	// fields to manage the resultsCache cache
	private final Map< K, T > cache;
	private int maxCachedItems;
	
	/**
	 * Constructs a basic cache that holds the specified number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * @param maxResults The maximum number of cache entries allowed.
	 */
	public FifoCache( final int maxResults )
	{
		cache = new LinkedHashMap<>();	// WARNING: must be a LINKED hash map...FIFO cache!
		maxCachedItems = maxResults;
	}
	
	/**
	 * Constructs a basic cache that holds the default number of entries. When an item is added, and
	 * that causes the number of items to exceed the maximum number of items, the oldest item is dropped.
	 * 
	 * The maximum number of cached items ({@link #MAX_ITEMS}) defaults to a value of {@value #MAX_ITEMS}.
	 */
	public FifoCache()
	{
		this( MAX_ITEMS );
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
	public synchronized T add( final K key, final T cacheEntry )
	{
		// put the new result to the cache
		final T previousResults = cache.put( key, cacheEntry );
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Cached result" + Constants.NEW_LINE );
			message.append( "  Cache Key: " + key.toString() + Constants.NEW_LINE );
			message.append( "  Cache Entry: " + Constants.NEW_LINE );
			message.append( cacheEntry.toString() );
			LOGGER.info( message.toString() );
		}
		
		if( previousResults == null && cache.size() > maxCachedItems )
		{
			final Iterator< Map.Entry< K, T > > iter = cache.entrySet().iterator();
			cache.remove( iter.next().getKey() );
		}
		return previousResults;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#getResultFromCache(java.lang.String)
	 */
	@Override
	public synchronized T get( final K key )
	{
		return cache.get( key );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.cache.Cache#getKeys()
	 */
	@Override
	public synchronized Set< K > getKeys()
	{
		return Collections.unmodifiableSet( cache.keySet() );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#isResultCached(java.lang.String)
	 */
	@Override
	public synchronized boolean isCached( final K key )
	{
		return cache.containsKey( key );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultsCache#remove(org.microtitan.diffusive.diffuser.restful.resources.ResultId)
	 */
	@Override
	public synchronized void remove( final K key )
	{
		cache.remove( key );
	}
}
