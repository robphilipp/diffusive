package org.microtitan.diffusive.diffuser.restful.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converts between the {@link Class} and the class name representation
 * @author Robert Philipp
 */
public class ClassNameAdapter extends XmlAdapter< String, Class< ? > > {

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Class< ? > unmarshal( final String className ) throws Exception
	{
		return Class.forName( className );
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal( final Class< ? > clazz )
	{
		return clazz.getName();
//		return URLEncoder.encode( clazz.getName(), "utf-8" );
	}

}
