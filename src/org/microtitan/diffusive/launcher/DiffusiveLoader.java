package org.microtitan.diffusive.launcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.ClassPool;
import javassist.Loader;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.launcher.config.RestfulDiffuserRepositoryConfig;


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

	private final List< String > configurationClasses;
	private final List< String > delegationPrefixes;
	
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
	public DiffusiveLoader( final List< String > configClasses, final ClassLoader parentLoader, final ClassPool classPool )
	{
		super( parentLoader, classPool );
		configurationClasses = new ArrayList<>( configClasses );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}
	
	/**
     * Creates a new class loader using the specified parent class loader for delegation.
	 * @param parentLoader the parent class loader of this class loader
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassLoader parentLoader, final ClassPool classPool )
	{
		this( new ArrayList< String >(), parentLoader, classPool );
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
	public DiffusiveLoader( final List< String > configClasses, final ClassPool classPool )
	{
		super( classPool );
		configurationClasses = new ArrayList<>( configClasses );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}

	/**
     * Creates a new class loader.
	 * @param classPool the source of the class files
	 */
	public DiffusiveLoader( final ClassPool classPool )
	{
		this( new ArrayList< String >(), classPool );
	}
		
	/**
     * Creates a new class loader. Also provides a mechanism for running additional configuration that 
     * will be loaded by the same class loader that is loading the application with the diffuser annotations.
	 * @param configClasses A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation
	 */
	public DiffusiveLoader( final List< String > configClasses )
	{
		super();
		configurationClasses = new ArrayList<>( configClasses );
		delegationPrefixes = createDefaultDelegationPrefixes();
	}
	
	/**
     * Creates a new class loader.
	 */
	public DiffusiveLoader()
	{
		this( new ArrayList< String >() );
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
		// invoke the methods of the classes that are used to configure Diffusive
		// For example, we may load the KeyedDiffuserRepository and set the diffuser
		// that we like. Using this method ensures that the loading happens using the
		// same class loader (this one) as is used to load the application that is to be
		// run.
		invokeConfigurationClasses();
		
		// call the Javassist class loader's run method to load and run the code
		super.run( classname, args );
	}

	/*
	 * Invokes the methods of the classes specified in the {@link #configurationClasses} list
	 * that are annotated with @{@link DiffusiveConfiguration}. Because the class loaders for the
	 * annotation and the code to process the annotation may be different, we use a round-about method
	 * that compares the annotations and the class name of the annotation. Needs to be fixed.
	 * TODO fix me (see above)
	 * @throws Throwable
	 */
	private void invokeConfigurationClasses() throws Throwable
	{
		// old method where the user passed in the class name AND the method name
		// instead of the new approach where the methods are annotated as configuration methods
//		for( Map.Entry< String, String > entry : configClassMethodMap.entrySet() )
//		{
//			final Class< ? > setupClazz = loadClass( entry.getKey() );
//			try
//			{
//				setupClazz.getDeclaredMethod( entry.getValue() ).invoke( null/*setupClazz.newInstance()*/ );
//			}
//			catch( InvocationTargetException e )
//			{
//				throw e.getTargetException();
//			}
//		}
		
		for( String className : configurationClasses )
		{
			final Class< ? > setupClazz = loadClass( className );
			Method configurationMethod = null;
			try
			{
				// grab the methods that have an annotation @DiffusiveConfiguration
				for( final Method method : setupClazz.getMethods() )
				{
					final List< Annotation > annotations = Arrays.asList( method.getDeclaredAnnotations() );
					for( Annotation annot : annotations )
					{
						if( annot.annotationType().getName().equals( DiffusiveConfiguration.class.getName() ) )
						{
							// hold on the the method in case there is an invocation exception
							// and to warn the user if no configuration method was found
							configurationMethod = method;
							method.invoke( null/*setupClazz.newInstance()*/ );
						}
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

	public static void main( String[] args ) throws Exception
	{
		for( Method m : RestfulDiffuserRepositoryConfig.class.getMethods() )
		{
			if( m.isAnnotationPresent( DiffusiveConfiguration.class ) )
			{
				System.out.println( "Method annotaed with @" + DiffusiveConfiguration.class.getName() + ": " + m.getName() );
			}
			else
			{
				System.out.println( "Method not annotaed with @" + DiffusiveConfiguration.class.getName() + ": " + m.getName() );
			}
		}
	}

}
