package org.microtitan.diffusive.launcher.config.xml;

import java.util.List;

import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;

public interface DiffuserStrategyConfigXml {

	/**
	 * @return The list of end-points from which the strategy can select
	 */
	List< String > getClientEndpoints();
	
	/**
	 * Sets the list of end-points from which the strategy can select
	 * @param clientEndpoints The list of end-points from which the strategy can select
	 */
	void setClientEndpoints( final List< String > clientEndpoints );

	/**
	 * Creates a {@link RandomDiffuserStrategy} from the valid client end-points held in this object
	 * @return a {@link RandomDiffuserStrategy} set with the valid client end-points specified in this
	 * object and with the random seed specified in this object.
	 */
	DiffuserStrategy createStrategy();
}
