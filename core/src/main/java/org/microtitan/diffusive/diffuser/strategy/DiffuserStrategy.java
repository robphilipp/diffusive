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
package org.microtitan.diffusive.diffuser.strategy;

import org.microtitan.diffusive.containers.Copyable;

import java.net.URI;
import java.util.List;


/**
 * Interface that defines the strategy for selecting an end-point to which to send the run request.
 * 
 * @author Robert Philipp
 */
public interface DiffuserStrategy extends Copyable< DiffuserStrategy > {

	/**
	 * @return A list of end-point to which to send the next run/execute request. Each subsequent call
	 * should return an end-point based on the implementing classes strategy.
	 */
	List< URI > getEndpoints();
	
	/**
	 * @return The {@link List} of all the end-points that have been registered with this strategy.
	 */
	List< URI > getEndpointList();
	
	/**
	 * Sets the end-point list, overwriting the existing end-point list
	 * @param endpoints The new list of end-points
	 */
	void setEndpointList( final List< URI > endpoints );
	
	/**
	 * Appends the specified end-points to the end of the current end-point list
	 * @param endpoints The end-points to append to the current list of end-points
	 */
	void appendEndpoints( final List< URI > endpoints );
	
	/**
	 * @return true if the strategy has no end points
	 */
	boolean isEmpty();
}
