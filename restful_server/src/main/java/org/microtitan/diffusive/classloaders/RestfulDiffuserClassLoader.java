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
package org.microtitan.diffusive.classloaders;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.Translator;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.diffuser.restful.atom.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.client.RestfulClientFactory;
import org.microtitan.diffusive.launcher.DiffusiveLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class loader that overrides the {@link #findClass(String)} method, using the {@link RestfulClassReader} to read a
 * byte stream of the {@link Class} object's data if the javassist class' {@link #findClass(String)} method can't
 * load the class.
 *
 * @author Robert Philipp
 */
public class RestfulDiffuserClassLoader extends DiffusiveLoader {
	
	private static Logger LOGGER = Logger.getLogger( RestfulDiffuserClassLoader.class );
	
	private static final String DELEGATION_PREFIX = "org.microtitan.diffusive.";

	private List< URI > classPaths;
	private final RestfulClassReader classReader;

	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param classPaths The {@link List} of class path URI
	 * @param configClasses A {@link Map} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need. 
	 * @param delegationPrefixes The list of prefixes to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths,
									   final Map< String, Object[] > configClasses,
									   final List< String > delegationPrefixes,
									   final ClassLoader parentLoader, 
									   final ClassPool classPool )
	{
		super( configClasses, delegationPrefixes, parentLoader, classPool );

		// we want all classes in the diffusive package to be loaded by the application (parent)
		// class loader and not by the javassist version
		addDelegationPrefix( DELEGATION_PREFIX );

		// the base URI of the resource
		this.classPaths = classPaths;

		// create the RESTful class data reader
		this.classReader = new RestfulClassReader( AbderaFactory.getInstance(), RestfulClientFactory.getInstance() );
	}

	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link Map} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths,
									   final Map< String, Object[] > configClasses,
									   final ClassLoader parentLoader, 
									   final ClassPool classPool )
	{
		this( classPaths, configClasses, createDefaultDelegationPrefixes(), parentLoader, classPool );
	}

	/**
     * Creates a new class loader using the specified parent class loader for delegation.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths, final ClassLoader parentLoader, final ClassPool classPool )
	{
		this( classPaths, new LinkedHashMap< String, Object[] >(), parentLoader, classPool );
	}

	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths, 
			   						   final Map< String, Object[] > configClasses,
									   final ClassPool classPool )
	{
		this( classPaths, configClasses, RestfulDiffuserClassLoader.class.getClassLoader(), classPool );
	}

	/**
     * Creates a new class loader.
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths, final ClassPool classPool )
	{
		this( classPaths, new LinkedHashMap< String, Object[] >(), classPool );
	}
		
	/**
     * Creates a new class loader with the specified class paths, and uses the default class pool.
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths )
	{
		this( classPaths, ClassPool.getDefault() );
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.RestfulClassLoader#findClass(java.lang.String)
	 */
	@Override
	public Class< ? > findClass( final String className ) throws ClassNotFoundException
	{
		Class< ? > clazz = null;
		try
		{
			clazz = super.findClass( className );
		}
		catch( ClassNotFoundException local )
		{
			// read the bytes from the network
			final byte[] bytes = classReader.readClassData( className, classPaths );
			if( bytes == null || bytes.length == 0 )
			{
				throw new ClassNotFoundException( className );
			}
			
			// the act of calling makeClass(...) on the class pool creates the CtClass object in the
			// class pool, and this allows the the parent Loader to pull out the class bytes the way
			// it normally does...
			final ClassPool classPool = getClassPool();
			final Translator translator = getTranslator();
			if( classPool != null )
			{
				try( final ByteArrayInputStream input = new ByteArrayInputStream( bytes ) )
				{
					classPool.makeClass( input );
				}
				catch( IOException e )
				{
					final String header = "Failed to add Class-object bytes to the class pool for modification.";
					final String message = createMessage( header, className, classPool, bytes );
					LOGGER.error( message, e );
					throw new IllegalStateException( message, e );
				}
				
				try
				{
					// modify the class file as it is loaded
					translator.onLoad( classPool, className );
	
					// grab the modified class from the class pool
					final byte[] classfile = classPool.get( className ).toBytecode();
					
					// define the class (actually load it into the JVM)
					clazz = defineClass( className, classfile, 0, classfile.length );
				}
				catch( CannotCompileException e )
				{
					final String header = "Failed to instrument diffusive method call.";
					final String message = createMessage( header, className, classPool, bytes );
					LOGGER.error( message, e );
					throw new IllegalStateException( message, e );
				}
				catch( NotFoundException | IOException e )
				{
					final String header = "Class not found in the class pool. This shouldn't happen.";
					final String message = createMessage( header, className, classPool, bytes );
					LOGGER.error( message, e );
					throw new ClassNotFoundException( message, e );
				}
			}
			
			if( LOGGER.isInfoEnabled() )
			{
				final String header = "Loaded class from remote source.";
				LOGGER.info( createMessage( header, className, classPool, bytes, classPaths ) );
			}
		}
		return clazz;
	}
	
	/**
	 * Constructs a message that contains the information about the class to be loaded.
	 * @param header The explanation of the message 
	 * @param className The name of the class being loaded
	 * @param pool The {@link ClassPool} that acts as the source of the class files
	 * @param bytesRead The bytes read from the remote location
	 * @return The message
	 */
	private static String createMessage( final String header, final String className, final ClassPool pool, final byte[] bytesRead )
	{
		final StringBuilder message = new StringBuilder();
		message.append( header ).append( Constants.NEW_LINE )
                .append("  Class Name: ").append( className ).append( Constants.NEW_LINE )
                .append("  Class Pool: ").append( pool.toString() ).append( Constants.NEW_LINE )
                .append("  Bytes Read: ").append( bytesRead.toString() );
		return message.toString();
	}
	
	/**
	 * Constructs a message that contains the information about the class to be loaded, and the class path
	 * from where it was loaded.
	 * @param header The explanation of the message 
	 * @param className The name of the class being loaded
	 * @param pool The {@link ClassPool} that acts as the source of the class files
	 * @param bytesRead The bytes read from the remote location
	 * @param classPaths The {@link URI} of the locations from which to load the class objects
	 * @return The message
	 */
	private static String createMessage( final String header,
                                         final String className,
                                         final ClassPool pool,
                                         final byte[] bytesRead,
                                         final List< URI > classPaths )
	{
		final StringBuilder message = new StringBuilder( createMessage( header, className, pool, bytesRead ) );
		message.append( "  Class Paths: " );
		for( URI uri : classPaths )
		{
			message.append( Constants.NEW_LINE ).append( "    " ).append( uri.toString() );
		}
		return message.toString();
	}

}
