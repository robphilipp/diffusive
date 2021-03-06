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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.NotFoundException;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.convertor.MethodIntercepterEditor;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;
import org.microtitan.diffusive.launcher.config.RestfulDiffuserConfig;
import org.microtitan.diffusive.translator.BasicDiffusiveTranslator;
import org.microtitan.diffusive.translator.DiffusiveTranslator;


/**
 * Launches the user application in a way that all method calls to methods annotated with {@code @Diffusive}
 * are intercepted and passed off to a {@link Diffuser} which runs the code and returns the results. Although
 * execution of the method may take place on a different machine, JVM, or locally, the code works as if the
 * diffusion where turned off.
 * 
 * The main method allows users to run this as an application, handing the name of the class for which execute
 * the {@code main(...)} method, and handing it a list of command line arguments that {@code main(...)} method
 * accepts. In this way, there is no change to the code that is run, with the exception of the {@code @Diffusive}
 * annotations that are added to the methods that are to be diffused.  
 *   
 * @author Robert Philipp
 */
public class DiffusiveLauncher {
	
	private static final Logger LOGGER = Logger.getLogger( DiffusiveLauncher.class );

	/**
	 * The directory containing the diffuser configuration file for the application-attached diffuser.
	 */
	public static final String XML_CONFIG_DIR = "config/launcher/";

	/**
	 * The name of the XML configuration file that is read to obtain the configuration settings that are needed
	 * by the RESTful diffuser
	 */
	public static final String XML_CONFIG_FILE_NAME = "restful_diffuser_config.xml";

	private final DiffusiveLoader loader;

	/**
	 * Constructs a {@link DiffusiveLauncher} using the specified class loader.
	 * @param loader The {@link DiffusiveLoader} used to load classes, rewrite diffusive methods, and delegate
	 * loading to the parent class loader.
	 */
	public DiffusiveLauncher( final DiffusiveLoader loader )
	{
		this.loader = loader;
	}
	
	/**
	 * Constructs a {@link DiffusiveLauncher} that uses a default {@link DiffusiveLoader} to load classes, 
	 * rewrite diffusive methods, and delegate loading to the parent class loader. Uses a {@link RestfulDiffuser},
	 * uses the default values from the {@link DiffusiveLoader} to determine which classes to load using the
	 * parent class loader (logging, abdera, diffusive configuration annotation, etc).
	 * @param configurationClasses the classes holding the configuration for the launcher. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 */
	public DiffusiveLauncher( final Map< String, Object[] > configurationClasses, final List< String > classPaths )
	{
		this( createLoader( configurationClasses, classPaths ) );
	}
	
	/**
	 * Constructs a {@link DiffusiveLauncher} that uses a default {@link DiffusiveLoader} to load classes, 
	 * rewrite diffusive methods, and delegate loading to the parent class loader. Uses a {@link RestfulDiffuser},
	 * uses the default values from the {@link DiffusiveLoader} to determine which classes to load using the
	 * parent class loader (logging, abdera, diffusive configuration annotation, etc).
	 */
	public DiffusiveLauncher()
	{
		this( createLoader() );
	}
	
	/**
	 * @return creates a default list of class names that have configuration items (methods used to configure
	 * diffusive)
	 */
	private static Map< String, Object[] > createDefaultConfiguration()
	{
		final Map< String, Object[] > configurations = new LinkedHashMap<>();
		configurations.put( RestfulDiffuserConfig.class.getName(), new Object[] { XML_CONFIG_FILE_NAME } );
		return configurations;
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
	 * @return creates and returns a {@link MethodIntercepterEditor} with a local {@link Diffuser}
	 */
	private static MethodIntercepterEditor createDefaultMethodIntercepter()
	{
		return new MethodIntercepterEditor();
	}
	
	/**
	 * @return a {@link DiffusiveLoader} that uses the default set of configuration class, the
	 * default set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the default {@link DiffusiveTranslator}.
	 */
	public static DiffusiveLoader createLoader()
	{
		return createLoader( createDefaultConfiguration() );
	}
    
	/**
	 * @param classPaths The class paths to the application's jar file
	 * @return a {@link DiffusiveLoader} that uses the default set of configuration class, the
	 * default set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the default {@link DiffusiveTranslator}.
	 */
	public static DiffusiveLoader createLoader( final List< String > classPaths )
	{
		return createLoader( createDefaultConfiguration(), classPaths );
	}
    
	/**
	 * Creates a {@link DiffusiveLoader} the uses the specified list of configuration classes, the
	 * default set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the default {@link DiffusiveTranslator}. 
	 * @param configurations A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @return a {@link DiffusiveLoader}
	 */
	public static DiffusiveLoader createLoader( final Map< String, Object[] > configurations )
	{
		return createLoader( configurations, null );
	}
	
	/**
	 * Creates a {@link DiffusiveLoader} the uses the specified list of configuration classes, the
	 * default set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the default {@link DiffusiveTranslator}. 
	 * @param configurations A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param classPaths The class paths to the application's jar file
	 * @return a {@link DiffusiveLoader}
	 */
	public static DiffusiveLoader createLoader( final Map< String, Object[] > configurations, final List< String > classPaths )
	{
		return createLoader( configurations, classPaths, createDefaultTranslator( createDefaultMethodIntercepter() ) );
	}
	
	/**
	 * Creates a {@link DiffusiveLoader} the uses the specified list of configuration classes, the
	 * default set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the specified {@link DiffusiveTranslator}. 
	 * @param configurations A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation.  Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param classPaths The class paths to the application's jar file
	 * @param translator The translator used to modify the diffusive methods.
	 * @return a {@link DiffusiveLoader}
	 */
	public static DiffusiveLoader createLoader( final Map< String, Object[] > configurations,
											    final List< String > classPaths,
												final DiffusiveTranslator translator )
	{
		// get the default class pool
		final ClassPool pool = ClassPool.getDefault();
		
		// add the specified application's Jar file to the class pool's class path
		addClassPathToClassPool( classPaths, pool );
		
		// create a loader for that pool, setting the class loader for this class as the parent
		final DiffusiveLoader loader = new DiffusiveLoader( configurations, DiffusiveLauncher.class.getClassLoader(), pool );
		
		try
		{
			// set up the class loader with the translator
			loader.addTranslator( pool, translator );
		}
		catch( Throwable exception )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Error loading the specified class" ).append( Constants.NEW_LINE )
			        .append( "  Loader: " ).append( loader.getClass().getName() ).append( Constants.NEW_LINE );

			LOGGER.error( message.toString(), exception );
			throw new IllegalArgumentException( message.toString(), exception );
		}

		return loader;
	}
	
	/**
	 * Creates a {@link DiffusiveLoader} the uses the specified list of configuration classes, the
	 * specified set of delegation prefixes (defined in the {@link DiffusiveLoader} class), and
	 * the specified {@link DiffusiveTranslator}. 
	 * @param configurations A {@link List} containing the names of configuration classes that are 
	 * used for configuration. Because these need to be loaded by this class loader, they must all 
	 * be static methods (i.e. the class shouldn't have already been loaded) and they must be annotated
	 * with the @{@link DiffusiveConfiguration} annotation. Associated with each configuration class is
	 * an {@code {@link Object}[]} containing any arguments the configuration method may need.
	 * @param classPaths The class paths to the application's jar file
	 * @param delegationPrefixes The list of prefixes to the fully qualified class name. Classes whose fully qualified class
	 * names start with one of these prefixes are loaded by the parent class loader instead of this one.
	 * @param translator The translator used to modify the diffusive methods.
	 * @return a {@link DiffusiveLoader}
	 */
	public static DiffusiveLoader createLoader( final Map< String, Object[] > configurations,
                                                final List< String > classPaths,
												final List< String > delegationPrefixes,
												final DiffusiveTranslator translator )
	{
		// get the default class pool
		final ClassPool pool = ClassPool.getDefault();

		// add the specified application's Jar file to the class pool's class path
		addClassPathToClassPool( classPaths, pool );
		
		// create a loader for that pool, setting the class loader for this class as the parent
		final DiffusiveLoader loader = new DiffusiveLoader( configurations, delegationPrefixes, DiffusiveLauncher.class.getClassLoader(), pool );
		
		try
		{
			// set up the class loader with the translator
			loader.addTranslator( pool, translator );
		}
		catch( Throwable exception )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Error loading the specified class" ).append( Constants.NEW_LINE )
			        .append( "  Loader: " ).append( loader.getClass().getName() ).append( Constants.NEW_LINE );

			LOGGER.error( message.toString(), exception );
			throw new IllegalArgumentException( message.toString(), exception );
		}

		return loader;
	}
	
	/**
	 * Adds the specified Jar files to the {@link ClassPool}'s class path's 
	 * @param classPaths The list of paths to the application's Jar files 
	 * @param pool The {@link ClassPool} to which to add the Jar files
	 */
	private static void addClassPathToClassPool( final List< String > classPaths, final ClassPool pool )
	{
		// nothing to do if the class path list is null or empty
		if( classPaths == null || classPaths.isEmpty() )
		{
			return;
		}
		
		// add the class path to the application's jar file
		for( String classPath : classPaths )
		{
			try
			{
				pool.insertClassPath( classPath );
				
				if( LOGGER.isInfoEnabled() )
				{
					final StringBuilder message = new StringBuilder();
					message.append( "Added Jar file to class pool's class path" ).append( Constants.NEW_LINE )
                            .append( "  Jar Class Path: " ).append( classPath ).append( Constants.NEW_LINE )
                            .append( "  Class Pool: " ).append( pool.toString() );
					LOGGER.info( message.toString() );
				}
			}
			catch( NotFoundException e ) 
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Unable to find Jar file when attempting to add it to class pool's class path" ).append( Constants.NEW_LINE )
                        .append( "  Jar Class Path: " ).append( classPath ).append( Constants.NEW_LINE )
                        .append( "  Class Pool: " ).append( pool.toString() );
				LOGGER.warn( message.toString(), e );
			}
		}
	}
	
	/**
	 * Runs the "main" method for the class name, passing in the command-line arguments handed to this
	 * objects constructor.
	 * @param classNameToRun The name of the class for which to run the "main" method
	 * @param programArguments The command-line arguments passed to the "main" method
	 */
	public void run( final String classNameToRun, final String...programArguments )
	{
		run( loader, classNameToRun, programArguments );
	}

	/**
	 * Runs the "main" method for the specified class name, passing in the specified command-line arguments
	 * @param loader The {@link DiffusiveLoader} used to load the classes and run the "main" method
	 * @param classNameToRun The name of the class for which to run the "main" method
	 * @param programArguments The command-line arguments passed to the "main" method
	 */
	public static void run( final DiffusiveLoader loader,
							final String classNameToRun, 
							final String...programArguments )
	{
		try
		{
			// invoke the "main" method of the class named in the "className" variable
			loader.run( classNameToRun, programArguments );
		}
		catch( Throwable exception )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Error running the specified class" ).append( Constants.NEW_LINE )
                    .append( "  Loader: " ).append( loader.getClass().getName() ).append( Constants.NEW_LINE )
                    .append( "  Class Name: " ).append( classNameToRun );
			if( programArguments.length > 0 )
			{
				message.append( Constants.NEW_LINE ).append( "  Program Arguments: " );
				for( String arg : programArguments )
				{
					message.append( Constants.NEW_LINE ).append( "    " ).append( arg );
				}
			}
			else
			{
				message.append( Constants.NEW_LINE ).append( "  Program Arguments: [none specified]" );
			}
			LOGGER.error( message.toString(), exception );
			throw new IllegalArgumentException( message.toString(), exception );
		}
	}
	
	/**
	 * Runs the application without using Javassist to instrument the code for diffusion. This simply
	 * allows you to test you application through the system class loader. Mainly available for debugging.
	 * @param classNameToRun The name of the class for which to run it "main" method
	 * @param programArguments The command-line arguments passed to the "main" method
	 */
	public static void runClean( final String classNameToRun, final String...programArguments )
	{
		try
		{
			// load the target class to be run
			final Class< ? > clazz = DiffusiveLauncher.class.getClassLoader().loadClass( classNameToRun );
			
			// grab the type of the program arguments (should all be String)
			final Class< ? >[] progArgsType = new Class[] { programArguments.getClass() };
			
			// find the "main" method for the class (throws exception if can't be found)
			final Method mainMethod = clazz.getDeclaredMethod( "main", progArgsType );
			
			// invoke the "main" of the class (first arg null because main is static)
			mainMethod.invoke( null, new Object[] { programArguments } );
		}
		catch( ClassNotFoundException | 
			   NoSuchMethodException | 
			   SecurityException | 
			   IllegalAccessException | 
			   IllegalArgumentException | 
			   InvocationTargetException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to launch application" ).append( Constants.NEW_LINE )
                    .append( "  Name of class to run: " ).append( classNameToRun ).append( Constants.NEW_LINE )
                    .append( "  Name of method to run: main" ).append( Constants.NEW_LINE )
                    .append( "  Program arguments (command line arguments): " );
			if( programArguments.length > 0 )
			{
				for( String arg : programArguments )
				{
					message.append( Constants.NEW_LINE ).append( "    " ).append( arg );
				}
			}
			else
			{
				message.append( "[none]" );
			}
			message.append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}
	
	/**
	 * Whether the diffuser launcher is to diffuse to remote diffusers or just stay local
	 * for debugging.
	 *  
	 * @author Robert Philipp
	 */
	private static enum RunMode {
		CLEAN( "clean" ),
		DIFFUSED( "diffused" );
		
		private String mode;
		private RunMode( final String mode )
		{
			this.mode = mode;
		}
		
		public String getName()
		{
			return mode;
		}
		
		public static RunMode getRunMode( final String modeString )
		{
			RunMode runMode = null;
			for( RunMode mode : RunMode.values() )
			{
				if( mode.getName().equals( modeString ) )
				{
					runMode = mode;
					break;
				}
			}
			return runMode;
		}
	}
	
	/**
	 * Validates the the user specified a valid run mode. If the user specified a valid run-mode string, then
	 * the method returns the associated run mode, otherwise it exits with a message.
	 * @param runModeSpec The command-line specification for the run-mode
	 * @param options The options entered on the command-line by the user
	 * @return The {@link RunMode} associated with the user-specified string.
	 */
	private static RunMode validateRunMode( final OptionSpec< String > runModeSpec, final OptionSet options )
	{
		final RunMode runMode = RunMode.getRunMode( runModeSpec.value( options ) );
		if( runMode == null )
		{
			final String message = "Invalid argument for \"run-mode\" option: " + runModeSpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return runMode;
	}

	/**
	 * Make sure to run a {@link RestfulDiffuserServer} instance before calling this. And
	 * make sure that the endpoint listed in the {@link RestfulDiffuserServer#DEFAULT_SERVER_URI}
	 * method matches up to that in the {@link RestfulDiffuserConfig} so that it
	 * knows how to call the endpoint.
	 * 
	 * @param args Program arguments
	 * @throws IOException 
	 */
	public static void main( String...args ) throws IOException
	{
		// set up the command-line arguments
		final OptionParser parser = new OptionParser();
		final OptionSpec< String > logLevelSpec = 
				parser.accepts( "log-level" ).withRequiredArg().ofType( String.class ).defaultsTo( Level.WARN.toString() ).
				describedAs( Level.TRACE + "|" + Level.DEBUG + "|" + Level.INFO + "|" + Level.WARN + "|" + Level.ERROR );
		final OptionSpec< String > runModeSpec =
				parser.accepts( "run-mode" ).withRequiredArg().ofType( String.class ).defaultsTo( RunMode.DIFFUSED.getName() ).
				describedAs( RunMode.DIFFUSED.getName() + "|" + RunMode.CLEAN.getName() );
		final OptionSpec< String > configDirSpec =
				parser.accepts( "config-dir" ).withRequiredArg().ofType( String.class ).defaultsTo( XML_CONFIG_DIR );
		final OptionSpec< String > configFileSpec =
				parser.accepts( "config-file" ).withRequiredArg().ofType( String.class ).defaultsTo( XML_CONFIG_FILE_NAME );
		final OptionSpec< String > configClassSpec =
				parser.accepts( "config-class" ).withRequiredArg().ofType( String.class ).defaultsTo( RestfulDiffuserConfig.class.getName() );
		final OptionSpec< String > classNameSpec = 
				parser.accepts( "execute-class" ).withRequiredArg().ofType( String.class ).defaultsTo( "org.microtitan.tests.montecarlo.VolumeCalc" );
		final char pathSeparator = System.getProperty( "path.separator").charAt( 0 );
		final OptionSpec< String > classPathSpec =
				parser.accepts( "class-path" ).withRequiredArg().ofType( String.class ).withValuesSeparatedBy( pathSeparator );
		final OptionSpec< String > programArgSpec = 
				parser.accepts( "prog-args" ).withRequiredArg().ofType( String.class ).withValuesSeparatedBy( ' ' );
		parser.accepts( "help" );
		
		// parse the command-line arguments
		OptionSet options = null;
		try
		{
			options = parser.parse( args );
		}
		catch( OptionException e )
		{
			e.printStackTrace();
			LOGGER.error( "Error parsing the command-line options.", e );

			System.out.println( "\nPlease see the usage information below: " );
			parser.printHelpOn( System.out );
			System.exit( -1 );
		}

		// if the user requests help, print out help
		if( options.has( "help" ) )
		{
			parser.printHelpOn( System.out );
			System.exit( 0 );
		}

		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.toLevel( logLevelSpec.value( options ) ) );

		// grab the values of the command-line arguments
		final RunMode runMode = validateRunMode( runModeSpec, options );
		final String classNameToRun = classNameSpec.value( options );
		List< String > classPaths = null;
		if( options.has( classPathSpec ) )
		{
			classPaths = classPathSpec.values( options );
		}
		
		String[] programArgs = new String[] {};
		if( options.has( programArgSpec ) )
		{
            final List< String > values = programArgSpec.values( options );
			programArgs = values.toArray( new String[ values.size() ] );
		}

		// start the timing for the run
		final long start = System.currentTimeMillis();
		if( runMode == RunMode.DIFFUSED )
		{
			// grab the list of configuration classes for configuring the application attached diffuser
			final String configFile = configDirSpec.value( options ) + configFileSpec.value( options );
			final Map< String, Object[] > configurationClasses = new LinkedHashMap<>();
			configurationClasses.put( configClassSpec.value( options ), new Object[] { configFile } );

			// run the application for the specified class
			final DiffusiveLauncher launcher = new DiffusiveLauncher( configurationClasses, classPaths );
			launcher.run( classNameToRun, programArgs );
		}
		else if( runMode == RunMode.CLEAN )
		{
			runClean( classNameToRun, programArgs );
		}
		
		System.out.println( "done: " + (double)(System.currentTimeMillis() - start)/1000 + " s" );
	}
}
