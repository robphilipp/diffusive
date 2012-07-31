package org.microtitan.diffusive.diffuser.restful.resources;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.diffuser.restful.request.ExecuteDiffuserRequest;

public class BasicResultsCache< T > {

	private static final Logger LOGGER = Logger.getLogger( BasicResultsCache.class );
	
	private static final int MAX_RESULTS = 100;
	
	// fields to manage the resultsCache cache
	private final Map< String, T > resultsCache;
	private int maxResultsCached;
	
	public BasicResultsCache( final int maxResults )
	{
		resultsCache = new LinkedHashMap<>();
		maxResultsCached = maxResults;
	}
	
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
	public synchronized T cacheResults( final ResultId key, final T cacheEntry )
	{
		// put the new result to the cache
		final T previousResults = resultsCache.put( key.getResultId(), cacheEntry );
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

	/**
	 * Returns the result from the {@link #resultsCache} by generating the cache key from the
	 * specified signature and request ID
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return the result from the {@link #resultsCache} associated with the specified signature 
	 * and request ID 
	 */
	public synchronized T getResultFromCache( final String signature, final String requestId )
	{
		return resultsCache.get( createResultsCacheId( signature, requestId ) );
	}
	
	/**
	 * Returns true if the specified key is contained in the results cache; false otherwise
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return true if the specified key is contained in the results cache; false otherwise
	 */
	public synchronized boolean isResultCached( final String signature, final String requestId )
	{
		return resultsCache.containsKey( createResultsCacheId( signature, requestId ) );
	}
	
	/**
	 * Returns true if the specified key is contained in the results cache; false otherwise
	 * @param key The key to the results object
	 * @return true if the specified key is contained in the results cache; false otherwise
	 */
	public synchronized boolean isResultCached( final String key )
	{
		return resultsCache.containsKey( key );
	}
	
	/**
	 * Creates the results cache ID used as the key into the {@link #resultsCache} {@link Map}.
	 * @param signature The signature of the method that was executed
	 * @param requestId The request ID that was specified as part of the {@link ExecuteDiffuserRequest}
	 * @return The key for the {@link #resultsCache} {@link Map}.
	 */
	public static final String createResultsCacheId( final String signature, final String requestId )
	{
		return ResultId.create( signature, requestId );
	}
}
