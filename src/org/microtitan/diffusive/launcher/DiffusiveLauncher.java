package org.microtitan.diffusive.launcher;

import java.util.Arrays;

import javassist.ClassPool;
import javassist.Loader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.convertor.MethodInterceptorEditor;
import org.microtitan.diffusive.diffuser.Diffuser;
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
	
	private String classNameToRun;
	private String[] programArguments;
	
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
	 * @see MethodInterceptorEditor
	 */
	public DiffusiveLauncher( final String classNameToRun, final String...programArguments )
	{
		this( createDefaultTranslator( createDefaultExpressionEditor() ), classNameToRun, programArguments );
	}
	
	/**
	 * 
	 * @param expressionEditor
	 * @return
	 */
	private static DiffusiveTranslator createDefaultTranslator( final MethodInterceptorEditor expressionEditor )
	{
		return new BasicDiffusiveTranslator( expressionEditor );
	}

	/**
	 * 
	 * @return
	 */
	private static MethodInterceptorEditor createDefaultExpressionEditor()
	{
		return new MethodInterceptorEditor();
	}

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
	public MethodInterceptorEditor setExpressionEditor( final MethodInterceptorEditor expressionEditor )
	{
		final MethodInterceptorEditor oldEditor = translator.setExpressionEditor( expressionEditor );
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
	 * 
	 */
	public void run()
	{
		run( classNameToRun, programArguments );
	}

	/**
	 * 
	 * @param classNameToRun
	 * @param programArguments
	 */
	public static void run( final String classNameToRun, final String...programArguments )
	{
		try
		{
			// get the default class pool
			final ClassPool pool = ClassPool.getDefault();

			// create a loader for that pool
			final Loader loader = new Loader( pool );
			
			// add up the class loader with the translator
			loader.addTranslator( pool, new BasicDiffusiveTranslator( new MethodInterceptorEditor() ) );
			
			// invoke the "main" method of the class named in the "className" variable
			loader.run( classNameToRun, programArguments );
		}
		catch( Throwable exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( Loader.class.getName() + " failed to load and run the specified class" );
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
		
		// run the application for the specified class
		final String classNameToRun = args[ 0 ];
		final String[] programArgs = Arrays.copyOfRange( args, 1, args.length );
		run( classNameToRun, programArgs );
	}
}
