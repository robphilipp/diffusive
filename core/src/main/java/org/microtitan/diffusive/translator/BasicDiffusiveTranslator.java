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

import javassist.*;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.converter.MethodInterceptorEditor;

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

			if( LOGGER.isInfoEnabled() )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Read class file from source: " ).append( Constants.NEW_LINE )
						.append( "  Class Name: " ).append( classname ).append( Constants.NEW_LINE )
						.append( "  Class Pool: " ).append( pool.toString() ).append( Constants.NEW_LINE )
						.append( "  Methods: " ).append( Constants.NEW_LINE );
				for( CtMethod method: ctClass.getMethods() )
				{
					message.append( "    " ).append( method.getName() ).append( "; " ).append( method.getSignature() ).append( Constants.NEW_LINE );
				}
				LOGGER.debug( message.toString() );
			}
			
			// sets the expression editor used to replace the calls to @Diffusive methods with
			// a Diffuser (throws a CannotCompileException if it fails to compile the
			// source code produced by the expression editor)
			ctClass.instrument( expressionEditor );
			
			// log the interception
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug(
						"Intercepted the loading of:" + Constants.NEW_LINE +
						"  Class Name: " + classname + Constants.NEW_LINE +
						"  Class Pool: " + pool.toString()
				);
			}
		}
		catch( NotFoundException exception )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Attempted to intercept the loading of the class: " ).append( classname ).append( Constants.NEW_LINE );
			message.append( "But could not find its class file to load." ).append( Constants.NEW_LINE );
			message.append( "  Class Pool: " ).append( pool.toString() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
		catch( CannotCompileException exception )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Attempted to intercept the loading of the class: " ).append( classname ).append( Constants.NEW_LINE );
			message.append( "But could not compile the replacement source code produced by the expression editor." ).append( Constants.NEW_LINE );
			message.append( "  Expression Editor: " ).append( expressionEditor.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Class Pool: " ).append( pool.toString() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
	}
}
