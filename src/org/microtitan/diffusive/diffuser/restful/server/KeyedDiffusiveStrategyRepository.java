package org.microtitan.diffusive.diffuser.restful.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;

/**
 * Keyed repository hold information about how to diffuser tasks (strategy and load threshold)
 * The {@link SharedRegistry} is the shared registry, but others can be created with different
 * keys as needed.
 * 
 * @author Robert Philipp
 */
public class KeyedDiffusiveStrategyRepository {

	private static final Logger LOGGER = Logger.getLogger( KeyedDiffusiveStrategyRepository.class );

	public static final String STRATEGY_SET_PROPERTY = "KeyedDiffusiveStrategyRepository:strategy_set";
	public static final String LOAD_THRESHOLD_SET = "KeyedDiffusiveStrategyRepository:load_threshold_set";
	
	private static final DiffuserStrategy DEFAULT_STRATEGY = new RandomDiffuserStrategy();
	private static final double DEFAULT_LOAD_THRESHOLD = 0.7;

	// registries
	private static final Map< Object, KeyedDiffusiveStrategyRepository > instances;

	// create a shared diffuser strategy registry that can be accessed by anyone
	public static class SharedRegistry {
	}

	// create the map that holds the key-repository pairs
	static
	{
		instances = new HashMap< Object, KeyedDiffusiveStrategyRepository >();
	}

	private DiffuserStrategy strategy;
	private double loadThreshold;

	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * @return the shared instance of the diffusive strategy repository. Sets the {@link DiffuserStrategy}
	 * to the {@link #DEFAULT_STRATEGY} ({@link RandomDiffuserStrategy}), and the load threshold to
	 * the {@link #DEFAULT_LOAD_THRESHOLD} = {@value #DEFAULT_LOAD_THRESHOLD}.
	 * @see #setLoadThreshold(double)
	 * @see #setStrategy(DiffuserStrategy)
	 * @see #setValues(DiffuserStrategy, double)
	 */
	public static KeyedDiffusiveStrategyRepository getInstance()
	{
		return getInstance( SharedRegistry.class );
	}

	/**
	 * Returns the named instance if the key exists, or a new object with the key name if the key doesn't
	 * already exist. Sets the {@link DiffuserStrategy} to the {@link #DEFAULT_STRATEGY} 
	 * ({@link RandomDiffuserStrategy}), and the load threshold to the {@link #DEFAULT_LOAD_THRESHOLD} 
	 * = {@value #DEFAULT_LOAD_THRESHOLD}.
	 * @param key The key associated, or to be associated, with the registry instance
	 * @return the named instance if the key exists, or a new object if the key doesn't exist
	 * @see #setLoadThreshold(double)
	 * @see #setStrategy(DiffuserStrategy)
	 * @see #setValues(DiffuserStrategy, double)
	 */
	public static KeyedDiffusiveStrategyRepository getInstance( final Object key )
	{
		synchronized( instances )
		{
			// "per key" singleton
			KeyedDiffusiveStrategyRepository instance = instances.get( key );

			if( instance == null )
			{
				// lazily create instance
				instance = new KeyedDiffusiveStrategyRepository();

				// add it to map
				instances.put( key, instance );
			}

			return instance;
		}
	}

	/**
	 * Associates the specified new key to the registry that is currently associated with the specified key. In other
	 * words, associates the specified new key to the same registry as the key (if the key exists). As as side effect,
	 * any previous associations the new key may have are over written.
	 * 
	 * @param key The key that holds the current association to the registry
	 * @param newKey The object to associate to the same registry as the key.
	 * @return the keyed registry associated with specified new key if it is being replaced; or null if it is
	 *         not being replaced
	 */
	public static KeyedDiffusiveStrategyRepository associate( Object key, Object newKey )
	{
		KeyedDiffusiveStrategyRepository instance = null;
		synchronized( instances )
		{
			// "per key" singleton
			instance = instances.get( key );

			// if key exists, then add the new object to the map as a
			// a key to the same instance
			if( instance != null )
			{
				// add the new key to the map of instances
				instance = instances.put( newKey, instance );

				// log the action
				if( LOGGER.isDebugEnabled() )
				{
					String keyString = key.getClass().getSimpleName() + " (class name)";
					if( key instanceof String )
					{
						keyString = (String)key;
					}
					String newKeyString = newKey.getClass().getSimpleName() + " (class name)";
					if( newKey instanceof String )
					{
						newKeyString = (String)newKey;
					}

					StringBuffer message = new StringBuffer();
					message.append( "Created a new association between keys:" ).append( Constants.NEW_LINE );
					message.append( "  Existing key: " ).append( keyString ).append( Constants.NEW_LINE );
					message.append( "  New key: " ).append( newKeyString );

					LOGGER.debug( message.toString() );
				}
			}
			else
			{
				// the key didn't exist, and so, the new key couldn't be
				// associated to it
				// issue error message and throw an exception
				String keyString = key.getClass().getSimpleName() + " (class name)";
				if( key instanceof String )
				{
					keyString = (String)key;
				}
				String newKeyString = newKey.getClass().getSimpleName() + " (class name)";
				if( newKey instanceof String )
				{
					newKeyString = (String)newKey;
				}

				StringBuffer message = new StringBuffer();
				message.append( "Unable to create association between keys. Creating an association requires" );
				message.append( Constants.NEW_LINE );
				message.append( "that the \"existing key\" is a key in the registry, which it isn't." );
				message.append( Constants.NEW_LINE );
				message.append( "  Existing key: " ).append( keyString ).append( Constants.NEW_LINE );
				message.append( "  New key: " ).append( newKeyString );

				LOGGER.error( message.toString() );

				throw new IllegalArgumentException( message.toString() );
			}
		}
		return instance;
	}

	/**
	 * Associates the specified new key to this registry (if the key exists). As as side effect, any previous
	 * associations the new key may have are over written.
	 * 
	 * @param newKey The object to associate to the same registry as the key.
	 * @return the keyed registry associated with specified new key if it is being replaced; or null if it is
	 *         not being replaced
	 */
	public KeyedDiffusiveStrategyRepository associate( final Object newKey )
	{
		synchronized( instances )
		{
			return associate( getKey(), newKey );
		}
	}

	/**
	 * Deletes the repository associated with the specified key
	 * @param key The key associated with the repository to be deleted
	 * @return The repository that was deleted; or null if no such key exists.
	 */
	public static KeyedDiffusiveStrategyRepository deleteRepository( final Object key )
	{
		synchronized( instances )
		{
			return instances.remove( key );
		}
	}

	/**
	 * Deletes this repository instance from the available repositories
	 * @return This repository instance from the available repositories
	 */
	public KeyedDiffusiveStrategyRepository deleteRepository()
	{
		synchronized( instances )
		{
			return instances.remove( getKey() );
		}
	}

	/**
	 * Returns the key associated with this object; null if this isn't found (should never happen)
	 * @return the key associated with this object
	 */
	public Object getKey()
	{
		synchronized( instances )
		{
			for( Map.Entry< Object, KeyedDiffusiveStrategyRepository > entry : instances.entrySet() )
			{
				if( entry.getValue() == this )
				{
					return entry.getKey();
				}
			}
			return null;
		}
	}

	/**
	 * Constructor that sets the default {@link DiffuserStrategy} and load threshold
	 */
	private KeyedDiffusiveStrategyRepository()
	{
		this.strategy = DEFAULT_STRATEGY;
		this.loadThreshold = DEFAULT_LOAD_THRESHOLD;
		this.propertyChangeSupport = new PropertyChangeSupport( this );
	}

	/**
	 * @return the {@link DiffuserStrategy} set
	 */
	public DiffuserStrategy getStrategy()
	{
		return strategy;
	}

	/**
	 * Sets the {@link DiffuserStrategy} and returns the previous value (if there is one)
	 * @param strategy The new {@link DiffuserStrategy}
	 * @return The previous {@link DiffuserStrategy} assigned to this registry
	 */
	public DiffuserStrategy setStrategy( final DiffuserStrategy strategy )
	{
		final DiffuserStrategy oldStrategy = this.strategy;
		this.strategy = strategy;
		firePropertyChange( STRATEGY_SET_PROPERTY, oldStrategy, this.strategy );
		return oldStrategy;
	}
	
	/**
	 * Sets the load threshold above which the diffuser should diffuse the task to a remote diffuser
	 * @param loadThreshold The load threshold in the interval (0.0, infinity]
	 * @return the previous value of the load threshold
	 */
	public double setLoadThreshold( final double loadThreshold )
	{
		final double oldThreshold = this.loadThreshold;
		this.loadThreshold = loadThreshold;
		firePropertyChange( LOAD_THRESHOLD_SET, oldThreshold, this.loadThreshold );
		return oldThreshold;
	}
	
	/**
	 * @return The CPU load threshold for determining whether to execute locally or diffuse the
	 * execution to a remote diffuser.
	 */
	public double getLoadThreshold()
	{
		return loadThreshold;
	}
	
	/**
	 * Convenience method that allows setting the strategy and the load threshold at once 
	 * @param strategy The new {@link DiffuserStrategy}
	 * @param loadThreshold The load threshold in the interval (0.0, infinity]
	 * @see #setStrategy(DiffuserStrategy)
	 * @see #setLoadThreshold(double)
	 */
	public void setValues( final DiffuserStrategy strategy, final double loadThreshold )
	{
		setStrategy( strategy );
		setLoadThreshold( loadThreshold );
	}

	/**
	 * Allows registering a {@link PropertyChangeListener} to receive property change events
	 * when the strategy or load threshold are changed.
	 * @param listener The {@link PropertyChangeListener} to register
	 */
	public void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	/**
	 * Removes the specified {@link PropertyChangeListener} from receiving change events.
	 * @param listener The {@link PropertyChangeListener} to remove
	 */
	public void removePropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	/**
	 * Fires a {@link PropertyChangeEvent} for the specified change
	 * @param propertyName The name of the property change
	 * @param oldValue The value of the property before the change
	 * @param newValue The value of the property after the change
	 */
	private void firePropertyChange( final String propertyName, final Object oldValue, final Object newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}
}
