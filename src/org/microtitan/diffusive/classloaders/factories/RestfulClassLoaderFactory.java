package org.microtitan.diffusive.classloaders.factories;

import java.net.URI;
import java.util.List;

import org.microtitan.diffusive.classloaders.RestfulClassLoader;

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

}
