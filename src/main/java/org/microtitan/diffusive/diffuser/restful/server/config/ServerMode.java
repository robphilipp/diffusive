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
package org.microtitan.diffusive.diffuser.restful.server.config;

/**
 * Defines the modes for configuration of the RESTful diffuser server.
 * 
 * @author Robert Philipp
 */
public enum ServerMode {
	
	CLASS( "class_server" ),
	REMOTE( "remote_server" );
	
	private String serverMode;
	private ServerMode( final String serverType )
	{
		this.serverMode = serverType;
	}
	
	public String getName()
	{
		return serverMode;
	}
	
	public static ServerMode getServerType( final String serverType )
	{
		for( ServerMode type : values() )
		{
			if( type.getName().equals( serverType ) )
			{
				return type;
			}
		}
		return null;
	}
}
