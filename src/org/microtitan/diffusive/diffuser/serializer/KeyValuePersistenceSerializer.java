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

import org.freezedry.persistence.KeyValuePersistence;

/**
 * Serializes objects into a list of key-value pairs and deserializes key-value pairs back into objects using the
 * FreezeDry framework's {@link KeyValuePersistence} engine.
 * 
 * @author Robert Philipp
 */
public class KeyValuePersistenceSerializer extends PersistenceSerializer {

	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects into and out of JSON
	 */
	public KeyValuePersistenceSerializer()
	{
		super( new KeyValuePersistence() );
	}
	
}
