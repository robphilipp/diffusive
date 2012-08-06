package org.microtitan.diffusive.diffuser.strategy.load;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * Uses the system's CPU load based on the {@link OperatingSystemMXBean#getProcessCpuLoad()}
 * method. This gives the current load for the system (not just this JVM process).
 * 
 * @author Robert Philipp
 */
public class SystemCpuLoad implements DiffuserLoad {
	
	private static SystemCpuLoad INSTANCE = null;
	
	private final OperatingSystemMXBean mxBean;
	
	private SystemCpuLoad()
	{
		this.mxBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
	}

	/**
	 * @return the one and only instance (per class loader) of the {@link DiffuserLoad}
	 */
	public static SystemCpuLoad getInstance()
	{
		synchronized( INSTANCE )
		{
			if( INSTANCE == null )
			{
				// lazily create the instance
				INSTANCE = new SystemCpuLoad();
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
		return mxBean.getSystemCpuLoad();
	}

}
