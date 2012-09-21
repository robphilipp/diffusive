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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache;

/**
 * Load calculator that uses the number of tasks running in the cache divided by the number of
 * processors available on the machine.
 * 
 * @author Robert Philipp
 */
public class TaskCpuLoadCalc implements DiffuserLoadCalc {

	private final ResultsCache cache;
	private final OperatingSystemMXBean mxBean;
	
	public TaskCpuLoadCalc( final ResultsCache cache )
	{
		this.cache = cache;
		this.mxBean = ManagementFactory.getOperatingSystemMXBean();
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoad#getLoad()
	 */
	@Override
	public double getLoad()
	{
		final int numProcessors = mxBean.getAvailableProcessors();
		final long numRunningTasks = cache.getNumRunning();
		return numRunningTasks / (double)numProcessors;
	}

}
