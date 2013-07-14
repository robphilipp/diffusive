package org.microtitan.diffusive.diffuser.resolver.config.xml;

import org.microtitan.diffusive.diffuser.resolver.EndPointResolver;

public interface EndpointMappingConfigXml {

	/**
	 * Creates the returns the end-point mapping resolver based on the configuration specified
	 * in the xml file
	 * @return an {@link EndPointResolver} based on the configuration (mapping) specified in 
	 * the xml file 
	 */
	EndPointResolver createResolver();
}
