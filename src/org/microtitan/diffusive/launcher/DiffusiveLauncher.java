package org.microtitan.diffusive.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;

import javassist.ClassPool;
import javassist.Loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.convertor.MethodIntercepterEditor;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserApplication;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.tests.BeanTest;
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
 * @author rob
 */
public class DiffusiveLauncher {
	
	private static final Logger LOGGER = Logger.getLogger( DiffusiveLauncher.class );
	
	private final String classNameToRun;
	private final String[] programArguments;
	
	private DiffusiveTranslator translator;

	/**
	 * 
	 * @param translator
	 * @param classNameToRun
	 * @param programArguments
	 */
	public DiffusiveLauncher( final DiffusiveTranslator translator, final String classNameToRun, final String...programArguments )
	{
		this.classNameToRun = classNameToRun;
		this.programArguments = programArguments;
		this.translator = translator;
	}
	
	/**
	 * 
	 * @param classNameToRun
	 * @param programArguments
	 * 
	 * @see DiffusiveTranslator
	 * @see MethodIntercepterEditor
	 */
	public DiffusiveLauncher( final String classNameToRun, final String...programArguments )
	{
		this( createDefaultTranslator( createDefaultMethodIntercepter() ), classNameToRun, programArguments );
	}
	
	/**
	 * 
	 * @param expressionEditor
	 * @return
	 */
	private static DiffusiveTranslator createDefaultTranslator( final MethodIntercepterEditor expressionEditor )
	{
		return new BasicDiffusiveTranslator( expressionEditor );
	}

	/*
	 * Creates a default method intercepter using the specified {@link Diffuser}
	 * @param diffuser The diffuser used with the default method intercepter
	 * @return creates and returns a {@link MethodIntercepterEditor} with a local {@link Diffuser}
	 */
	private static MethodIntercepterEditor createDefaultMethodIntercepter( /*final Diffuser diffuser*/ )
	{
		return new MethodIntercepterEditor( /*diffuser*/ );
	}
	
//	/*
//	 * @return creates and returns a {@link MethodIntercepterEditor} with a local {@link Diffuser}
//	 */
//	private static MethodIntercepterEditor createDefaultMethodIntercepter()
//	{
//		return createDefaultMethodIntercepter( new LocalDiffuser() );
//	}
	
	/**
	 * 
	 * @param translator
	 * @return
	 */
	public DiffusiveTranslator setTranslator( final DiffusiveTranslator translator )
	{
		final DiffusiveTranslator oldTranslator = this.translator;
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Set diffusive translator:" + Constants.NEW_LINE );
			message.append( "  Old Translator: " + oldTranslator.getClass().getName() + Constants.NEW_LINE );
			message.append( "  New Translator: " + translator.getClass().getName() );
			LOGGER.info( message.toString() );
		}
		this.translator = translator;
		return oldTranslator;
	}
	
	/**
	 * 
	 * @param expressionEditor
	 * @return
	 */
	public MethodIntercepterEditor setExpressionEditor( final MethodIntercepterEditor expressionEditor )
	{
		final MethodIntercepterEditor oldEditor = translator.setExpressionEditor( expressionEditor );
		if( LOGGER.isInfoEnabled() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Set diffusive translator:" + Constants.NEW_LINE );
			message.append( "  Old Expression Editor: " + oldEditor.getClass().getName() + Constants.NEW_LINE );
			message.append( "  New Expression Editor: " + expressionEditor.getClass().getName() );
			LOGGER.info( message.toString() );
		}
		return oldEditor;
	}
	
	/**
	 * Runs the "main" method for the class name, passing in the command-line arguments handed to this
	 * objects constructor.
	 * @see DiffusiveLauncher#classNameToRun
	 * @see DiffusiveLauncher#programArguments
	 */
	public void run()
	{
		run( translator, classNameToRun, programArguments );
	}

	/**
	 * Runs the "main" method for the specified class name, passing in the specified command-line arguments
	 * @param classNameToRun The name of the class for which to run the "main" method
	 * @param programArguments The command-line arguments passed to the "main" method
	 */
	public static void run( final DiffusiveTranslator translator, final String classNameToRun, final String...programArguments )
	{
		try
		{
			// get the default class pool
			final ClassPool pool = ClassPool.getDefault();

			// create a loader for that pool, setting the class loader for this class as the parent
			final Loader loader = new Loader( DiffusiveLauncher.class.getClassLoader(), pool );
			
			// add up the class loader with the translator
			loader.addTranslator( pool, translator );
			
			// invoke the "main" method of the class named in the "className" variable
			loader.run( classNameToRun, programArguments );
		}
		catch( Throwable exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( Loader.class.getName() + " failed to load and run the specified class" + Constants.NEW_LINE );
			message.append( "  Loader: " + Loader.class.getName() + Constants.NEW_LINE );
			message.append( "  Class Name: " + classNameToRun + Constants.NEW_LINE );
			if( programArguments.length > 0 )
			{
				message.append( "  Program Arguments: " + Constants.NEW_LINE );
				for( String arg : programArguments )
				{
					message.append( "    " + arg + Constants.NEW_LINE );
				}
			}
			else
			{
				message.append( "  Program Arguments: [none specified]" + Constants.NEW_LINE );
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
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to launch application" + Constants.NEW_LINE );
			message.append( "  Name of class to run: " + classNameToRun + Constants.NEW_LINE );
			message.append( "  Name of method to run: main" + Constants.NEW_LINE );
			message.append( "  Program arguments (command line arguments): " );
			if( programArguments.length > 0 )
			{
				for( String arg : programArguments )
				{
					message.append( Constants.NEW_LINE + "    " + arg );
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
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		// ensure that a class has been specified (the class must have a main)
		if( args.length < 1 )
		{
			System.out.println();
			System.out.println( "+-------------------------------------+" );
			System.out.println( "|  Usage: name_of_class_to_run [arg]* |" );
			System.out.println( "|                                     |" );
			System.out.println( "|  **** Running simple test code **** |" );
			System.out.println( "+-------------------------------------+" );
			System.out.println();
			args = new String[] { BeanTest.class.getName() };
		}
		
		// TODO this needs to be set up through a configuration or programatically. Probably best through a RESTfulDiffusiveLauncher,
		// a LocalDiffusiveLauncher, a NullDiffusiveLauncher, etc..
		// run and set up the local RESTful Diffuser server
		final RestfulDiffuserManagerResource resource = new RestfulDiffuserManagerResource();
		final RestfulDiffuserApplication application = new RestfulDiffuserApplication();
		application.addSingletonResource( resource );

		final URI serverUri = URI.create( "http://localhost:8182/" );
		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, application );
		
		// TODO have the configuration code (currently in BeanTest) inserted into the main of the class-to-run
		// set the use of the RESTful diffuser
//		final Serializer serializer = new ObjectSerializer();
//		final List< URI > endpoints = Arrays.asList( URI.create( "http://localhost:8182/diffuser" ) );
//		final Diffuser restfulDiffuser = new RestfulDiffuser( serializer, endpoints );
//		KeyedDiffuserRepository.getInstance().setDiffuser( new RestfulDiffuser( serializer, endpoints ) );

		// run the application for the specified class
		final String classNameToRun = args[ 0 ];
		final String[] programArgs = Arrays.copyOfRange( args, 1, args.length );
		final DiffusiveTranslator translator = createDefaultTranslator( createDefaultMethodIntercepter() );
		run( translator, classNameToRun, programArgs );
//		runClean( classNameToRun, programArgs );
		
		System.out.println( String.format( "Jersy app start with WADL available at %sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...", serverUri, serverUri ) );
		try
		{
			System.in.read();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.stop();
	}
}
