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
public class ResultCacheEntry< T > {

	private final Future< T > result;
	private final String serializerType;

	public ResultCacheEntry( final Future< T > result, final String serializerType )
	{
		this.result = result;
		this.serializerType = serializerType;
	}

	public ResultCacheEntry( final Future< T > result, final Serializer serializer )
	{
		this( result, SerializerFactory.getSerializerName( serializer.getClass() ) );
	}

	public String getSerializerType()
	{
		return serializerType;
	}

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

	public boolean isDone()
	{
		return result.isDone();
	}

	// public boolean isCancelled()
	// {
	// return result.isCancelled();
	// }
}