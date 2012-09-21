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
