package org.microtitan.diffusive.diffuser.strategy.load;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.microtitan.diffusive.diffuser.restful.resources.cache.ResultsCache;

/**
 * Load calculator
 * @author Robert Philipp
 *
 */
public class TaskCpuLoad implements DiffuserLoad {

	private final ResultsCache cache;
	private final OperatingSystemMXBean mxBean;
	
	public TaskCpuLoad( final ResultsCache cache )
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
