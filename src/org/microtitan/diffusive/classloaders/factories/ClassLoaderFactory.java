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
	 * Creates a {@link ClassLoader} for loading classes over the network. Should default the parent
	 * class loader to the loader that loads this class.
	 * @param baseSignature The signature of the base method that shouldn't be diffused further
	 * @param classPaths The list of {@link URI} specifying the web service end-point for
	 * retrieving serialized {@link Class} objects
	 * @return The {@link ClassLoader} for loading classes over the network
	 */
	ClassLoader create( final String baseSignature, final List< URI > classPaths );

	/**
	 * Creates a {@link ClassLoader} for loading classes over the network
	 * @param parentLoader The parent class loader
	 * @param baseSignature The signature of the base method that shouldn't be diffused further
	 * @param classPaths The list of {@link URI} specifying the web service end-point for
	 * retrieving serialized {@link Class} objects
	 * @return The {@link ClassLoader} for loading classes over the network
	 */
	ClassLoader create( final ClassLoader parentLoader, final String baseSignature, final List< URI > classPaths );
}
