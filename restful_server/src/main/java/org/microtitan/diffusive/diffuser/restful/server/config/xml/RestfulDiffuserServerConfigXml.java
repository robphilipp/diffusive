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

import java.io.IOException;
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
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.server.config.RestfulDiffuserServerConfig;
import org.microtitan.diffusive.diffuser.restful.server.config.ServerMode;
import org.microtitan.diffusive.diffuser.restful.server.config.StrategyType;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategyConfigXml;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategyConfigXml;
import org.microtitan.diffusive.diffuser.strategy.RandomWeightedDiffuserStrategyConfigXml;

/**
 * This is the RESTful server configuration object that is persisted as XML and read from XML to 
 * perform all the configuration of the {@link org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer}. Objects of this class are used by
 * the {@link org.microtitan.diffusive.diffuser.restful.server.config.RestfulDiffuserServerConfig}, which performs the configuration.<p>
 * 
 * The {@code main(...)} method provides a utility for generating and validating the main RESTful 
 * server configuration file and the diffuser strategy configuration file. For information on
 * using the {@code main(...)} method, run this class with the option "{@code --help}".<p>
 * 
 * {@code java RestfulDiffuserServerConfigXml --help}
 * 
 * @author Robert Philipp
 */
public class RestfulDiffuserServerConfigXml {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserServerConfigXml.class );
	
	/**
	 * For diffusers created through calls to the server, this is the load threshold value.
	 * the threshold for CPU loads, above which the diffuser will send the tasks to a remote diffuser,
	 * unless of course, there are no client end-points specified. When the threshold is below the 
	 * load threshold, the diffuser will call the local diffuser to execute the tasks.
	 */
	private double loadThreshold;
	
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
	 * @return the threshold above which load the diffuser is to diffuse any tasks to
	 * a remote diffuser.
	 */
	public double getLoadThreshold()
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
			final StringBuilder message = new StringBuilder();
            message.append( "Error: Failed to load class: [" ).append( strategyConfigClassName )
                    .append( "]" ).append(Constants.NEW_LINE)
                    .append("Class Path: ").append(System.getProperty("java.class.path"));
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
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
		final StringBuffer buffer = new StringBuffer();
		buffer.append( this.getClass().getName() + Constants.NEW_LINE );
		buffer.append( "  Load Threshold: " + loadThreshold + Constants.NEW_LINE );
		buffer.append( "  Strategy Config File: " + strategyConfigFile + Constants.NEW_LINE );
		buffer.append( "  Strategy Config Class: " + strategyConfigClassName + Constants.NEW_LINE );
		return buffer.toString();
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
	 * @param options The parsed command-line options
	 * @return The {@link org.microtitan.diffusive.diffuser.restful.server.config.xml.RestfulDiffuserServerConfigXml.UsageMode}
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
	
	/**
	 * Returns the server mode (for example, is it a remote server, or a class server, etc)
	 * @param serverModeSpec The option specification for the server mode
	 * @param options The command-line options
	 * @return The {@link org.microtitan.diffusive.diffuser.restful.server.config.ServerMode}
	 */
	private static final ServerMode validateServerMode( final OptionSpec< String > serverModeSpec, final OptionSet options )
	{
		final ServerMode mode = ServerMode.getServerType( serverModeSpec.value( options ) );
		if( mode == null )
		{
			final String message = "Invalid argument for \"server-mode\" option: " + serverModeSpec.value( options );
			LOGGER.error( message );
			System.out.println( message );
			System.exit( 0 );
		}
		return mode;
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
	
	/**
	 * Utility that allows generating and validating the main and strategy configuration file
	 * @param args The command-line arguments
	 * @throws java.io.IOException
	 */
	public static void main( String...args ) throws IOException
	{
		// set some default values
		final UsageMode DEFAULT_MODE = UsageMode.VALIDATE;
		final ServerMode DEFAULT_SERVER_MODE = ServerMode.REMOTE;
		final StrategyType DEFAULT_STRATEGY = StrategyType.RANDOM;
		
		// define the command-line options
		final OptionParser parser = new OptionParser();
		final OptionSpec< String > logLevelSpec = 
				parser.accepts( "log-level" ).withRequiredArg().ofType( String.class ).defaultsTo( Level.WARN.toString() ).
				describedAs( Level.TRACE + "|" + Level.DEBUG + "|" + Level.INFO + "|" + Level.WARN + "|" + Level.ERROR );
		final OptionSpec< String > modeSpec = 
				parser.accepts( "usage-mode" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_MODE.getName() ).
				describedAs( UsageMode.VALIDATE.getName() + "|" + UsageMode.GENERATE.getName() );
		final OptionSpec< String > serverModeSpec = 
				parser.accepts( "server-mode" ).withRequiredArg().ofType( String.class ).defaultsTo( DEFAULT_SERVER_MODE.getName() ).
				describedAs( ServerMode.REMOTE.getName() + "|" + ServerMode.CLASS.getName() );
		final OptionSpec< String > configDirSpec = 
				parser.accepts( "config-dir" ).withRequiredArg().ofType( String.class ).defaultsTo( RestfulDiffuserServer.BASE_CONFIGURATION_PATH );
		final OptionSpec< String > configFileSpec = 
				parser.accepts( "config-file" ).withRequiredArg().ofType( String.class ).defaultsTo( RestfulDiffuserServer.DEFAULT_CONFIGURATION_FILE );
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
		
		// set the values
		final UsageMode mode = validateUsageMode( modeSpec, options );
		final ServerMode serverType = validateServerMode( serverModeSpec, options );
		final String directory = configDirSpec.value( options ) + serverType.getName() + "/";
		final String configFile = directory + configFileSpec.value( options );

		final StrategyType strategyType = validateStrategyType( strategySpec, options );
		String strategyConfigFile = directory + strategyConfigFileSpec.value( options );
		String strategyConfigClassName = strategyConfigClassSpec.value( options );
		if( !options.has( strategyConfigFileSpec ) )
		{
			strategyConfigFile = directory + strategyType.getFileName();
		}
		if( !options.has( strategyConfigClassSpec ) )
		{
			strategyConfigClassName = strategyType.getClassName();
		}
		
		final double loadThreshold = thresholdSpec.value( options );
		final long randomSeed = strategySeedSpec.value( options );
		
		// report back on the settings
		final StringBuffer message = new StringBuffer();
		message.append( "  Mode: " + mode.getName() + Constants.NEW_LINE );
		message.append( "  Server Type: " + serverType.getName() + Constants.NEW_LINE );
		message.append( "  Config Directory: " + directory + Constants.NEW_LINE );
		message.append( "  Strategy Type: " + strategyType.getName() + Constants.NEW_LINE );
		message.append( "  Strategy Config File: " + strategyConfigFile + Constants.NEW_LINE );
		message.append( "  Strategy Config Class: " + strategyConfigClassName + Constants.NEW_LINE );
		message.append( "  Strategy Load Threshold: " + loadThreshold + Constants.NEW_LINE );
		LOGGER.info( message.toString() );
		System.out.println( message.toString() );
		
		// 
		if( mode == UsageMode.GENERATE )
		{
			// set upt the configuration object and write it out
			final RestfulDiffuserServerConfigXml xmlConfig = new RestfulDiffuserServerConfigXml();
			xmlConfig.setLaodThreshold( loadThreshold );
			xmlConfig.setDiffuserStrategyConfigClassName( strategyConfigClassName );
			xmlConfig.setDiffuserStrategyConfigFile( strategyConfigFile );

			// write the main configuration file
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
			
			// write the strategy configuration file
			new XmlPersistence().write( xmlStrategyConfig, strategyConfigFile );
			System.out.println( "Wrote strategy configuration file: " + strategyConfigFile );
			System.out.println( xmlStrategyConfig.toString() );
		}
		else if( mode == UsageMode.VALIDATE )
		{
			// read the config file back in to test
			final XmlPersistence persist = new XmlPersistence();
			final RestfulDiffuserServerConfigXml config = persist.read( RestfulDiffuserServerConfigXml.class, configFile );

			System.out.println( config.toString() );
			
			// read the configuration file into the strategy configuration object and display the results
			final DiffuserStrategyConfigXml strategyConfig = persist.read( config.getDiffuserStrategyConfigClass(), 
																   		   config.getDiffuserStrategyConfigFile() );
			System.out.println( strategyConfig.toString() );
		}
	}

}
