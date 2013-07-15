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
package org.microtitan.diffusive.diffuser.resources.cache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

/**
 * The entry into the results cache. Each entry holds the results object and the
 * serializer name used for serializing and deserializing the result object.
 * 
 * @see org.microtitan.diffusive.diffuser.resources.cache.ResultsCache
 * 
 * @author Robert Philipp
 */
public class ResultCacheEntry< T > {

	private final Future< T > result;
	private final String serializerType;

	/**
	 * Constructs the entry for the {@link org.microtitan.diffusive.diffuser.resources.cache.ResultsCache}
	 * @param result The {@link java.util.concurrent.Future} that can be queried for the result.
	 * @param serializerType The name of the serializer used to serialize/deserialize
	 * the result object (see {@link org.microtitan.diffusive.diffuser.serializer.SerializerFactory.SerializerType} for a list
	 * of available serializers).
	 */
	public ResultCacheEntry( final Future< T > result, final String serializerType )
	{
		this.result = result;
		this.serializerType = serializerType;
	}

	/**
	 * Constructs the entry for the {@link org.microtitan.diffusive.diffuser.resources.cache.ResultsCache}
	 * @param result The {@link java.util.concurrent.Future} that can be queried for the result.
	 * @param serializer The serializer used to serialize/deserialize
	 * the result object (see {@link org.microtitan.diffusive.diffuser.serializer.SerializerFactory} for a list
	 * of available serializers).
	 */
	public ResultCacheEntry( final Future< T > result, final Serializer serializer )
	{
		this( result, SerializerFactory.getSerializerName( serializer.getClass() ) );
	}

	/**
	 * @return The name of the serializer used serialize/deserialize the result object
	 */
	public String getSerializerType()
	{
		return serializerType;
	}

	/**
	 * This is a blocking call to request the result object
	 * @return The result object
	 * @throws InterruptedException
	 * @throws java.util.concurrent.ExecutionException
	 * @see java.util.concurrent.Future
	 * @see java.util.concurrent.ExecutorService
	 */
	public T getResult() throws InterruptedException, ExecutionException
	{
		return result.get();
	}

	/**
	 * @return true if the task has completed; false otherwise
	 */
	public boolean isDone()
	{
		return result.isDone();
	}
}
