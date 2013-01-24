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

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;

import org.microtitan.diffusive.convertor.MethodIntercepterEditor;

/**
 * Observer of the {@link Loader}, which calls the {@link #start(ClassPool)} and {@link #onLoad(ClassPool, String)}
 * method of an INSTANCE of this object. These methods can be used to translate the byte code before it is loaded.
 * 
 * The {@link DiffusiveTranslator} is intended to intercept method calls to methods that are annotated with {@code @Diffusive}.
 * 
 * @author Robert Philipp
 */
public interface DiffusiveTranslator extends Translator {

	/**
	 * @return The {@link MethodIntercepterEditor} used to create the source code for the intercepted method call
	 */
	MethodIntercepterEditor getExpressionEditor();
	
	/**
	 * Sets the {@link MethodIntercepterEditor} used to create the source code for the intercepted method call
	 * @param expressionEditor the {@link MethodIntercepterEditor} used to create the source code for the 
	 * intercepted method call
	 * @return the {@link MethodIntercepterEditor} that was replaced by the specified one
	 */
	MethodIntercepterEditor setExpressionEditor( final MethodIntercepterEditor expressionEditor );
}
