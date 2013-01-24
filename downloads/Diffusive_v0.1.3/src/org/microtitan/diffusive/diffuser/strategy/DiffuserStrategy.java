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
