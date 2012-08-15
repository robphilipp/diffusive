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
