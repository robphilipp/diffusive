package org.microtitan.diffusive.diffuser.resolver;

import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;

/**
 * Resolves non-physical end-points to physical end-points based on the mapping specification.
 * The mapping mechanism must be defined in the implementing classes. A mapping specification
 * could be a simple map containing the non-physical end-point as the key and the physical
 * end-point as the value. Or it could me a more complicated specification containing rules
 * or ranges and the like that allows the non-physical end-points to be mapped to physical ones.
 * 
 * @author Robert Philipp
 */
public interface EndPointResolver {

	/**
	 * Updates the specified {@link DiffuserStrategy} by replacing the existing end-points
	 * with end-points specified in the mapping.
	 * @param strategy The {@link DiffuserStrategy} to resolve
	 */
	void resolve( final DiffuserStrategy strategy );
}
