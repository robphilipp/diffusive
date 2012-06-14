package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UriAdapter extends XmlAdapter< String, URI > {

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public URI unmarshal( final String uriString ) throws Exception
	{
		return URI.create( uriString );
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal( final URI uri ) throws Exception
	{
		return uri.toString();
	}
}
