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
package org.microtitan.diffusive.launcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.Translator;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;


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
	
	private static final Logger LOGGER = Logger.getLogger( DiffusiveLoader.class );

//	private final List< String > configurationClasses;
	private final Map< String, Object[] > configurationClasses;
	private final List< String > delegationPrefixes;
	
	// hold a reference to this, because we need it to overload the javassist Loader
	private ClassPool classPool;
	private Translator translator;
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
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
	public DiffusiveLoader( final Map< String, Object[] > configClasses,  
							final List< String > delegationPrefixes,
							final ClassLoader parentLoader, 
							final ClassPool classPool )
	{
		super( parentLoader, classPool );
		configurationClasses = new LinkedHashMap<>( configClasses );
		this.delegationPrefixes = new ArrayList<>( delegationPrefixes );

		this.classPool = classPool;
	}
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation. Also
     * provides a mechanism for running additional configuration that will be loaded by the
     * same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final Map< String, Object[] > configClasses, 
							final ClassLoader parentLoader, 
							final ClassPool classPool )
	{
		this( configClasses, createDefaultDelegationPrefixes(), parentLoader, classPool );
	}
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassLoader parentLoader, final ClassPool classPool )
	{
		this( new LinkedHashMap< String, Object[] >(), parentLoader, classPool );
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
	public DiffusiveLoader( final Map< String, Object[] > configClasses, final ClassPool classPool )
	{
		super( classPool );
		configurationClasses = new LinkedHashMap<>( configClasses );
		delegationPrefixes = createDefaultDelegationPrefixes();

		this.classPool = classPool;
	}

	/**
     * Creates a new class loader.
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassPool classPool )
	{
		this( new LinkedHashMap< String, Object[] >(), classPool );
	}
		
	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 */
	public DiffusiveLoader( final Map< String, Object[] > configClasses )
	{
		super();
		configurationClasses = new LinkedHashMap<>( configClasses );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}
	
	/**
     * Creates a new class loader.
	 */
	public DiffusiveLoader()
	{
		this( new LinkedHashMap< String, Object[] >() );
	}
	
	/**
	 * Adds the name of a class that contains configuration items. A configuration item is
	 * represented by a method the has a @{@link DiffusiveConfiguration} annotation.
	 * @param className The name of a class containing configuration items.
	 * @return true if the class name was added to the list of configuration classes; false otherwise
	 * @see #invokeConfigurationClasses()
	 */
	public Object[] addConfigurationClass( final String className, final Object[] parameters )
	{
		if( configurationClasses.containsKey( className ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Replaced parameters of the configuration class: " + Constants.NEW_LINE );
			message.append( "  Configuration Class: " + className + Constants.NEW_LINE );
			message.append( "  Parameters: " + parameters );
			LOGGER.info( message.toString() );
		}
		return configurationClasses.put( className, parameters );
	}
	
	/**
	 * Removes the name of a class that contains configuration items. A configuration item is
	 * represented by a method the has a @{@link DiffusiveConfiguration} annotation.
	 * @param className The name of a class containing configuration items.
	 * @return true if the class name was removed to the list of configuration classes; false otherwise
	 * @see #invokeConfigurationClasses()
	 */
	public Object[] removeConfigurationClass( final String className )
	{
		return configurationClasses.remove( className );
	}
	
	/**
	 * Removes all the name of a class that contains configuration items. A configuration item is
	 * represented by a method the has a @{@link DiffusiveConfiguration} annotation.
	 * @see #invokeConfigurationClasses()
	 */
	public void clearConfigurationItems()
	{
		configurationClasses.clear();
	}
	
	/**
	 * @return Creates and returns the default list of delegation prefixes. These are the
	 * prefixes of fully qualified class names that should be loaded by the parent class
	 * loader, instead of this class loader.
	 * @see #loadClassByDelegation(String)
	 */
	protected final static List< String > createDefaultDelegationPrefixes()
	{
		final List< String > prefixes = new ArrayList<>();
		prefixes.add( "org.apache.log4j." );
		prefixes.add( "org.apache.commons.logging." );
		prefixes.add( "org.apache.abdera." );
		
		// we want to make sure that the DiffusiveConfiguration annotation is loaded
		// by the default app class loader, and NOT this one.
		prefixes.add( DiffusiveConfiguration.class.getName() );
		
		return prefixes;
	}
	
	/**
	 * Add a delegation prefix to the list of prefixes used to determine what classes get loaded by
	 * the parent class loader instead of this one. Useful for loggers and configuration classes. 
	 * The following conditions apply: 
	 * <ul>
	 * 	<li>If the specified prefix already exists, then nothing is added.</li>
	 * 	<li>If the specified prefix is more specific than an existing one, then it isn't added. For example, 
	 * 		if {@code "org.microtitan."} already exists, and {@code "org.microtitan.testing."} is specified, 
	 * 		then the specified prefix is unnecessary, and therefore, isn't added.</li> 
	 * 	<li>And if the specified prefix is more general than an existing one, then the specified
	 * 		one is added, and the more specific one is removed because it becomes superfluous.</li>
	 * @param prefix The prefix to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @return true of the delegation prefix was added; false otherwise.
	 * @see #loadClassByDelegation(String)
	 */
	public final boolean addDelegationPrefix( final String prefix )
	{
		boolean isAdded = false;
		if( delegationPrefixes.contains( prefix ) )
		{
			isAdded = true;
		}
		else
		{
			// loop through a copy of the delegation prefixes
			for( String delegationPrefix : new ArrayList<>( delegationPrefixes ) )
			{
				if( prefix.startsWith( delegationPrefix ) )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Attempting to a delegation prefix that is more specific than an existing delegation prefix." + Constants.NEW_LINE );
					message.append( "Specified prefix not added, but behavior continues as expected." + Constants.NEW_LINE );
					message.append( "  Specified Prefix: " + prefix + Constants.NEW_LINE );
					message.append( "  Existing Prefix: " + delegationPrefix );
					LOGGER.warn( message.toString() );
					
					// should act as if the delegation prefix was added, because the behavior is the same
					isAdded = true;
					
					// we're done with the loop
					break;
				}
				else if( delegationPrefix.startsWith( prefix ) )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Attempting to a delegation prefix that is more general than an existing delegation prefix." + Constants.NEW_LINE );
					message.append( "Specified prefix will be added, but the more specific one will be removed." + Constants.NEW_LINE );
					message.append( "  Specified Prefix: " + prefix + Constants.NEW_LINE );
					message.append( "  Existing Prefix: " + delegationPrefix );
					LOGGER.warn( message.toString() );
					
					// should act as if the delegation prefix was added, because the behavior is the same
					isAdded = delegationPrefixes.add( prefix );
					if( isAdded )
					{
						delegationPrefixes.remove( delegationPrefix );
					}
					
					// we're done with the loop
					break;
				}
			}
			if( !isAdded )
			{
				isAdded = delegationPrefixes.add( prefix );
			}
		}
		
		return isAdded;
	}
	
	/**
	 * Removes the specified prefix from the list of delegation prefixes. These are the
	 * prefixes of fully qualified class names that should be loaded by the parent class
	 * loader, instead of this class loader.
	 * @param prefix The prefix to remove from the list of delegation prefixes.
	 * @return true if the prefix was removed from the list of delegation prefixes; false otherwise
	 * @see #loadClassByDelegation(String)
	 */
	public boolean removeDelegationPrefix( final String prefix )
	{
		return delegationPrefixes.remove( prefix );
	}
	
	/**
	 * Removes all the prefixes from the list of delegation prefixes. These are the
	 * prefixes of fully qualified class names that should be loaded by the parent class
	 * loader, instead of this class loader.
	 * @see #loadClassByDelegation(String)
	 */
	public void clearDelegationPrefixes()
	{
		delegationPrefixes.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javassist.Loader#setClassPool(javassist.ClassPool)
	 */
	@Override
	public final void setClassPool( final ClassPool classPool )
	{
		super.setClassPool( classPool );
		this.classPool = classPool;
	}
	
	/**
	 * @return The source of the class files
	 */
	protected final ClassPool getClassPool()
	{
		return classPool;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javassist.Loader#addTranslator(javassist.ClassPool, javassist.Translator)
	 */
	@Override
	public final void addTranslator( final ClassPool classPool, final Translator translator ) throws NotFoundException, CannotCompileException
    {
    	super.addTranslator( classPool, translator );
    	this.classPool = classPool;
    	this.translator = translator;
    }
	
	/**
	 * @return the translator that gets called when a class is loaded
	 */
	protected final Translator getTranslator()
	{
		return translator;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see javassist.Loader#run(java.lang.String[])
	 */
	@Override
	public void run( final String[] args ) throws Throwable
	{
		// invoke the methods of the classes that are used to configure Diffusive
		// For example, we may load the KeyedDiffuserRepository and set the diffuser
		// that we like. Using this method ensures that the loading happens using the
		// same class loader (this one) as is used to load the application that is to be
		// run.
		invokeConfigurationClasses();

		super.run( args );
	}

	/* 
	 * (non-Javadoc)
	 * @see javassist.Loader#run(java.lang.String, java.lang.String[])
	 */
	@Override
	public void run( final String classname, final String[] args ) throws Throwable
	{
		// invoke the methods of the classes that are used to configure Diffusive
		// For example, we may load the KeyedDiffuserRepository and set the diffuser
		// that we like. Using this method ensures that the loading happens using the
		// same class loader (this one) as is used to load the application that is to be
		// run.
		invokeConfigurationClasses();
		
		// call the Javassist class loader's run method to load and run the code
		super.run( classname, args );
	}

	/**
	 * Invokes the methods of the classes specified in the {@link #configurationClasses} list
	 * that are annotated with @{@link DiffusiveConfiguration}.
	 * 
	 * For this method to work, the @{@link DiffusiveConfiguration} class name must be in the list
	 * of prefixes ({@link #delegationPrefixes}) that are delegated to the parent class loader. If
	 * the annotation does not appear in the list, it may be loaded by this class loader instead
	 * of the default app class loader, and this method will not see the configuration method's
	 * annotation, and will therefore, NOT call it.
	 *  
	 * @throws Throwable
	 */
	private void invokeConfigurationClasses() throws Throwable
	{
		// run through the class names, load the classes, and then invoke the configuration methods
		// (that have been annotated with @DiffusiveConfiguration)
		for( Map.Entry< String, Object[] > className : configurationClasses.entrySet() )
		{
			final Class< ? > setupClazz = loadClass( className.getKey() );
			Method configurationMethod = null;
			try
			{
				// grab the methods that have an annotation @DiffusiveConfiguration and invoke them
				for( final Method method : setupClazz.getMethods() )
				{
					if( method.isAnnotationPresent( DiffusiveConfiguration.class ) )
					{
						// hold on the the method in case there is an invocation exception
						// and to warn the user if no configuration method was found
						configurationMethod = method;
						method.invoke( null, className.getValue() );
					}
				}
				if( configurationMethod == null )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Error finding a method annotated with @Configure" + Constants.NEW_LINE );
					message.append( "  Configuration Class: " + className + Constants.NEW_LINE );
					LOGGER.warn( message.toString() );
				}
			}
			catch( InvocationTargetException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Error invoking target method." + Constants.NEW_LINE );
				message.append( "  Class Name: " + className + Constants.NEW_LINE );
				message.append( "  Method Name: " + configurationMethod.getName() );
				LOGGER.error( message.toString(), e );
				throw new IllegalArgumentException( message.toString(), e );
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javassist.Loader#loadClassByDelegation(java.lang.String)
	 */
	@Override
	protected Class< ? > loadClassByDelegation( final String name ) throws ClassNotFoundException
	{
		// ask the javassist class loader to delegate what it wants to delegate
		Class< ? > clazz = super.loadClassByDelegation( name );

		// if not delegated then check if we want to delegate
		if( clazz == null )
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
