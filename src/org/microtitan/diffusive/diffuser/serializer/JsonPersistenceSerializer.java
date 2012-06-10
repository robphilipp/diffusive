package org.microtitan.diffusive.diffuser.serializer;

import org.freezedry.persistence.JsonPersistence;

/**
 * Serializes objects into JSON and deserializes JSON back into objects using the
 * FreezeDry framework's {@link JsonPersistence} engine.
 * 
 * @author Robert Philipp
 */
public class JsonPersistenceSerializer extends PersistenceSerializer {

	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects into and out of JSON
	 */
	public JsonPersistenceSerializer()
	{
		super( new JsonPersistence() );
	}
	
}
