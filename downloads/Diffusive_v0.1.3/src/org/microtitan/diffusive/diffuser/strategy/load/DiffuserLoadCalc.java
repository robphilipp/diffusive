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
package org.microtitan.diffusive.diffuser.strategy.load;

/**
 * Represents the CPU load in a similar way to Unix load:
 * <ul>
 * 	<li>0.0 means no load</li>
 * 	<li>1.0 means work is perfectly balanced with CPU resources (this isn't necessarily
 * 		the threshold value which the load shouldn't surpass, that may by 0.7 or so.)</li>
 * 	<li>Greater than 1.0 means the load is too high.</li>
 * </ul>
 * 
 * @author Robert Philipp
 */
public interface DiffuserLoadCalc {

	/**
	 * @return the CPU load. The method of calculating the load is determined by the implementing
	 * class. And the interpretation of the load should be consistent with that implementation.
	 * For example, is it a system or process load. Or is it an time-average load over some interval.
	 * Or is it a spot value.
	 */
	double getLoad();
}
