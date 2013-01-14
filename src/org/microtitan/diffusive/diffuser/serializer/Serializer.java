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
package org.microtitan.diffusive.diffuser.serializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for serialization/deserialization of Java objects for transporting across the
 * network. Implementing classes may chose their unique serialized form for Java objects, and 
 * must provide a mechanism in the {@link #deserialize(InputStream, Class)} method that takes
 * that serialized form back into an object (including the whole object tree).
 *  
 * @author Robert Philipp
 */
public interface Serializer {

	/**
	 * Serializes a Java object and places it into the specified {@link OutputStream} for transporting 
	 * across the network.
	 * <p>NOTE that implementations of this method should be thread safe.
	 * @param object The {@link Object} to serialize
	 * @param output The {@link OutputStream} into which the object is serialized
	 */
	void serialize( final Object object, final OutputStream output );
	
	/**
	 * Reads a Java object representation from the specified {@link InputStream} and creates and 
	 * returns the fully constructed Java object of the specified type.
	 * <p>NOTE that implementations of this method should be thread safe.
	 * @param input The {@link InputStream} from which to read the object representation.
	 * @param clazz The {@link Class} type of the object to be read and returned
	 * @return The Java object represented in the {@link InputStream}
	 */
	< T > T deserialize( final InputStream input, final Class< T > clazz );

}
