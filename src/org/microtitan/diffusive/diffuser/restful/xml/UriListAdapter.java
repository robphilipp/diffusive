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
package org.microtitan.diffusive.diffuser.restful.xml;

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
