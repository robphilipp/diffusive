package org.microtitan.diffusive.diffuser.resolver.config;

import org.apache.log4j.Logger;
import org.freezedry.persistence.XmlPersistence;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.DiffusiveMappingConfiguration;
import org.microtitan.diffusive.diffuser.resolver.EndPointResolver;
import org.microtitan.diffusive.diffuser.resolver.config.xml.FileEndpoingMappingConfigXml;

public class FileEndpoingMappingConfig {

	private static final Logger LOGGER = Logger.getLogger( FileEndpoingMappingConfig.class );

	@DiffusiveMappingConfiguration
	public static final EndPointResolver configure( final String mappingFilename )
	{
		return null;
	}

	/**
	 * Loads the configuration object from the XML file and returns it. If the configuration file
	 * couldn't be read properly, then returns null.
	 * @param filename The name of the configuration file to read
	 * @return The configuration object or null if the configuration file can't be read properly
	 */
	private static final FileEndpoingMappingConfigXml loadConfig( final String filename )
	{
		FileEndpoingMappingConfigXml config = null;
		try
		{
			config = new XmlPersistence().read( FileEndpoingMappingConfigXml.class, filename );
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
	
}
