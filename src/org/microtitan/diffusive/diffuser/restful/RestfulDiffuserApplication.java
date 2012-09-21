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
package org.microtitan.diffusive.diffuser.restful;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Defines the resources that are created per request and only once (singletons) for the JAX-RS application.
 * 
 * @author rob
 */
public class RestfulDiffuserApplication extends Application {

	private Set< Object > singletonResources;
	private Set< Class< ? > > perRequestResources;

	/**
	 * Constructs the application object with the specified singleton objects, and the per-request classes.
	 * @param singletonResources The resources that are singletons, and therefore, live across many requests.
	 * @param perRequestResClasses The resources that are create for each request, and then destroyed.
	 */
	public RestfulDiffuserApplication( final Set< Object > singletonResources, final Set< Class< ? > > perRequestResClasses )
	{
		this.singletonResources = singletonResources;
		this.perRequestResources = perRequestResClasses;
	}
	
	/**
	 * Basic no-arg constructor
	 */
	public RestfulDiffuserApplication() {}
	
	/**
	 * @return the {@link Set} of singleton resource objects
	 */
	private Set< Object > getSingletonResources()
	{
		if( singletonResources == null )
		{
			this.singletonResources = new HashSet<>();
		}
		return singletonResources;
	}
	
	/**
	 * @return the {@link Set} of resources that are create for each request 
	 */
	private Set< Class< ? > > getPerRequestResources()
	{
		if( perRequestResources == null )
		{
			this.perRequestResources = new HashSet<>();
		}
		return perRequestResources;
	}
	
	/**
	 * Adds an object as a resource that spans multiple requests
	 * @param singletonResource The resource object that is to be a singleton resource
	 * @return true if the resource object was added; false otherwise
	 */
	public boolean addSingletonResource( final Object singletonResource )
	{
		return getSingletonResources().add( singletonResource );
	}
	
	/**
	 * Adds a {@link Class} to the set of resources that will be created for each request
	 * @param perRequestResource A {@link Class} that will act as a resource that will be created for each request
	 * @return true if the resource's {@link Class} was added; false otherwise
	 */
	public boolean addPerRequestResource( final Class< ? > perRequestResource )
	{
		return getPerRequestResources().add( perRequestResource );
	}
	
	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set< Class< ? > > getClasses()
	{
		return getPerRequestResources();
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getSingletons()
	 */
	@Override
	public Set< Object > getSingletons()
	{
		return getSingletonResources();
	}
}
