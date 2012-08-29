package org.microtitan.diffusive.classloaders.factories;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassPool;

import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.classloaders.RestfulDiffuserClassLoader;

/**
 * Factory for creating {@link RestfulDiffuserClassLoader} objects. Use the {@code set(...)} methods to set the parameters
 * that the factory will use to create the {@link RestfulDiffuserClassLoader} object.
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserClassLoaderFactory implements ClassLoaderFactory {

	private static RestfulDiffuserClassLoaderFactory instance = null;
	
	private List< String > configClasses;
    private List< String > delegationPrefixes;
    private ClassLoader parentLoader; 
    private ClassPool classPool;
    
    /**
	 * Default no-arg constructor
	 */
	private RestfulDiffuserClassLoaderFactory()
	{
		/* disallow */ 
	}
	
	/**
	 * @return the {@link ClassLoaderFactoryRepository} instance (lazy instantiation)
	 */
	public static RestfulDiffuserClassLoaderFactory getInstance()
	{
		synchronized( RestfulDiffuserClassLoaderFactory.class )
		{
			if( instance == null )
			{
				// lazily create instance
				instance = new RestfulDiffuserClassLoaderFactory();
			}

			return instance;
		}
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param delegationPrefixes The list of prefixes to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public void set( final List< String > configClasses, final List< String > delegationPrefixes, final ClassLoader parent, final ClassPool pool )
	{
		this.configClasses = new ArrayList<>( configClasses );
		this.delegationPrefixes = new ArrayList<>( delegationPrefixes );
		this.parentLoader = parent;
		this.classPool = pool;
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public void set( final List< String > configClasses, final ClassLoader parent, final ClassPool pool )
	{
		this.configClasses = new ArrayList<>( configClasses );
		this.parentLoader = parent;
		this.classPool = pool;
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public void set( final ClassLoader parent, final ClassPool pool )
	{
		this.parentLoader = parent;
		this.classPool = pool;
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param classPool the source of the class files
	 */
	public void set( final List< String > configClasses, final ClassPool pool )
	{
		this.configClasses = new ArrayList<>( configClasses );
		this.classPool = pool;
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param classPool the source of the class files
	 */
	public void set( final ClassPool pool )
	{
		this.classPool = pool;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory#create(java.util.List)
	 */
	@Override
	public RestfulDiffuserClassLoader create( final List< URI > classPaths )
	{
		RestfulDiffuserClassLoader loader = null;
		if( ( configClasses != null && !configClasses.isEmpty() ) &&
			( delegationPrefixes != null && !delegationPrefixes.isEmpty() ) &&
			parentLoader != null &&
			classPool != null )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, configClasses, delegationPrefixes, parentLoader, classPool );
		}
		else
		if( ( configClasses == null || configClasses.isEmpty() ) &&
			( delegationPrefixes == null || delegationPrefixes.isEmpty() ) &&
			parentLoader == null &&
			classPool == null )
		{
			// the parent class loader should be the class loader that loaded the RestfulDiffuserClassLoader
			final ClassLoader defaultParentLoader = RestfulDiffuserClassLoader.class.getClassLoader();

			// get the default class pool
			final ClassPool pool = ClassPool.getDefault();

			loader = new RestfulDiffuserClassLoader( classPaths, defaultParentLoader, pool );
		}
		else
		if( ( configClasses != null && !configClasses.isEmpty() ) &&
			( delegationPrefixes == null || delegationPrefixes.isEmpty() ) &&
			parentLoader != null &&
			classPool != null )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, configClasses, parentLoader, classPool );
		}
		else
		if( ( configClasses == null || configClasses.isEmpty() ) &&
			( delegationPrefixes == null || delegationPrefixes.isEmpty() ) &&
			parentLoader != null &&
			classPool != null )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, parentLoader, classPool );
		}
		else
		if( ( configClasses != null && !configClasses.isEmpty() ) &&
			( delegationPrefixes == null || delegationPrefixes.isEmpty() ) &&
			parentLoader == null &&
			classPool != null )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, configClasses, classPool );
		}
		else
		if( ( configClasses == null || configClasses.isEmpty() ) &&
			( delegationPrefixes == null || delegationPrefixes.isEmpty() ) &&
			parentLoader == null &&
			classPool != null )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, classPool );
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Invalid factory configuration: should never reach this point!" );
			throw new IllegalStateException( message.toString() );
		}
		
		return loader;
	}
}
