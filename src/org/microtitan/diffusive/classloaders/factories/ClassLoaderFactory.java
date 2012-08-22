package org.microtitan.diffusive.classloaders.factories;

import java.net.URI;
import java.util.List;

/**
 * Interface that defines a {@link ClassLoaderFactory} intended for loading classes over the network
 * 
 * @author Robert Philipp
 */
public interface ClassLoaderFactory {
	
	/**
	 * Creates a {@link ClassLoader} for loading classes over the network
	 * @param classPaths The list of {@link URI} specifying the web service end-point for
	 * retrieving serialized {@link Class} objects
	 * @return The {@link ClassLoader} for loading classes over the network
	 */
	ClassLoader create( final List< URI > classPaths );
}
