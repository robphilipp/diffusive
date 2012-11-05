package org.microtitan.diffusive.launcher.config.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.XmlPersistence;
import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.annotations.PersistCollection;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulClassPathResource;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.launcher.config.ConfigUtils;
import org.microtitan.diffusive.launcher.config.RestfulDiffuserConfig;

public class RestfulDiffuserConfigXml {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserConfigXml.class );
	
//	/**
//	 * List of end-points that newly created diffuser will look to send their requests, if
//	 * the list is empty, then the newly created diffusers will execute ALL requests locally,
//	 * and not be able to diffuse them to other end-points if they are currently busy.
//	 */
////	@PersistCollection(elementPersistName="endPoint")
//	@Persist( ignore=true )
//	private List< String > clientList;
	
	/**
	 * List of end-points that serve up classes to the remote class loader. These end-points will be set
	 * for a newly created diffuser as part of its configuration, so that the new diffuser knows where
	 * to point its class loader in the event that it can't find a class locally.
	 * 
	 * The end-points must be valid {@link URI}.
	 */
	@PersistCollection(elementPersistName="classPath")
	private List< String > classPathList;
	
	/**
	 * For diffusers created through calls to the server, this is the load threshold value.
	 * the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	 * unless of course, there are no client end-points specified. When the threshold is below the 
	 * load threshold, the diffuser will call the local diffuser to execute the tasks.
	 */
	private double loadThreshold;
	
	/**
	 * The name of the serializer used to serialize objects that get pass back and forth between the diffusers.
	 * The type of serialization doesn't, in and of itself, matter. What matters is that the diffusers use the
	 * same method of serialization so that the receiving diffuser can reconstruct (de-serialize) the object.
	 * The serializer is created from this name using the {@link SerializerFactory}.
	 */
	private String serializerName;
	
	// use the strategy class to pass to the persistence, along with the file name holding
	// the strategy, need to add an ignore the strategy (don't persistence)
	/**
	 * The name of the strategy class. This gets persisted, and is passed to the persistence reader
	 * to load the strategey from the strategy file
	 */
	private String strategyConfigClassName;
	
	/**
	 * The name of the configuration file (XML) that holds the configuration for the strategy. This is
	 * the XML file that the persistence reader uses to load the strategy configuration.
	 */
	private String strategyConfigFile;
	
	/**
	 * The strategy that is reconstructed from the XML persistence. This is not persisted, because it is
	 * persisted to a separated file.
	 */
	@Persist( ignore=true )
	private DiffuserStrategy diffuserStrategy;
	
//	/**
//	 * @return {@link List} of end-points that the newly created diffusers will use to send
//	 * their requests.
//	 */
//	public List< String > getClientEndpoints()
//	{
//		return clientList;
//	}
//	
//	/**
//	 * Validates and removes invalid client end points, and then sets the end point list
//	 * to the list of valid end points. 
//	 * @param clientEndpoints
//	 */
//	public void setClientEndpoints( final List< String > clientEndpoints )
//	{
//		this.clientList = ConfigUtils.validateEndpoints( clientEndpoints );
//	}
//
//	/**
//	 * @return {@link List} of end-points that the newly created diffusers will use to send
//	 * their requests.
//	 */
//	public List< URI > getClientEndpointsAsUri()
//	{
//		return ConfigUtils.createEndpointList( clientList );
//	}
	
	/**
	 * @return The list of class path end-points from which newly created diffusers can load
	 * classes remotely.
	 */
	public List< String > getClassPathList()
	{
		return classPathList;
	}

	/**
	 * @return The {@link List} of class path {@link URI}
	 */
	public List< URI > getClassPathsAsUri()
	{
		return ConfigUtils.createEndpointList( classPathList );
	}

	/**
	 * Sets the list of class path end-points from which newly created diffusers can remotely
	 * load classes needed for execution of the diffused methods.
	 * @param classPathList The list of class path end-points from which newly created diffusers can remotely
	 * load classes needed for execution of the diffused methods.
	 */
	public void setClassPaths( final List< String > classPathList )
	{
		this.classPathList = classPathList;
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
	 * @return The name of the serializer used to serialize and reconstruct (de-serialize) the objects
	 * passed between the diffusers.
	 * @see SerializerFactory for infomration about the names and the standard available names.
	 */
	public String getSerializerName()
	{
		return serializerName;
	}

	/**
	 * Sets the name of the serializer used to serialize and reconstruct (de-serialize) the objects
	 * passed between the diffusers.
	 * @param serializerName The name of the serializer used to serialize and reconstruct (de-serialize) the objects
	 * passed between the diffusers.
	 * @see SerializerFactory for infomration about the names and the standard available names.
	 */
	public void setSerializerName( String serializerName )
	{
		this.serializerName = serializerName;
	}
	
//	public DiffuserStrategy getDiffuserStrategy()
//	{
//		return diffuserStrategy;
//	}
//	
//	public void setDiffuserStrategy( final RandomDiffuserStrategy diffuserStrategy )
//	{
//		this.diffuserStrategy = diffuserStrategy;
//	}

	/**
	 * @return The fully qualified class name of the {@link Class} implementing the diffuser strategy
	 */
	public String getDiffuserStrategyConfigClassName()
	{
		return strategyConfigClassName;
	}
	
	/**
	 * @return the {@link Class} of the diffuser strategy
	 */
	@SuppressWarnings( "unchecked" )
	public Class< ? extends DiffuserStrategyConfigXml > getDiffuserStrategyConfigClass()
	{
		Class< ? extends DiffuserStrategyConfigXml > strategyClazz = null;
		try
		{
			strategyClazz = (Class< ? extends DiffuserStrategyConfigXml >)Class.forName( strategyConfigClassName );
		}
		catch( ClassNotFoundException e )
		{
			final String message = "Error: Failed to load class: " + strategyConfigClassName;
			LOGGER.error( message, e );
			throw new IllegalStateException( message, e );
		}
		return strategyClazz;
	}

	/**
	 * Sets the fully qualified class name of the {@link Class} implementing the diffuser strategy
	 * @param strategyClassName The fully qualified class name of the {@link Class} implementing the 
	 * diffuser strategy
	 */
	public void setDiffuserStrategyConfigClassName( String strategyClassName )
	{
		this.strategyConfigClassName = strategyClassName;
	}

	/**
	 * @return the name of the XML file holding the strategy configuration
	 */
	public String getDiffuserStrategyConfigFile()
	{
		return strategyConfigFile;
	}

	/**
	 * Sets the name of the XML file holding the strategy configuration.
	 * @param strategyConfigFile the name of the XML file holding the strategy configuration
	 */
	public void setDiffuserStrategyConfigFile( String strategyConfigFile )
	{
		this.strategyConfigFile = strategyConfigFile;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer();
//		if( clientList != null )
//		{
//			rep.append( "Client End-Point List:" + Constants.NEW_LINE );
//			for( String endpoint : clientList )
//			{
//				rep.append( "  " + endpoint + Constants.NEW_LINE );
//			}
//		}
		rep.append( "Class Path List:" + Constants.NEW_LINE );
		for( String path : classPathList )
		{
			rep.append( "  " + path + Constants.NEW_LINE );
		}
		rep.append( "Load Threshold: " + loadThreshold + Constants.NEW_LINE );
		rep.append( "Serializer Name: " + serializerName + Constants.NEW_LINE );
//		rep.append( "Diffuser Strategy Type: " + diffuserStrategy.getClass().getName() + Constants.NEW_LINE );
//		rep.append( "Diffuser Strategy: " + Constants.NEW_LINE );
//		rep.append( diffuserStrategy.toString() );
		
		return rep.toString();
	}

	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.WARN );

		final RestfulDiffuserConfigXml xmlConfig = new RestfulDiffuserConfigXml();
		final List< String > endpoints = new ArrayList<>( Arrays.asList( "http://192.168.1.4:8182" + RestfulDiffuserManagerResource.DIFFUSER_PATH ) );
//		xmlConfig.setClientEndpoints( endpoints );
		xmlConfig.setLaodThreshold( 0.75 );
		xmlConfig.setSerializerName( SerializerFactory.SerializerType.PERSISTENCE_XML.getName() );
		xmlConfig.setClassPaths( Arrays.asList( RestfulDiffuserServer.DEFAULT_SERVER_URI + RestfulClassPathResource.CLASSPATH_PATH ) );
//		xmlConfig.setDiffuserStrategy( new RandomDiffuserStrategy( xmlConfig.getClientEndpointsAsUri() ) );
		xmlConfig.setDiffuserStrategyConfigClassName( RestfulDiffuserConfig.XML_STRATEGY_CONFIG_CLASS_NAME );
		xmlConfig.setDiffuserStrategyConfigFile( RestfulDiffuserConfig.XML_STRATEGY_CONFIG_FILE_NAME );
		
		// write out the diffuser configuration file file
		new XmlPersistence().write( xmlConfig, RestfulDiffuserConfig.XML_CONFIG_FILE_NAME );
		
		// write out the diffuser-strategy configuration file
		final RandomDiffuserStrategyConfigXml xmlStrategyConfig = new RandomDiffuserStrategyConfigXml();
//		xmlStrategyConfig.setClientEndpoints( xmlConfig.getClientEndpoints() );
		xmlStrategyConfig.setClientEndpoints( endpoints );
		xmlStrategyConfig.setRandomSeed( 0 );
		new XmlPersistence().write( xmlStrategyConfig, RestfulDiffuserConfig.XML_STRATEGY_CONFIG_FILE_NAME );
		
		// read the config file back in to test
		final XmlPersistence persist = new XmlPersistence();
		final RestfulDiffuserConfigXml config = persist.read( RestfulDiffuserConfigXml.class, 
															  RestfulDiffuserConfig.XML_CONFIG_FILE_NAME );
		System.out.println( config.toString() );
		
		final DiffuserStrategyConfigXml strategyConfig = persist.read( config.getDiffuserStrategyConfigClass(), 
															   		   config.getDiffuserStrategyConfigFile() );
//		config.setClientEndpoints( strategyConfig.getClientEndpoints() );
		
	}
}
