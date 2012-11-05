package org.microtitan.diffusive.launcher.config.xml;

import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;

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
