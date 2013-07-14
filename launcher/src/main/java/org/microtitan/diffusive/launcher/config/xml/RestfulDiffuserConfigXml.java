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
package org.microtitan.diffusive.launcher.config.xml;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.XmlPersistence;
import org.freezedry.persistence.annotations.Persist;
import org.freezedry.persistence.annotations.PersistCollection;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.config.StrategyType;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory.SerializerType;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategyConfigXml;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategyConfigXml;
import org.microtitan.diffusive.diffuser.strategy.RandomWeightedDiffuserStrategyConfigXml;
import org.microtitan.diffusive.launcher.DiffusiveLauncher;
import org.microtitan.diffusive.launcher.config.ConfigUtils;
import org.microtitan.diffusive.utils.NetworkUtils;

/**
 * Configuration object for the REStful diffuser launcher. This object is persisted as XML and
 * and the XML configuration is loaded into an object of this class for use by the {@link org.microtitan.diffusive.launcher.config.RestfulDiffuserConfig}
 * object that configures the launcher.
 *  
 * @author Robert Philipp
 */
public class RestfulDiffuserConfigXml {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserConfigXml.class );
	
	/**
	 * The name of the XML configuration file holding the diffuser strategy configuration.
	 * NOTE: This field is only used in the main(...) class for testing and to help create
	 * configuration files for strategies 
	 */
	public static final String XML_STRATEGY_CONFIG_FILE_NAME = "random_diffuser_strategy.xml";
//	public static final String XML_STRATEGY_CONFIG_FILE_NAME = "random_weighted_diffuser_strategy.xml";
	
	/**
	 * The fully qualified class name of the {@link Class} of the diffuser strategy implementation
	 * NOTE: This field is only used in the main(...) class for testing and to help create
	 * configuration files for strategies 
	 */
	public static final String XML_STRATEGY_CONFIG_CLASS_NAME = RandomDiffuserStrategyConfigXml.class.getName();
//	public static final String XML_STRATEGY_CONFIG_CLASS_NAME = RandomWeightedDiffuserStrategyConfigXml.class.getName();

	/**
	 * List of end-points that serve up classes to the remote class loader. These end-points will be set
	 * for a newly created diffuser as part of its configuration, so that the new diffuser knows where
	 * to point its class loader in the event that it can't find a class locally.
	 * 
	 * The end-points must be valid {@link java.net.URI}.
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
	 * The serializer is created from this name using the {@link org.microtitan.diffusive.diffuser.serializer.SerializerFactory}.
	 */
	private String serializerName;
	
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
	
	/**
	 * @return The list of class path end-points from which newly created diffusers can load
	 * classes remotely.
	 */
	public List< String > getClassPathList()
	{
		return classPathList;
	}

	/**
	 * @return The {@link java.util.List} of class path {@link java.net.URI}
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
	public void setLoadThreshold( final double loadThreshold )
	{
		if( loadThreshold >= 0.0 && loadThreshold <= 1.0 )
		{
			this.loadThreshold = loadThreshold;
		}
	}

	/**
	 * @return The name of the serializer used to serialize and reconstruct (de-serialize) the objects
	 * passed between the diffusers.
	 * @see org.microtitan.diffusive.diffuser.serializer.SerializerFactory for infomration about the names and the standard available names.
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
	 * @see org.microtitan.diffusive.diffuser.serializer.SerializerFactory for infomration about the names and the standard available names.
	 */
	public void setSerializerName( String serializerName )
	{
		this.serializerName = serializerName;
	}
	
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
		rep.append( "Class Path List:" + Constants.NEW_LINE );
		for( String path : classPathList )
		{
			rep.append( "  " + path + Constants.NEW_LINE );
		}
		rep.append( "Load Threshold: " + loadThreshold + Constants.NEW_LINE );
		rep.append( "Serializer Name: " + serializerName + Constants.NEW_LINE );
		
		return rep.toString();
	}

	
	/**
	 * Enumeration of the usage mode for this utility
	 *  
	 * @author Robert Philipp
	 */
	private static enum UsageMode {
		GENERATE( "generate" ),
		VALIDATE( "validate" );
		
		private String mode;
		private UsageMode( final String mode )
		{
			this.mode = mode;
		}
		
		public String getName()
		{
			return mode;
		}
		
		public static UsageMode getMode( final String mode )
		{
			for( UsageMode type : values() )
			{
				if( type.getName().equals( mode ) )
				{
					return type;
				}
			}
			return null;
		}
	}
	
	/**
	 * Validate that the utility mode string was a valid option.
	 * @param modeSpec The option specification for the mode
	 * @param options The parsed command-line optoins
	 * @return The {@link org.microtitan.diffusive.launcher.config.xml.RestfulDiffuserConfigXml.UsageMode}
	 */
	private static final UsageMode validateUsageMode( final OptionSpec< String > modeSpec, final OptionSet options )
	{
		final UsageMode mode = UsageMode.getMode( modeSpec.value( options ) );
		if( mode == null )
		{
			final String message = "Invalid argument for \"usage-mode\" option: " + modeSpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return mode;
	}
	
	private static final SerializerType validateSerializerType( final OptionSpec< String > serializerSpec, final OptionSet options )
	{
		final SerializerType serializerType = SerializerType.getSerializerType( serializerSpec.value( options ) );
		if( serializerType == null )
		{
			final String message = "Invalid argument for \"serializer\" option: " + serializerSpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return serializerType;
	}
	
	/**
	 * Returns the strategy type
	 * @param strategySpec The option specification for the strategy
	 * @param options The command-line options
	 * @return The {@link org.microtitan.diffusive.diffuser.restful.server.config.StrategyType}
	 */
	private static final StrategyType validateStrategyType( final OptionSpec< String > strategySpec, final OptionSet options )
	{
		final StrategyType strategy = StrategyType.getStrategyType( strategySpec.value( options ) );
		if( strategy == null )
		{
			final String message = "Invalid argument for \"strategy\" option: " + strategySpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return strategy;
	}
	
	private static final List< String > convertUri( final List< URI > uris )
	{
		final List< String > uriStrings = new ArrayList<>();
		for( final URI uri : uris )
		{
			uriStrings.add( uri.toString() );
		}
		return uriStrings;
	}
	
	public static void main( String...args ) throws IOException
	{
		// default settings
		final StrategyType DEFAULT_STRATEGY = StrategyType.RANDOM;
		final URI DEFAULT_SERVER_URI = URI.create( NetworkUtils.createLocalHostServerUri( "http", 8182 ) + RestfulDiffuserManagerResource.DIFFUSER_PATH );

		// set up the command-line arguments
		final OptionParser parser = new OptionParser();
		final OptionSpec< String > logLevelSpec = 
				parser.accepts( "log-level" ).withRequiredArg().ofType( String.class ).defaultsTo( Level.WARN.toString() ).
				describedAs( Level.TRACE + "|" + Level.DEBUG + "|" + Level.INFO + "|" + Level.WARN + "|" + Level.ERROR );
		final OptionSpec< String > modeSpec = 
				parser.accepts( "usage-mode" ).withRequiredArg().ofType( String.class ).defaultsTo( UsageMode.VALIDATE.getName() ).
				describedAs( UsageMode.VALIDATE.getName() + "|" + UsageMode.GENERATE.getName() );
		final OptionSpec< String > configDirSpec = 
				parser.accepts( "config-dir" ).withRequiredArg().ofType( String.class ).defaultsTo( DiffusiveLauncher.XML_CONFIG_DIR );
		final OptionSpec< String > configFileSpec = 
				parser.accepts( "config-file" ).withRequiredArg().ofType( String.class ).defaultsTo( DiffusiveLauncher.XML_CONFIG_FILE_NAME );
		final OptionSpec< URI > classPathsSpec =
				parser.accepts( "class-paths" ).withRequiredArg().ofType( URI.class ).withValuesSeparatedBy( ' ' ).defaultsTo( DEFAULT_SERVER_URI );
		final OptionSpec< String > serializerSpec =
				parser.accepts( "serializer" ).withRequiredArg().ofType( String.class ).defaultsTo( SerializerType.PERSISTENCE_XML.getName() ).
				describedAs( SerializerType.PERSISTENCE_XML.getName() + "|" +
						 	 SerializerType.PERSISTENCE_JSON.getName() + "|" +
						 	 SerializerType.OBJECT.getName() + "|" +
						 	 SerializerType.PERSISTENCE_KEY_VALUE.getName() );
		final OptionSpec< String > strategySpec = 
				parser.accepts( "strategy" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_STRATEGY.getName() ).
				describedAs( StrategyType.RANDOM.getName() + "|" + StrategyType.RANDOM_WEIGHTED.getName() );
		final OptionSpec< String > strategyConfigFileSpec = 
				parser.accepts( "strategy-config-file" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_STRATEGY.getFileName() );
		final OptionSpec< String > strategyConfigClassSpec = 
				parser.accepts( "strategy-config-class" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_STRATEGY.getClassName() );
		final OptionSpec< Long > strategySeedSpec =
				parser.accepts( "strategy-seed" ).withRequiredArg().ofType( Long.class ).defaultsTo( 3141592653l );
		final OptionSpec< Double > thresholdSpec = 
				parser.accepts( "load-threshold" ).withRequiredArg().ofType( Double.class ).defaultsTo( 0.75 ).describedAs( "[0,1]" );
		parser.accepts( "help" );
		
		// parse the command-line arguments
		OptionSet options = null;
		try
		{
			options = parser.parse( args );
		}
		catch( OptionException e )
		{
			e.printStackTrace();
			LOGGER.error( "Error parsing the command-line options.", e );

			System.out.println( "\nPlease see the usage information below: " );
			parser.printHelpOn( System.out );
			System.exit( -1 );
		}

		// if the user requests help, print out help
		if( options.has( "help" ) )
		{
			parser.printHelpOn( System.out );
			System.exit( 0 );
		}

		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.toLevel( logLevelSpec.value( options ) ) );
		
		// grab the values from the command-line options
		final UsageMode usageMode = validateUsageMode( modeSpec, options );
		final String configDir = configDirSpec.value( options );
		final String configFile = configDir + configFileSpec.value( options );
		final List< String > classPaths = convertUri( classPathsSpec.values( options ) );
		final SerializerType serializerType = validateSerializerType( serializerSpec, options );

		final StrategyType strategyType = validateStrategyType( strategySpec, options );
		String strategyConfigFile = configDir + strategyConfigFileSpec.value( options );
		String strategyConfigClassName = strategyConfigClassSpec.value( options );
		if( !options.has( strategyConfigFileSpec ) )
		{
			strategyConfigFile = configDir + strategyType.getFileName();
		}
		if( !options.has( strategyConfigClassSpec ) )
		{
			strategyConfigClassName = strategyType.getClassName();
		}
		
		final double loadThreshold = thresholdSpec.value( options );
		final long randomSeed = strategySeedSpec.value( options );

		// 
		if( usageMode == UsageMode.GENERATE )
		{
			final RestfulDiffuserConfigXml xmlConfig = new RestfulDiffuserConfigXml();
			xmlConfig.setClassPaths( classPaths );
			xmlConfig.setSerializerName( serializerType.getName() );
			xmlConfig.setDiffuserStrategyConfigFile( strategyConfigFile );
			xmlConfig.setDiffuserStrategyConfigClassName( strategyConfigClassName );
			xmlConfig.setLoadThreshold( loadThreshold );

			// write out the diffuser configuration file file
			new XmlPersistence().write( xmlConfig, configFile );
			System.out.println( "Wrote main configuration file: " + configFile );
			System.out.println( xmlConfig.toString() );
			
			// a temporary end-point for the strategy--user must fill this in with a valid end-point
			final String endpoint = "http://[hostname|ip_address]:8182/diffusers";

			// set up the diffuser strategy and write out its configuration file
			DiffuserStrategyConfigXml xmlStrategyConfig = null;
			if( strategyType == StrategyType.RANDOM )
			{
				xmlStrategyConfig = new RandomDiffuserStrategyConfigXml();
				final List< String > endpoints = new ArrayList<>( Arrays.asList( endpoint ) );
				((RandomDiffuserStrategyConfigXml)xmlStrategyConfig).setClientEndpoints( endpoints );
				((RandomDiffuserStrategyConfigXml)xmlStrategyConfig).setRandomSeed( randomSeed );
			}
			else if( strategyType == StrategyType.RANDOM_WEIGHTED )
			{
				xmlStrategyConfig = new RandomWeightedDiffuserStrategyConfigXml();
				final Map< String, Double > endpoints = new LinkedHashMap<>();
				endpoints.put( endpoint, 3.14159 );
				((RandomWeightedDiffuserStrategyConfigXml)xmlStrategyConfig).setClientEndpoints( endpoints );
				((RandomWeightedDiffuserStrategyConfigXml)xmlStrategyConfig).setRandomSeed( randomSeed );
			}
			
			new XmlPersistence().write( xmlStrategyConfig, strategyConfigFile );
			System.out.println( "Wrote strategy configuration file: " + strategyConfigFile );
			System.out.println( xmlStrategyConfig.toString() );
		}
		else if( usageMode == UsageMode.VALIDATE )
		{
			// read the configuration file into the diffuser configuration object and display the results
			final XmlPersistence persist = new XmlPersistence();
			final RestfulDiffuserConfigXml config = persist.read( RestfulDiffuserConfigXml.class, configFile );
			System.out.println( config.toString() );
			
			// read the configuration file into the strategy configuration object and display the results
			final DiffuserStrategyConfigXml strategyConfig = persist.read( config.getDiffuserStrategyConfigClass(), 
																   		   config.getDiffuserStrategyConfigFile() );
			System.out.println( strategyConfig.toString() );
		}
	}
}
