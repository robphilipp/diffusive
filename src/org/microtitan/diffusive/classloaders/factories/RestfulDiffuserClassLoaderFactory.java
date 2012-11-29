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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.expr.ExprEditor;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.classloaders.RestfulDiffuserClassLoader;
import org.microtitan.diffusive.convertor.MethodIntercepterEditor;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.translator.BasicDiffusiveTranslator;
import org.microtitan.diffusive.translator.DiffusiveTranslator;

/**
 * Factory for creating {@link RestfulDiffuserClassLoader} objects. Use the {@code set(...)} methods to set the parameters
 * that the factory will use to create the {@link RestfulDiffuserClassLoader} object.
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserClassLoaderFactory implements ClassLoaderFactory {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserClassLoaderFactory.class );
	
	private static RestfulDiffuserClassLoaderFactory instance = null;
	
	private Map< String, Object[] > configClasses;
	private List< String > delegationPrefixes;
	private ClassPool classPool;
    
	/**
	 * Default no-arg constructor. Sets a few default values for the
	 * {@link DiffusiveTranslator}, the {@link ExprEditor}, the parent class
	 * loader, and the class pool.
	 */
	private RestfulDiffuserClassLoaderFactory()
	{
		// gets the default class pool
		this.classPool = ClassPool.getDefault();
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
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param delegationPrefixes The list of prefixes to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public void set( final Map< String, Object[] > configClasses, final List< String > delegationPrefixes, final ClassPool pool )
	{
		this.configClasses = new LinkedHashMap<>( configClasses );
		this.delegationPrefixes = new ArrayList<>( delegationPrefixes );
		this.classPool = pool;
	}
	
	/**
	 * Sets the specified parameters. None of the parameter objects should be null, and none of the lists should be empty. 
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param classPool the source of the class files
	 */
	public void set( final Map< String, Object[] > configClasses, final ClassPool pool )
	{
		this.configClasses = new LinkedHashMap<>( configClasses );
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
	
	/**
	 * Creates the default translator for Javassist to replace method calls from diffused methods
	 * @param expressionEditor The expression editor containing the code that replaces the method call
	 * @return A new translator for rewriting method calls to diffused methods
	 */
	private static DiffusiveTranslator createDefaultTranslator( final MethodIntercepterEditor expressionEditor )
	{
		return new BasicDiffusiveTranslator( expressionEditor );
	}
	
	/**
	 * Creates a default method intercepter using the specified {@link Diffuser}
	 * @param signature The base signature of the associated diffuser. Method calls with this signature won't be
	 * diffused further.
	 * @return creates and returns a {@link MethodIntercepterEditor} with a local {@link Diffuser}
	 */
	private static MethodIntercepterEditor createDefaultMethodIntercepter( final String signature )
	{
		return new MethodIntercepterEditor( signature );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory#create(java.lang.ClassLoader, java.lang.String, java.util.List)
	 */
	@Override
	public RestfulDiffuserClassLoader create( final ClassLoader parentLoader, final String signature, final List< URI > classPaths )
	{
		RestfulDiffuserClassLoader loader = null;
		// everything is set
		if( configClasses != null && !configClasses.isEmpty() && delegationPrefixes != null && !delegationPrefixes.isEmpty() )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, configClasses, delegationPrefixes, parentLoader, classPool );
		}
		else
		// nothing is set
		if( ( configClasses == null || configClasses.isEmpty() ) && ( delegationPrefixes == null || delegationPrefixes.isEmpty() ) ) 
		{
			loader = new RestfulDiffuserClassLoader( classPaths, parentLoader, classPool );
		}
		else
		// everything except delegation prefixes are set
		if( ( configClasses != null && !configClasses.isEmpty() ) && ( delegationPrefixes == null || delegationPrefixes.isEmpty() ) )
		{
			loader = new RestfulDiffuserClassLoader( classPaths, configClasses, parentLoader, classPool );
		}
		// invalid configuration
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Invalid factory configuration: should never reach this point!" );
			throw new IllegalStateException( message.toString() );
		}
		
		// set up the class loader with the translator
		try
		{
			loader.addTranslator( classPool, createDefaultTranslator( createDefaultMethodIntercepter( signature ) ) );
		}
		catch( Throwable exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error loading the specified class" + Constants.NEW_LINE );
			message.append( "  Loader: " + loader.getClass().getName() + Constants.NEW_LINE );

			LOGGER.error( message.toString(), exception );
			throw new IllegalArgumentException( message.toString(), exception );
		}

		return loader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.factories.ClassLoaderFactory#create(java.util.List)
	 */
	@Override
	public RestfulDiffuserClassLoader create( final String signature, final List< URI > classPaths )
	{
		return create( RestfulDiffuserClassLoader.class.getClassLoader(), signature, classPaths );
	}
}
