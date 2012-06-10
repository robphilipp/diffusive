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
