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
package org.microtitan.diffusive.translator;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.NotFoundException;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.converter.MethodInterceptorEditor;
import org.microtitan.diffusive.diffuser.Diffuser;

/**
 * Observer of the {@link Loader}, which calls the {@link #start(ClassPool)} and {@link #onLoad(ClassPool, String)}
 * method of an INSTANCE of this object. These methods can be used to translate the byte code before it is loaded.
 * 
 * In this case only the {@link #onLoad(ClassPool, String)} method responds by inserting a {@link org.microtitan.diffusive.converter.MethodInterceptorEditor}
 * that is used to intercept method calls to methods that are annotated with {@code @Diffusive}.
 * 
 * @author Robert Philipp
 */
public class BasicDiffusiveTranslator implements DiffusiveTranslator {
	
	private static final Logger LOGGER = Logger.getLogger( BasicDiffusiveTranslator.class );
	
	private MethodInterceptorEditor expressionEditor;
	
	/**
	 * Constructor that accepts a {@link org.microtitan.diffusive.converter.MethodInterceptorEditor} which will be used to generate the source
	 * code that replaces the intercepted method call
	 * @param expressionEditor The {@link org.microtitan.diffusive.converter.MethodInterceptorEditor} used to create the source code that replaces
	 * the intercepted method call
	 */
	public BasicDiffusiveTranslator( final MethodInterceptorEditor expressionEditor )
	{
		this.expressionEditor = expressionEditor;
	}
	
	/**
	 * Constructor that sets as its default expression editor, the {@link org.microtitan.diffusive.converter.MethodInterceptorEditor} with
	 * the specified {@link Diffuser}.
	 * @param diffuser the specified {@link Diffuser}
	 */
	public BasicDiffusiveTranslator( final Diffuser diffuser )
	{
		this.expressionEditor = createDefaultMethodIntercepter( /*diffuser*/ );
	}

	/**
	 * Constructor that sets as its default expression editor, the {@link org.microtitan.diffusive.converter.MethodInterceptorEditor}
	 */
	public BasicDiffusiveTranslator()
	{
		this( createDefaultMethodIntercepter() );
	}
	
	/*
	 * Creates a default method intercepter using the specified {@link Diffuser}
	 * @param diffuser The diffuser used with the default method intercepter
	 * @return creates and returns a {@link MethodInterceptorEditor} with a local {@link Diffuser}
	 */
	private static MethodInterceptorEditor createDefaultMethodIntercepter( /*final Diffuser diffuser*/ )
	{
		return new MethodInterceptorEditor( /*diffuser*/ );
	}
	
//	/*
//	 * @return creates and returns a {@link MethodInterceptorEditor} with a local {@link Diffuser}
//	 */
//	private static MethodInterceptorEditor createDefaultMethodIntercepter()
//	{
//		return createDefaultMethodIntercepter( new LocalDiffuser() );
//	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.translator.DiffusiveTranslator#getExpressionEditor()
	 */
	@Override
	public MethodInterceptorEditor getExpressionEditor()
	{
		return expressionEditor;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.translator.DiffusiveTranslator#setExpressionEditor(org.microtitan.diffusive.converter.MethodInterceptorEditor)
	 */
	@Override
	public MethodInterceptorEditor setExpressionEditor( final MethodInterceptorEditor expressionEditor )
	{
		final MethodInterceptorEditor oldEditor = this.expressionEditor;
		this.expressionEditor = expressionEditor;
		return oldEditor;
	}

	/*
	 * (non-Javadoc)
	 * @see javassist.Translator#start(javassist.ClassPool)
	 */
	@Override
	public void start( final ClassPool pool ) 
	{
		// don't do anything at this point
	}

	/*
	 * (non-Javadoc)
	 * @see javassist.Translator#onLoad(javassist.ClassPool, java.lang.String)
	 */
	@Override
	public void onLoad( final ClassPool pool, final String classname )
	{
		try
		{
			// attempts to read the class file from the source (throws a NotFoundException if
			// the class file cannot be found)
			final CtClass ctClass = pool.get( classname );
			
			// sets the expression editor used to replace the calls to @Diffusive methods with
			// a Diffuser (throws a CannotCompileException if it fails to compile the
			// source code produced by the expression editor)
			ctClass.instrument( expressionEditor );
			
			// log the interception
			if( LOGGER.isDebugEnabled() )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Intercepted the loading of:" + Constants.NEW_LINE );
				message.append( "  Class Name: " + classname + Constants.NEW_LINE );
				message.append( "  Class Pool: " + pool.toString() );
				LOGGER.debug( message.toString() );
			}
		}
		catch( NotFoundException exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Attempted to intercept the loading of the class: " + classname + Constants.NEW_LINE );
			message.append( "But could not find its class file to load." + Constants.NEW_LINE );
			message.append( "  Class Pool: " + pool.toString() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
		catch( CannotCompileException exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Attempted to intercept the loading of the class: " + classname + Constants.NEW_LINE );
			message.append( "But could not compile the replacement source code produced by the expression editor." + Constants.NEW_LINE );
			message.append( "  Expression Editor: " + expressionEditor.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Class Pool: " + pool.toString() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
	}
}
