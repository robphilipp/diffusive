package org.microtitan.diffusive.diffuser.restful.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategy;

public class KeyedDiffusiveStrategyRepository {

	private static final Logger LOGGER = Logger.getLogger( KeyedDiffusiveStrategyRepository.class );

	public static final String STRATEGY_SET_PROPERTY = "KeyedDiffusiveStrategyRepository:strategy_set";

	// registries
	private static final Map< Object, KeyedDiffusiveStrategyRepository > instances;

	// create a shared diffuser strategy registry that can be accessed by anyone
	public static class SharedRegistry {
	}

	static
	{
		instances = new HashMap< Object, KeyedDiffusiveStrategyRepository >();
	}

	private DiffuserStrategy strategy;

	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * @return the shared instance of the diffusive strategy repository
	 */
	public static KeyedDiffusiveStrategyRepository getInstance()
	{
		return getInstance( SharedRegistry.class );
	}

	/**
	 * Returns the named instance if the key exists, or a new object with the key name if the key doesn't already exist.
	 * 
	 * @param key The key associated, or to be associated, with the registry instance
	 * @return the named instance if the key exists, or a new object if the key doesn't exist
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
	 * @return the keyed calc model registry associated with specified new key if it is being replaced; or null if it is
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
	 * @return the keyed calc model registry associated with specified new key if it is being replaced; or null if it is
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

	/*
	 * 
	 */
	private KeyedDiffusiveStrategyRepository()
	{
		this.strategy = createDefaultStrategy();
		this.propertyChangeSupport = new PropertyChangeSupport( this );
	}

	private static DiffuserStrategy createDefaultStrategy()
	{
		return new RandomDiffuserStrategy();
	}

	public DiffuserStrategy getStrategy()
	{
		return strategy;
	}

	public DiffuserStrategy setStrategy( final DiffuserStrategy strategy )
	{
		final DiffuserStrategy oldStrategy = this.strategy;
		this.strategy = strategy;
		firePropertyChange( STRATEGY_SET_PROPERTY, oldStrategy, this.strategy );
		return oldStrategy;
	}

	public void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}

	public void removePropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}

	private void firePropertyChange( final String propertyName, final Object oldValue, final Object newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}
}
