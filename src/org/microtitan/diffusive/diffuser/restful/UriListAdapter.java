package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UriListAdapter extends XmlAdapter< List< String >, List< URI > > {

	private static final UriAdapter URI_ADAPTER = new UriAdapter();
	
	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public List< URI > unmarshal( final List< String > uriStrings ) throws Exception
	{
		final List< URI > uriList = new ArrayList<>();
		for( String uriString : uriStrings )
		{
			uriList.add( URI_ADAPTER.unmarshal( uriString ) );
		}
		return uriList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public List< String > marshal( final List< URI > uriList ) throws Exception
	{
		final List< String > uriStrings = new ArrayList<>();
		for( URI uri : uriList )
		{
			uriStrings.add( URI_ADAPTER.marshal( uri ) );
		}
		return uriStrings;
	}
}
