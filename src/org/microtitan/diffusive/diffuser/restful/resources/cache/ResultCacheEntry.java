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

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

/**
 * The entry into the results cache. Each entry holds the results object and the
 * serializer name used for serializing and deserializing the result object.
 * 
 * @author Robert Philipp
 */
public class ResultCacheEntry< T > {
	
	private static final Logger LOGGER = Logger.getLogger( ResultCacheEntry.class );

	private final Future< T > result;
	private final String serializerType;

	/**
	 * Constructs an entry for the results cache that holds the result and the serializer used
	 * to serialize and deserialize the object.
	 * @param result The {@link Future} object from which to retrieve the result
	 * @param serializerType The serializer type as specified in the {@link SerializerFactory}
	 */
	public ResultCacheEntry( final Future< T > result, final String serializerType )
	{
		this.result = result;
		this.serializerType = serializerType;
	}

	/**
	 * Constructs an entry for the results cache that holds the result and the serializer used
	 * to serialize and deserialize the object.
	 * @param result The {@link Future} object from which to retrieve the result
	 * @param serializer The serializer used to serialize/de-serialize the results object.
	 */
	public ResultCacheEntry( final Future< T > result, final Serializer serializer )
	{
		this( result, SerializerFactory.getSerializerName( serializer.getClass() ) );
	}

	/**
	 * @return The serializer type as specified in the {@link SerializerFactory}
	 */
	public String getSerializerType()
	{
		return serializerType;
	}

	/**
	 * Blocking call that returns the result object held in the {@link Future} object.
	 * @return the result object held in the {@link Future} object.
	 */
	public T getResult()
	{
		T resultObject = null;
		try
		{
			// blocking call
			resultObject = result.get();
		}
		catch( InterruptedException e )
		{
			Thread.currentThread().interrupt();
		}
		catch( ExecutionException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error executing the diffusive method." + Constants.NEW_LINE );
			message.append( "  Future: " + result.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Serializer Type: " + serializerType );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}
		return resultObject;
	}

	/**
	 * @return true if the task has completed; false otherwise
	 */
	public boolean isDone()
	{
		return result.isDone();
	}
}
