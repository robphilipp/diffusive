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
package org.microtitan.diffusive.diffuser.restful.resources.cache;

import org.microtitan.diffusive.cache.Cache;

/**
 * Cache for execution results. The keys are {@link String}s and the entry are {@link org.microtitan.diffusive.diffuser.restful.resources.cache.ResultCacheEntry}s
 * with an {@link Object} parameter type.
 * 
 * @author Robert Philipp
 */
public interface ResultsCache extends Cache< String, ResultCacheEntry< Object > > {

	/**
	 * Returns true if the task is still running; false otherwise
	 * @param signature The signature of the task
	 * @param requestId The ID associated with the request
	 * @return true if the task is still running; false otherwise
	 */
	boolean isRunning(final String key);
	
	/**
	 * @return The number of tasks in this cache that are currently running.
	 */
	long getNumRunning();
}
