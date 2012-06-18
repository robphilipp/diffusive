package org.microtitan.diffusive.diffuser.restful.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

public class SerializerNameAdapter extends XmlAdapter< String, Serializer > {

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Serializer unmarshal( final String serializerName ) throws Exception
	{
		return SerializerFactory.getInstance().createSerializer( serializerName );
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal( final Serializer serializer ) throws Exception
	{
		return SerializerFactory.getSerializerName( serializer.getClass() );
	}

}