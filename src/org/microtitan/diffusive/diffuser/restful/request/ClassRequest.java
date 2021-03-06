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
package org.microtitan.diffusive.diffuser.restful.request;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClassRequest {
	
	private final String baseUri;
	private final String className;
	
	/**
	 * Constructs a new {@link ClassRequest} with the specified URI base URI to the
	 * resource and the fully qualified class name for the {@link Class} to retrieve. The full
	 * URI to the {@link Class} is given by concatenating  the base URI and the class name.	 
	 * @param baseUri the base {@link URI} to the web resource
	 * @param className the fully qualified class name of the {@link Class} to retrieve
	 */
	public ClassRequest( final String baseUri, final String className )
	{
		this.baseUri = baseUri;
		this.className = className;
	}
	
	/**
	 * Creates and returns a new {@link ClassRequest} with the specified URI base URI to the
	 * resource and the fully qualified class name for the {@link Class} to retrieve. The full
	 * URI to the {@link Class} is given by concatenating  the base URI and the class name.
	 * @param baseUri the base {@link URI} to the web resource
	 * @param className the fully qualified class name of the {@link Class} to retrieve
	 * @return a new {@link ClassRequest}
	 */
	public static ClassRequest create( final String baseUri, final String className )
	{
		return new ClassRequest( baseUri, className );
	}

	/**
     * @return the base {@link URI} to the web resource
     */
    public String getBaseUri()
    {
    	return baseUri;
    }

	/**
     * @return the fully qualified class name of the {@link Class} to retrieve
     */
    public String getClassName()
    {
    	return className;
    }

    /**
     * @return constructs and returns the full URI to the class that is to be loaded
     */
	public URI getUri()
	{
		return UriBuilder.fromUri( baseUri ).path( className ).build();
	}
}
