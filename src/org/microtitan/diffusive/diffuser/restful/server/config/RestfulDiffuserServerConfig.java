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

import org.freezedry.persistence.XmlPersistence;
import org.microtitan.diffusive.annotations.DiffusiveServerConfiguration;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.KeyedDiffusiveStrategyRepository;
import org.microtitan.diffusive.diffuser.restful.server.config.xml.RestfulDiffuserServerConfigXml;
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

	/**
	 * List of end-points that newly created diffuser will look to send their requests, if
	 * the list is empty, then the newly created diffusers will execute ALL requests locally,
	 * and not be able to diffuse them to other end-points if they are currently busy.
	 */
	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList();
	
	/**
	 * For diffusers created through calls to the server, this is the load threshold value.
	 * the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	 * unless of course, there are no client end-points specified. When the threshold is below the 
	 * load threshold, the diffuser will call the local diffuser to execute the tasks.
	 */
	public static final double LOAD_THRESHOLD = 0.75;
	
	/**
	 * The name of the XML configuration file that is read
	 */
	public static final String XML_CONFIG_FILE_NAME = "restful_server_config.xml";

	/**
	 * Creates the list of end-points, the default strategy based on those end-points,
	 * the keyed diffuser strategy repository, and then sets the strategy in the repository.
	 */
	@DiffusiveServerConfiguration
	public static final void configure()
	{
		// laod the configuration file
		final RestfulDiffuserServerConfigXml config = loadConfig( XML_CONFIG_FILE_NAME );
		
		// if the config file was read properly, then use it to for configuration,
		// otherwise use the default values from this class 
		List< URI > clientEndpoints = null;
		double loadThreshold;
		if( config != null )
		{
			clientEndpoints = config.getClientEndpointsAsUri();
			loadThreshold = config.getLaodThreshold();
		}
		else
		{
			clientEndpoints = createEndpointList();
			loadThreshold = LOAD_THRESHOLD;
		}
		
		// set up the strategy and the strategy repository
		final DiffuserStrategy strategy = new RandomDiffuserStrategy( clientEndpoints );
		KeyedDiffusiveStrategyRepository.getInstance().setValues( strategy, loadThreshold );
	}
	
	/**
	 * Loads the configuration object from the XML file and returns it. If the configuration file
	 * couldn't be read properly, then returns null.
	 * @param filename The name of the configuration file to read
	 * @return The configuration object or null if the configuration file can't be read properly
	 */
	private static final RestfulDiffuserServerConfigXml loadConfig( final String filename )
	{
		RestfulDiffuserServerConfigXml config = null;
		try
		{
			config = new XmlPersistence().read( RestfulDiffuserServerConfigXml.class, filename );
		}
		catch( IllegalArgumentException e )
		{
			// really nothing to do..if it didn't load
		}
		return config;
	}
	
	/**
	 * @return a {@link List} of {@link URI} that hold the location of end points to which the
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
