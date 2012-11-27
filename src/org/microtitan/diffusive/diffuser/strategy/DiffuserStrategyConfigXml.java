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


/**
 * Interface for the diffuser strategy XML configuration object. This is used by the strategies
 * for persisting and loading diffuser strategies. The configuration object must load its configuration
 * and then use that to create the appropriate strategy, which it must return.
 * 
 * @author Robert Philipp
 */
public interface DiffuserStrategyConfigXml {

	/**
	 * Creates a {@link RandomDiffuserStrategy} from the valid client end-points held in this object
	 * @return a {@link RandomDiffuserStrategy} set with the valid client end-points specified in this
	 * object and with the random seed specified in this object.
	 */
	DiffuserStrategy createStrategy();
}
