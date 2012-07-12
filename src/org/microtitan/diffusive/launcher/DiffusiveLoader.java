package org.microtitan.diffusive.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.Loader;

/**
 * Class loader that provides the ability to load additional classes with the same class loader that is
 * used to load the and run the application with the {@link #run(String[])} or {@link #resolveClass(Class)} method.
 * For example, suppose you want to load a singleton as part of a set-up or configuration process, then you
 * can specify a set of classes and their associated static methods that will be loaded with the same class loader
 * that will be used to load and run the application.
 * 
 * Additionally, you can also specify prefixes of class names (package + class name) for which loading will be 
 * delegated to the parent class loader. This is useful, for example, to handle things like Apache's log4j configuration. 
 * 
 * @author Robert Philipp
 */
public class DiffusiveLoader extends Loader {

	private final Map< String, String > configClassMethodMap;
	private final List< String > delegationPrefixes;
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param configMap A {@link Map} containing the names of configuration classes and the method
	 * names in those classes that are used for configuration. Because these need to be loaded by this
	 * class loader, they must all be static methods (i.e. the class shouldn't have already been loaded).
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final Map< String, String > configMap, final ClassLoader parentLoader, final ClassPool classPool )
	{
		super( parentLoader, classPool );
		configClassMethodMap = new LinkedHashMap<>( configMap );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassLoader parentLoader, final ClassPool classPool )
	{
		this( new HashMap< String, String >(), parentLoader, classPool );
	}

	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configMap A {@link Map} containing the names of configuration classes and the method
	 * names in those classes that are used for configuration. Because these need to be loaded by this
	 * class loader, they must all be static methods (i.e. the class shouldn't have already been loaded).
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final Map< String, String > configMap, final ClassPool classPool )
	{
		super( classPool );
		configClassMethodMap = new LinkedHashMap<>( configMap );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}

	/**
     * Creates a new class loader.
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassPool classPool )
	{
		this( new HashMap< String, String >(), classPool );
	}
		
	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configMap A {@link Map} containing the names of configuration classes and the method
	 * names in those classes that are used for configuration. Because these need to be loaded by this
	 * class loader, they must all be static methods (i.e. the class shouldn't have already been loaded).
	 */
	public DiffusiveLoader( final Map< String, String > configMap )
	{
		super();
		configClassMethodMap = new LinkedHashMap<>( configMap );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}
	
	/**
     * Creates a new class loader.
	 */
	public DiffusiveLoader()
	{
		this( new HashMap< String, String >() );
	}
	
	private static List< String > createDefaultDelegationPrefixes()
	{
		final List< String > prefixes = new ArrayList<>();
		prefixes.add( "org.apache.log4j." );
		prefixes.add( "org.apache.commons.logging." );
		prefixes.add( "org.apache.abdera." );
		
		return prefixes;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see javassist.Loader#run(java.lang.String[])
	 */
	@Override
	public void run( final String[] args ) throws Throwable
	{
		run( args );
	}

	/* 
	 * (non-Javadoc)
	 * @see javassist.Loader#run(java.lang.String, java.lang.String[])
	 */
	@Override
	public void run( final String classname, final String[] args ) throws Throwable
	{
		for( Map.Entry< String, String > entry : configClassMethodMap.entrySet() )
		{
			final Class< ? > setupClazz = loadClass( entry.getKey() );
			try
			{
				setupClazz.getDeclaredMethod( entry.getValue() ).invoke( null/*setupClazz.newInstance()*/ );
			}
			catch( InvocationTargetException e )
			{
				throw e.getTargetException();
			}
		}
		
		super.run( classname, args );
	}

	/*
	 * (non-Javadoc)
	 * @see javassist.Loader#loadClassByDelegation(java.lang.String)
	 */
	@Override
	protected Class< ? > loadClassByDelegation( String name ) throws ClassNotFoundException
	{
		// ask the javassist class loader to delegate what it wants to delegate
		Class< ? > clazz = super.loadClassByDelegation( name );

		// if not delegated then check if we want to delegate
		if( clazz == null )//&& doDelegation )
		{
			for( String prefix : delegationPrefixes )
			{
				if( name.startsWith( prefix ) )
				{
					clazz = delegateToParent( name );
					break;
				}
			}
		}
		return clazz;
	}

}
