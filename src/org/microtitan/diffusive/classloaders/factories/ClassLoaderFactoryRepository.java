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
package org.microtitan.diffusive.classloaders.factories;

import java.util.HashMap;
import java.util.Map;

/**
 * Constructs the {@link ClassLoader} factory repository. The repository contains {@link ClassLoaderFactory} object used
 * for loading classes. This is intended for class loaders that work over the network.
 * 
 * @author Robert Philipp
 */
public class ClassLoaderFactoryRepository {

	private static ClassLoaderFactoryRepository instance = null;
	private Map< String, ClassLoaderFactory > loaders;
	
	private ClassLoaderFactoryRepository()
	{
		loaders = new HashMap<>();
	}
	
	/**
	 * @return the {@link ClassLoaderFactoryRepository} instance (lazy instantiation)
	 */
	public static ClassLoaderFactoryRepository getInstance()
	{
		synchronized( instance )
		{
			if( instance == null )
			{
				// lazily create instance
				instance = new ClassLoaderFactoryRepository();
			}

			return instance;
		}
	}

	/**
	 * Adds a {@link ClassLoaderFactory} to the list of factories.
	 * @param loaderType The {@link ClassLoader} type
	 * @param factory The {@link ClassLoaderFactory} responsible for creating the specified type
	 * @return The {@link ClassLoaderFactory} that was previously associated with the specified type;
	 * or null if no {@link ClassLoaderFactory} was associated with the specified type
	 */
	public synchronized ClassLoaderFactory add( final Class< ? extends ClassLoader > loaderType, 
												final ClassLoaderFactory factory )
	{
		return loaders.put( loaderType.getName(), factory );
	}
	
	/**
	 * Removes the class loader associated with the specified class-loader type.
	 * @param loaderType The {@link ClassLoader} type
	 * @return The {@link ClassLoaderFactory} that was removed
	 */
	public synchronized ClassLoaderFactory remove( final Class< ? extends ClassLoader > loaderType )
	{
		return loaders.remove( loaderType.getName() );
	}
	
	/**
	 * Returns the {@link ClassLoaderFactory} associated with the specified {@link ClassLoaderFactory} type
	 * @param loaderType The {@link ClassLoader} type
	 * @param loaderType 
	 * @return the {@link ClassLoaderFactory} associated with the specified {@link ClassLoaderFactory} type
	 */
	public ClassLoaderFactory get( final Class< ? extends ClassLoader > loaderType )
	{
		return loaders.get( loaderType.getName() );
	}
}
