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
package org.microtitan.diffusive.diffuser.restful.client;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.sun.jersey.api.client.Client;

public class RestfulClientFactory {

	private static Client instance = null;
	
	private RestfulClientFactory() {}
	
	public static Client getInstance()
	{
		synchronized( RestfulClientFactory.class )
		{
			if( instance == null )
			{
				// lazily create abdera
				instance = Client.create();
			}

			return instance;
		}
	}
	
	public static void main( String[] args ) throws IOException
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
	}
}
