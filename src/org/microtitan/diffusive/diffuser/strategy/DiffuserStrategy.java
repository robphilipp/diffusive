package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.List;

/**
 * Interface that defines the strategy for selecting an end-point to which to send the run request.
 * 
 * @author Robert Philipp
 */
public interface DiffuserStrategy {

	/**
	 * @return A list of end-point to which to send the next run/execute request. Each subsequent call
	 * should return an end-point based on the implementing classes strategy.
	 */
	List< URI > getEndpoints();
	
	/**
	 * @return true if the strategy has no end points
	 */
	boolean isEmpty();
}
