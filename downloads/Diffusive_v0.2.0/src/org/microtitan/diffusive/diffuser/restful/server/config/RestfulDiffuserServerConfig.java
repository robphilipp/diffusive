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

import org.apache.log4j.Logger;
import org.freezedry.persistence.XmlPersistence;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveServerConfiguration;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.KeyedDiffusiveStrategyRepository;
import org.microtitan.diffusive.diffuser.restful.server.config.xml.RestfulDiffuserServerConfigXml;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategyConfigXml;

/**
 * Configuration used by the {@link RestfulDiffuserManagerResource} so that it can supply
 * newly created {@link RestfulDiffuser}s with their diffuser strategy (end-points, weights,
 * selection criteria, etc)
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserServerConfig {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserServerConfig.class );

	/**
	 * Creates the list of end-points, the default strategy based on those end-points,
	 * the keyed diffuser strategy repository, and then sets the strategy in the repository.
	 */
	@DiffusiveServerConfiguration
	public static final void configure( final String configFileName )
	{
		// laod the configuration file
		final RestfulDiffuserServerConfigXml config = loadConfig( configFileName );
		
		// grab the information from the diffuser server configuration object to load the strategy
		final DiffuserStrategy strategy = loadStrategy( config.getDiffuserStrategyConfigFile(), config.getDiffuserStrategyConfigClass() );
		
		// grab the load threshold
		final double loadThreshold = config.getLaodThreshold();
		
		// set up the strategy and the strategy repository
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
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to load or read configuration file" + Constants.NEW_LINE );
			message.append( "  Configuration File Name: " + filename + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return config;
	}
	
	/**
	 * Loads the {@link DiffuserStrategy} from the specified file name and its associated XML configuration
	 * {@link Class}. Note that the XML configuration {@link Class} must have a generic argument that extends
	 * the {@link DiffuserStrategyConfigXml} class.
	 * @param fileName The name of the file containing the configuration information for creating the {@link DiffuserStrategy}.
	 * @param clazz The {@link Class} into which the configuration information is loaded. Note that the class
	 * must extend the {@link DiffuserStrategyConfigXml} class.
	 * @return The loaded {@link DiffuserStrategy}
	 */
	private static final DiffuserStrategy loadStrategy( final String fileName, final Class< ? extends DiffuserStrategyConfigXml > clazz )
	{
		DiffuserStrategy strategy = null;
		try
		{
			// read the configuration file into the configuration object...
			final DiffuserStrategyConfigXml config = new XmlPersistence().read( clazz, fileName );
			
			// ...and have the configuration object create the strategy 
			strategy = config.createStrategy();
		}
		catch( IllegalArgumentException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to load or read configuration file" + Constants.NEW_LINE );
			message.append( "  Configuration File Name: " + fileName + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return strategy;
	}
}
