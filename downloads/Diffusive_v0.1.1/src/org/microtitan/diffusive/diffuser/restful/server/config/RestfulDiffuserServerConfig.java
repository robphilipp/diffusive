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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.annotations.DiffusiveServerConfiguration;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.KeyedDiffusiveStrategyRepository;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;

/**
 * Configuration used by the {@link RestfulDiffuserManagerResource} so that it can supply
 * newly created {@link RestfulDiffuser}s with their diffuser strategy (end-points, weights,
 * selection criteria, etc)
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserServerConfig {

	// list of end-points that newly created diffuser will look to send their requests, if
	// the list is empty, then the newly created diffusers will execute ALL requests locally,
	// and not be able to diffuse them to other end-points if they are currently busy.
	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( "http://192.168.1.4:8182" );
//	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList();
	
	// for diffusers created through calls to the server, this is the load threshold value.
	// the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	// unless of course, there are no client end-points specified. When the threshold is below the 
	// load threshold, the diffuser will call the local diffuser to execute the tasks.
	public static final double LOAD_THRESHOLD = 0.75;

	/**
	 * Creates the list of end-points, the default strategy based on those end-points,
	 * the keyed diffuser strategy repository, and then sets the strategy in the repository.
	 */
	@DiffusiveServerConfiguration
	public static final void configure()
	{
		final List< URI > clientEndpoints = createEndpointList();
		final DiffuserStrategy strategy = new RandomDiffuserStrategy( clientEndpoints );
		KeyedDiffusiveStrategyRepository.getInstance().setValues( strategy, LOAD_THRESHOLD );
	}
	
	/**
	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
	 * local {@link RestfulDiffuser} can diffuse method calls.
	 */
	private static List< URI > createEndpointList()
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : CLIENT_ENDPOINTS )
		{
			endpoints.add( URI.create( client + RestfulDiffuserManagerResource.DIFFUSER_PATH ) );
		}
		return endpoints;
	}
	
}
