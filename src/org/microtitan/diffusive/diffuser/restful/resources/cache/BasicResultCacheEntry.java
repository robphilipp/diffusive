package org.microtitan.diffusive.diffuser.restful.resources.cache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

/**
 * The entry into the results cache. Each entry holds the results object and the
 * serializer name used for serializing and deserializing the result object.
 * 
 * @author Robert Philipp
 */
public class BasicResultCacheEntry< T > implements ResultCacheEntry< T > {

	private final Future< T > result;
	private final String serializerType;

	public BasicResultCacheEntry( final Future< T > result, final String serializerType )
	{
		this.result = result;
		this.serializerType = serializerType;
	}

	public BasicResultCacheEntry( final Future< T > result, final Serializer serializer )
	{
		this( result, SerializerFactory.getSerializerName( serializer.getClass() ) );
	}

	/* (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultCacheEntry#getSerializerType()
	 */
	@Override
	public String getSerializerType()
	{
		return serializerType;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultCacheEntry#getResult()
	 */
	@Override
	public T getResult()
	{
		T resultObject = null;
		try
		{
			resultObject = result.get();
		}
		catch( InterruptedException e )
		{
			Thread.currentThread().interrupt();
		}
		catch( ExecutionException e )
		{
			throw new IllegalStateException( e );
		}
		return resultObject;
	}

	/* (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.resources.ResultCacheEntry#isDone()
	 */
	@Override
	public boolean isDone()
	{
		return result.isDone();
	}

	// public boolean isCancelled()
	// {
	// return result.isCancelled();
	// }
}