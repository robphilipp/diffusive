package org.microtitan.diffusive.classloaders;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

public class RestfulDiffuserClassLoader extends DiffusiveLoader {
	
	private static Logger LOGGER = Logger.getLogger( RestfulDiffuserClassLoader.class );

	private List< URI > classPaths;
	private final RestfulClassReader classReader;

	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param classPaths The {@link List} of class path URI
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param delegationPrefixes The list of prefixes to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths,
									   final List< String > configClasses, 
									   final List< String > delegationPrefixes,
									   final ClassLoader parentLoader, 
									   final ClassPool classPool )
	{
		super( configClasses, delegationPrefixes, parentLoader, classPool );

		// the base URI of the resource
		this.classPaths = classPaths;

		// create the RESTful class data reader
		this.classReader = new RestfulClassReader( AbderaFactory.getInstance(), RestfulClientFactory.getInstance() );
	}

	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths,
									   final List< String > configClasses, 
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
		this( classPaths, new ArrayList< String >(), parentLoader, classPool );
	}

	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths, final List< String > configClasses, final ClassPool classPool )
	{
		this( classPaths, configClasses, RestfulDiffuserClassLoader.class.getClassLoader(), classPool );
	}

	/**
     * Creates a new class loader.
	 * @param classPool the source of the class files
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths, final ClassPool classPool )
	{
		this( classPaths, new ArrayList< String >(), classPool );
	}
		
	/**
     * Creates a new class loader with the specified class paths, and uses the default class pool.
	 */
	public RestfulDiffuserClassLoader( final List< URI > classPaths )
	{
		super();

		// the base URI of the resource
		this.classPaths = classPaths;

		// create the RESTful class data reader
		this.classReader = new RestfulClassReader( AbderaFactory.getInstance(), RestfulClientFactory.getInstance() );
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.classloaders.RestfulClassLoader#findClass(java.lang.String)
	 */
	@Override
	public Class< ? > findClass( String className ) throws ClassNotFoundException
	{
		// read the bytes from the network
		final byte[] bytes = classReader.readClassData( className, classPaths );
		if( bytes == null || bytes.length == 0 )
		{
			throw new ClassNotFoundException( className );
		}
		
		// attempt to instrument the class as it's loaded
		final ClassPool classPool = getClassPool();
		final Translator translator = getTranslator();
		try
		{
			if( classPool != null && translator != null )
			{
				translator.onLoad( classPool, className );
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Failed to instrument the diffusive method because the class pool or translator was null." + Constants.NEW_LINE );
				message.append( "  Class Name: " + className + Constants.NEW_LINE );
				if( classPool == null )
				{
					message.append( "  Class Pool: [null]" + Constants.NEW_LINE );
				}
				else
				{
					message.append( "  Class Pool: " + classPool.toString() + Constants.NEW_LINE );
				}
				if( translator == null )
				{
					message.append( "  Translator: [null]" + Constants.NEW_LINE );
				}
				else
				{
					message.append( "  Translator: " + translator.toString() + Constants.NEW_LINE );
				}
				LOGGER.warn( message.toString() );
			}
		}
		catch( NotFoundException | CannotCompileException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to instrument the diffusive method." + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Class Pool: " + classPool.toString() + Constants.NEW_LINE );
			message.append( "  Translator: " + translator.toString() + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		}

        // here we need to get the javassist class loader to load this...
		// define the class 
		final Class< ? > clazz = defineClass( className, bytes, 0, bytes.length );
		if( clazz == null )
		{
			throw new ClassFormatError( className );
		}
		
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Loaded class using the RESTful diffuser class loader: " + RestfulDiffuserClassLoader.class.getName() + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Class Paths: " );
			for( URI uri : classPaths )
			{
				message.append( Constants.NEW_LINE + uri.toString() );
			}
			LOGGER.info( message.toString() );
		}

		return clazz;
	}
}
