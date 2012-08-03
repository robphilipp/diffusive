package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;

/**
 * Interface that defines the strategy for selecting an end-point to which to send the run request.
 * 
 * @author Robert Philipp
 */
public interface DiffuserStrategy {

	/**
	 * @return An end-point to which to send the next run/execute request. Each subsequent call
	 * should return an end-point based on the implementing classes strategy.
	 */
	URI getEndpoint();
}
