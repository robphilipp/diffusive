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

import org.microtitan.diffusive.classloaders.RestfulClassLoader;

import java.net.URI;
import java.util.List;

/**
 * Factory for creating RESTful class loaders. These are class loaders that go against a RESTful
 * web service to retrieve a stream of bytes representing the {@link Class} object, and then 
 * construct and load the {@link Class} object.
 *  
 * @author Robert Philipp
 */
public class RestfulClassLoaderFactory implements ClassLoaderFactory {

	private static RestfulClassLoaderFactory instance = null;
	
	/**
	 * Default no-arg constructor
	 */
	private RestfulClassLoaderFactory() { /* disallow */ }
	
	/**
	 * @return the {@link ClassLoaderFactoryRepository} instance (lazy instantiation)
	 */
	public static RestfulClassLoaderFactory getInstance()
	{
		synchronized( RestfulClassLoaderFactory.class )
		{
			if( instance == null )
			{
				// lazily create instance
				instance = new RestfulClassLoaderFactory();
			}

			return instance;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory#create(java.util.List)
	 */
	@Override
	public RestfulClassLoader create( final String baseSignature, final List< URI > classPaths )
	{
		return new RestfulClassLoader( classPaths );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory#create(java.lang.ClassLoader, java.lang.String, java.util.List)
	 */
	@Override
	public RestfulClassLoader create( final ClassLoader parentLoader, final String baseSignature, final List< URI > classPaths )
	{
		return new RestfulClassLoader( classPaths, parentLoader );
	}

}
