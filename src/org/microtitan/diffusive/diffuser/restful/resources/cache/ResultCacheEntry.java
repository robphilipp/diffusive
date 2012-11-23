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

	public T getResult() throws InterruptedException, ExecutionException
	{
//		T resultObject = null;
//		try
//		{
//			resultObject = result.get();
//		}
//		catch( InterruptedException e )
//		{
//			Thread.currentThread().interrupt();
//		}
//		catch( ExecutionException e )
//		{
//			throw new IllegalStateException( e );
//		}
//		return resultObject;
		return result.get();
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
