package org.microtitan.diffusive.translator;

import org.microtitan.diffusive.convertor.MethodInterceptorEditor;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;

/**
 * Observer of the {@link Loader}, which calls the {@link #start(ClassPool)} and {@link #onLoad(ClassPool, String)}
 * method of an instance of this object. These methods can be used to translate the byte code before it is loaded.
 * 
 * The {@link DiffusiveTranslator} is intended to intercept method calls to methods that are annotated with {@code @Diffusive}.
 * 
 * @author Robert Philipp
 */
public interface DiffusiveTranslator extends Translator {

	/**
	 * @return The {@link MethodInterceptorEditor} used to create the source code for the intercepted method call
	 */
	MethodInterceptorEditor getExpressionEditor();
	
	/**
	 * Sets the {@link MethodInterceptorEditor} used to create the source code for the intercepted method call
	 * @param expressionEditor the {@link MethodInterceptorEditor} used to create the source code for the 
	 * intercepted method call
	 * @return the {@link MethodInterceptorEditor} that was replaced by the specified one
	 */
	MethodInterceptorEditor setExpressionEditor( final MethodInterceptorEditor expressionEditor );
}
