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
package org.microtitan.diffusive.launcher.config;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.freezedry.persistence.XmlPersistence;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.launcher.DiffusiveLauncher;
import org.microtitan.diffusive.launcher.config.xml.DiffuserStrategyConfigXml;
import org.microtitan.diffusive.launcher.config.xml.RestfulDiffuserConfigXml;

/**
 * TODO figure out how best to allow the configuration of the diffuser strategy.
 * 
 * Configuration class used to configure the Restful Diffuser framework. The method annotated
 * with {@link DiffusiveConfiguration} is the method that will be called by the {@link DiffusiveLauncher} 
 * to configure the framework. Any static method can be used in this manner. For example, one could
 * write a static method that reads from a configuration file and then performs the necessary configuration.
 * 
 * @author Robert Philipp
 * Jul 18, 2012
 */
public class RestfulDiffuserConfig {
	
	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserConfig.class );
	
	/**
	 * The name of the XML configuration file that is read to obtain the configuration settings that are needed
	 * by the RESTful diffuser
	 */
	public static final String XML_CONFIG_FILE_NAME = "restful_diffuser_config.xml";
	
	/**
	 * Method that is called to configure the Diffusive framework. In particular, creates a 
	 * {@link RestfulDiffuser} with the specified diffusion end-points and the class path URI
	 * passed to the remote restful diffuser so that it can load classes for which it needs to 
	 * run diffused methods.
	 * 
	 * Sets the default diffuser into the {@link KeyedDiffuserRepository}. This is a required step
	 * because the byte code engineering needs to know which diffuser to use, and the repository is
	 * the location holding that diffuser.
	 */
	@DiffusiveConfiguration
	public static final void configure()
	{
		// read the RESTful diffuser config file into the configuration object
		final RestfulDiffuserConfigXml config = loadConfig( XML_CONFIG_FILE_NAME );
		
		// now read the diffuser strategy configuration file into the strategy configuration object.
		// recall that the strategy configuration can create the strategy object
		final DiffuserStrategy strategy = loadStrategy( config.getDiffuserStrategyConfigFile(), config.getDiffuserStrategyConfigClass() );
		
		// create a default diffuser, load the diffuser repository, and set the default diffuser
		// into the repository (needed by the Javassist diffuser method replacement)
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( config.getSerializerName() );
		final List< URI > classPaths = config.getClassPathsAsUri();
		final Diffuser diffuser = new RestfulDiffuser( serializer, strategy, classPaths, config.getLaodThreshold() );
		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );
	}
	
	/**
	 * Loads the configuration object from the XML file and returns it. If the configuration file
	 * couldn't be read properly, then returns null.
	 * @param filename The name of the configuration file to read
	 * @return The configuration object or null if the configuration file can't be read properly
	 */
	private static final RestfulDiffuserConfigXml loadConfig( final String filename )
	{
		RestfulDiffuserConfigXml config = null;
		try
		{
			config = new XmlPersistence().read( RestfulDiffuserConfigXml.class, filename );
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
	
//	/**
//	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
//	 * local {@link RestfulDiffuser} can diffuse method calls.
//	 */
//	private static DiffuserStrategy createStrategy( final List< URI > endpoints )
//	{
//		return new RandomDiffuserStrategy( endpoints );
//	}
//
//	/**
//	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
//	 * local {@link RestfulDiffuser} can diffuse method calls.
//	 */
//	private static List< URI > createEndpointList()
//	{
//		final List< URI > endpoints = new ArrayList<>();
//		for( String client : CLIENT_ENDPOINTS )
//		{
//			endpoints.add( URI.create( client + RestfulDiffuserManagerResource.DIFFUSER_PATH ) );
//		}
//		return endpoints;
//	}
//	
//	/**
//	 * @return a {@link List} of {@link URI} that hold the location of endpoints to which the
//	 * local {@link RestfulDiffuser} can diffuse method calls.
//	 */
//	private static DiffuserStrategy createStrategy()
//	{
//		return new RandomDiffuserStrategy( createEndpointList() );
//	}
//	
//	/**
//	 * @return a {@link List} of {@link URI} holding the location from which a {@link RestfulClassLoader}
//	 * can load local classes. To each URI in the list, the resource path is appended.
//	 */
//	private static List< URI > createClassPathList()
//	{
//		final List< URI > endpoints = new ArrayList<>();
//		for( String client : CLASSPATH_URI )
//		{
//			endpoints.add( URI.create( client + RestfulClassPathResource.CLASSPATH_PATH ) );
//		}
//		return endpoints;
//	}
}
