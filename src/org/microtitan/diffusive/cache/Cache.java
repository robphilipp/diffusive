package org.microtitan.diffusive.cache;



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
}
