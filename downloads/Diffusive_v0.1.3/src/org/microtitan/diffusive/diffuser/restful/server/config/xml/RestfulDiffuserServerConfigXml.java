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
package org.microtitan.diffusive.diffuser.restful.server.config.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.XmlPersistence;
import org.freezedry.persistence.annotations.PersistCollection;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.config.RestfulDiffuserServerConfig;

/**
 * Simple utility class that can be used to generate the skeleton of the server configuration XML file.
 * The {@link RestfulDiffuserServerConfig} class then uses an instance of this object, created by reading
 * the XML configuration file to configure the RESTful diffuser server.
 * 
 * Use the main method to generate a skeleton configuration file when you want to generate it programmatically.
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserServerConfigXml {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserServerConfigXml.class );
	
	/**
	 * This is the diffuser path that is also specified in the {@link RestfulDiffuserManagerResource#DIFFUSER_PATH}
	 * and should match that value.
	 */
	private static final String DIFFUSER_PATH = RestfulDiffuserManagerResource.DIFFUSER_PATH;
	
	/**
	 * List of end-points that newly created diffuser will look to send their requests, if
	 * the list is empty, then the newly created diffusers will execute ALL requests locally,
	 * and not be able to diffuse them to other end-points if they are currently busy.
	 */
	@PersistCollection(elementPersistName="endPoint")
	private List< String > clientList;
	
	/**
	 * For diffusers created through calls to the server, this is the load threshold value.
	 * the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	 * unless of course, there are no client end-points specified. When the threshold is below the 
	 * load threshold, the diffuser will call the local diffuser to execute the tasks.
	 */
	private double loadThreshold;
	
	/**
	 * @return {@link List} of end-points that the newly created diffusers will use to send
	 * their requests.
	 */
	public List< String > getClientEndpoints()
	{
		return clientList;
	}
	
	/**
	 * Validates and removes invalid client end points, and then sets the end point list
	 * to the list of valid end points. 
	 * @param clientEndpoints
	 */
	public void setClientEndpoints( final List< String > clientEndpoints )
	{
		this.clientList = validateEndpoints( clientEndpoints );
	}

	/**
	 * @return {@link List} of end-points that the newly created diffusers will use to send
	 * their requests.
	 */
	public List< URI > getClientEndpointsAsUri()
	{
		return createEndpointList( clientList );
	}
	
	/**
	 * @return the threshold above which load the diffuser is to diffuse any tasks to
	 * a remote diffuser.
	 */
	public double getLaodThreshold()
	{
		return loadThreshold;
	}

	/**
	 * Sets the threshold above which load the diffuser is to diffuse any tasks to
	 * a remote diffuser.
	 * @param loadThreshold The load threshold, should be a value between 0.0 and 1.0
	 */
	public void setLaodThreshold( final double loadThreshold )
	{
		if( loadThreshold >= 0.0 && loadThreshold <= 1.0 )
		{
			this.loadThreshold = loadThreshold;
		}
	}
	
	/**
	 * @return a {@link List} of {@link URI} that hold the location of end-points to which the
	 * local {@link RestfulDiffuser} can diffuse method calls.
	 */
	private static List< URI > createEndpointList( final List< String > clientEndpoints )
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : clientEndpoints )
		{
			endpoints.add( URI.create( client ) );
		}
		return endpoints;
	}
	
	/**
	 * 
	 * @param clientEndpoints
	 * @return
	 */
	private List< String > validateEndpoints( final List< String > clientEndpoints )
	{
		final List< String > endpoints = new ArrayList<>();
		for( String client : clientEndpoints )
		{
			URI uri;
			try
			{
				uri = new URI( client );
				endpoints.add( uri.getScheme() + "://" + uri.getHost() +":" + uri.getPort() + uri.getPath() );
			}
			catch( URISyntaxException e )
			{
				final String message = "Invalid URI for end-point: " + client;
				LOGGER.warn( message, e );
			}
		}
		return endpoints;
	}

	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.WARN );

		final RestfulDiffuserServerConfigXml xmlConfig = new RestfulDiffuserServerConfigXml();
		xmlConfig.setClientEndpoints( Arrays.asList( "http://192.168.1.4:8182" + DIFFUSER_PATH ) );
		xmlConfig.setLaodThreshold( 0.75 );
		
		// write out the config file
		new XmlPersistence().write( xmlConfig, RestfulDiffuserServerConfig.XML_CONFIG_FILE_NAME );
		
		// read the config file back in to test
		final XmlPersistence persist = new XmlPersistence();
		final RestfulDiffuserServerConfigXml config = persist.read( RestfulDiffuserServerConfigXml.class, 
																	RestfulDiffuserServerConfig.XML_CONFIG_FILE_NAME );
		
		for( String uri : config.getClientEndpoints() )
		{
			System.out.println( uri );
		}
		System.out.println( config.getLaodThreshold() );
	}

}
