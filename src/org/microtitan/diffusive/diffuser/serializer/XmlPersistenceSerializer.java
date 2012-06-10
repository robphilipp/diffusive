package org.microtitan.diffusive.diffuser.serializer;

import org.freezedry.persistence.XmlPersistence;

/**
 * Serializes objects into XML and deserializes XML back into objects using the
 * FreezeDry framework's {@link XmlPersistence} engine.
 * 
 * @author Robert Philipp
 */
public class XmlPersistenceSerializer extends PersistenceSerializer {

	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects into and out of XML
	 */
	public XmlPersistenceSerializer()
	{
		super( new XmlPersistence() );
	}
	
}
