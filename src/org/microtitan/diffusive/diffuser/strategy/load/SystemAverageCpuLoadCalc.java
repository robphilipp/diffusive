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

/**
 * Uses the system's average CPU load based on the {@link OperatingSystemMXBean#getSystemLoadAverage()}
 * method. This gives the average load over the past minute. In cases where the tasks are long running (say
 * about greater than a minute) then this method may yield reasonable results for load-balancing 
 * strategies. However, in cases where there are many short running tasks, the average won't pick them up
 * and the strategy may not yield optimal load balancing.
 * 
 * @author Robert Philipp
 */
public class SystemAverageCpuLoadCalc implements DiffuserLoadCalc {
	
	private static SystemAverageCpuLoadCalc INSTANCE = null;
	
	private final OperatingSystemMXBean mxBean;
	
	private SystemAverageCpuLoadCalc()
	{
		this.mxBean = ManagementFactory.getOperatingSystemMXBean();
	}

	/**
	 * @return the one and only instance (per class loader) of the {@link DiffuserLoadCalc}
	 */
	public static SystemAverageCpuLoadCalc getInstance()
	{
		synchronized( INSTANCE )
		{
			if( INSTANCE == null )
			{
				// lazily create the instance
				INSTANCE = new SystemAverageCpuLoadCalc();
			}

			return INSTANCE;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserLoad#getLoad()
	 */
	@Override
	public double getLoad()
	{
		return mxBean.getSystemLoadAverage();
	}

}
