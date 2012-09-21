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
 * TODO fix this class to give the system CPU load. currently gives the average system load instead
 * Uses the system's CPU load based on the {@link OperatingSystemMXBean#getProcessCpuLoad()}
 * method. This gives the current load for the system (not just this JVM process).
 * 
 * @author Robert Philipp
 */
public class SystemCpuLoadCalc implements DiffuserLoadCalc {
	
	private static SystemCpuLoadCalc INSTANCE = null;
	
	private final OperatingSystemMXBean mxBean;
	
	private SystemCpuLoadCalc()
	{
		this.mxBean = ManagementFactory.getOperatingSystemMXBean();
	}

	/**
	 * @return the one and only instance (per class loader) of the {@link DiffuserLoadCalc}
	 */
	public static SystemCpuLoadCalc getInstance()
	{
		synchronized( INSTANCE )
		{
			if( INSTANCE == null )
			{
				// lazily create the instance
				INSTANCE = new SystemCpuLoadCalc();
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
		// TODO fix this to get the system CPU load instead...haven't figured this out yet
		return mxBean.getSystemLoadAverage();//getSystemCpuLoad();
	}

}
