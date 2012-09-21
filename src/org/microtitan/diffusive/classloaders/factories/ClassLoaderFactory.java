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
package org.microtitan.diffusive.classloaders.factories;

import java.net.URI;
import java.util.List;

/**
 * Interface that defines a {@link ClassLoaderFactory} intended for loading classes over the network
 * 
 * @author Robert Philipp
 */
public interface ClassLoaderFactory {
	
	/**
	 * Creates a {@link ClassLoader} for loading classes over the network. Should default the parent
	 * class loader to the loader that loads this class.
	 * @param baseSignature The signature of the base method that shouldn't be diffused further
	 * @param classPaths The list of {@link URI} specifying the web service end-point for
	 * retrieving serialized {@link Class} objects
	 * @return The {@link ClassLoader} for loading classes over the network
	 */
	ClassLoader create( final String baseSignature, final List< URI > classPaths );

	/**
	 * Creates a {@link ClassLoader} for loading classes over the network
	 * @param parentLoader The parent class loader
	 * @param baseSignature The signature of the base method that shouldn't be diffused further
	 * @param classPaths The list of {@link URI} specifying the web service end-point for
	 * retrieving serialized {@link Class} objects
	 * @return The {@link ClassLoader} for loading classes over the network
	 */
	ClassLoader create( final ClassLoader parentLoader, final String baseSignature, final List< URI > classPaths );
}
