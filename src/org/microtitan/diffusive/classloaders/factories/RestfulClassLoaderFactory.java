package org.microtitan.diffusive.classloaders.factories;

import java.net.URI;
import java.util.List;

import org.microtitan.diffusive.classloaders.RestfulClassLoader;

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
	public RestfulClassLoader create( final List< URI > classPaths )
	{
		return new RestfulClassLoader( classPaths );
	}

}
