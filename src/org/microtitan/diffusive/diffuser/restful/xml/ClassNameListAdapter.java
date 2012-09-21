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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ClassNameListAdapter extends XmlAdapter< List< String >, List< Class< ? > > > {
	
	private static final ClassNameAdapter CLASS_NAME_ADAPTER = new ClassNameAdapter();

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public List< Class< ? > > unmarshal( final List< String > classNames ) throws Exception
	{
		final List< Class< ? > > classList = new ArrayList<>();
		for( String className : classNames )
		{
			classList.add( CLASS_NAME_ADAPTER.unmarshal( className ) );
		}
		return classList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public List< String > marshal( final List< Class< ? > > classes )
	{
		final List< String > nameList = new ArrayList<>();
		for( Class< ? > clazz : classes )
		{
			nameList.add( CLASS_NAME_ADAPTER.marshal( clazz ) );
		}
		return nameList;
	}
}
