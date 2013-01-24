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

import java.util.Set;



/**
 * Interface defining cache for holding execution results. 
 * 
 * Implementations are not intended to manage the cached items. Rather the user of the cache should supply
 * the cache entry objects and should hide the details from the cache.
 * 
 * @author Robert Philipp
 *
 * @param <K> The key to the cache entry
 * @param <E> Cache entry object
 */
public interface Cache< K, E > {

	/**
	 * Adds a result to the resultsCache cache. If the addition of this result causes the
	 * number of cached items to increase beyond the maximum allowable items, then the
	 * item that was first added is removed.
	 * @param key The key for the cache
	 * @param cacheEntry The entry associated with the cache key
	 * @return the entry that was previously stored in the cache with this key
	 */
	E add( final K key, final E cacheEntry );
	
	/**
	 * Returns the result from the {@link #resultsCache} by generating the cache key from the
	 * specified signature and request ID
	 * @param key The key for the cache
	 * @return the result from the {@link #resultsCache} associated with the specified signature 
	 * and request ID 
	 */
	E get( final K key );
	
	/**
	 * Returns true if the specified key is contained in the results cache; false otherwise
	 * @param key The key for the cache
	 * @return true if the specified key is contained in the results cache; false otherwise
	 */
	boolean isCached( final K key );

	/**
	 * Removes the entry with the specified key from the cache
	 * @param key The key for the cache
	 */
	void remove( final K key );
	
	/**
	 * @return the set of keys held in this cache
	 */
	Set< K > getKeys();
}
