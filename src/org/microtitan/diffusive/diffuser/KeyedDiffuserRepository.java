package org.microtitan.diffusive.diffuser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.Marshaller.Listener;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

/**
 * Repository that holds diffusers. The global repository uses the {@link SharedRegistry}'s {@link Class}
 * as a key and can be accessed via {@link #getInstance()}. Local or private repositories can be created
 * and accessed through the {@link #getInstance(Object)} with a key.
 * 
 * Each repository holds a set of diffusers and their associated keys. The default diffuser can be accessed
 * through {@link #getDiffuser()} method and set through the {@link #setDiffuser(Diffuser)} method. Other
 * diffusers may be added and accessed through the {@link #putDiffuser(String, Diffuser)} and 
 * {@link #getDiffuser(String)} methods where the first argument is the key to that diffuser.
 * 
 * @author Robert Philipp
 */
public class KeyedDiffuserRepository {

	private static final Logger LOGGER = Logger.getLogger( KeyedDiffuserRepository.class );
	
	public static final String DIFFUSER_SET_PROPERTY = "KeyedDiffuserRepository:diffuser_set";
	public static final String DIFFUSER_DELETE_PROPERTY = "KeyedDiffuserRepository:diffuser_deleted";
	
	private static final String DEFAULT_DIFFUSER = "default_diffuser";
	
	// registries
	private static final Map< Object, KeyedDiffuserRepository > instances;

	// create a shared diffuser registry that can be accessed by anyone
	public static class SharedRegistry {}
	static 
	{
		instances = new HashMap< Object, KeyedDiffuserRepository >();
	}
	
	// the diffusers and their associated keys
	private final Map< String, Diffuser > diffusers;
	
	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * @return the instance of the global diffuser repository
	 */
	public static KeyedDiffuserRepository getInstance()
	{
		return getInstance( SharedRegistry.class );
	}
	
	/**
	 * Returns the named instance if the key exists, or a new object
	 * with the key name if the key doesn't already exist. 
	 * @param key The key associated, or to be associated, with the registry instance
	 * @return the named instance if the key exists, or a new object if the key 
	 * doesn't exist
	 */
	public static KeyedDiffuserRepository getInstance( final Object key )
	{
		synchronized( instances )
		{
			// "per key" singleton
			KeyedDiffuserRepository instance = instances.get( key );

			if( instance == null )
			{
				// lazily create instance
				instance = new KeyedDiffuserRepository();

				// add it to map
				instances.put( key, instance );
			}

			return instance;
		}
	}
	
	/**
	 * Associates the specified new key to the registry that is
	 * currently associated with the specified key. In other words, associates
	 * the specified new key to the same registry as the key (if the key exists). As
	 * as side effect, any previous associations the new key may have are over written.
	 * @param key The key that holds the current association to the registry
	 * @param newKey The object to associate to the same registry as the key.
	 * @return the keyed calc model registry associated with specified new key
	 * if it is being replaced; or null if it is not being replaced
	 */
	public static KeyedDiffuserRepository associate( Object key, Object newKey )
	{
		KeyedDiffuserRepository instance = null;
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
				// the key didn't exist, and so, the new key couldn't be associated to it
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
	 * Associates the specified new key to this registry (if the key exists). As
	 * as side effect, any previous associations the new key may have are over written.
	 * @param newKey The object to associate to the same registry as the key.
	 * @return the keyed calc model registry associated with specified new key
	 * if it is being replaced; or null if it is not being replaced
	 */
	public KeyedDiffuserRepository associate( Object newKey )
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
	public static KeyedDiffuserRepository deleteRepository( final Object key )
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
	public KeyedDiffuserRepository deleteRepository()
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
			for( Map.Entry< Object, KeyedDiffuserRepository > entry : instances.entrySet() )
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
	 * Private constructor to keep this a singleton (per class loader)
	 */
	private KeyedDiffuserRepository()
	{
		// create the map that holds the diffusers and their associated signature
		this.diffusers = new HashMap<>();
		this.diffusers.put( DEFAULT_DIFFUSER, createDefaultDiffuser() );
		this.propertyChangeSupport = new PropertyChangeSupport( this );
	}
	
	/**
	 * @return creates and returns a default {@link Diffuser}
	 */
	private static Diffuser createDefaultDiffuser()
	{
		return new LocalDiffuser();
	}
	
	/**
	 * @return the default {@link Diffuser} for the repository
	 */
	public final synchronized Diffuser getDiffuser()
	{
		return getDiffuser( DEFAULT_DIFFUSER );
	}
	
	/**
	 * Returns the {@link Diffuser} with the specified key
	 * @param key The key associated with the {@link Diffuser}
	 * @return the {@link Diffuser} with the specified key
	 */
	public final synchronized Diffuser getDiffuser( final String key )
	{
		return diffusers.get( key );
	}
	
	/**
	 * Sets the default {@link Diffuser} to the specified {@link Diffuser}
	 * @param diffuser The {@link Diffuser} that serves as the default {@link Diffuser}
	 * @return any previously set default {@link Diffuser}
	 */
	public final synchronized Diffuser setDiffuser( final Diffuser diffuser )
	{
		return putDiffuser( DEFAULT_DIFFUSER, diffuser );
	}
	
	/**
	 * Adds a {@link Diffuser} to the repository that is associated with the specified key and
	 * returns any {@link Diffuser} that might have been associated with the key prior to this call.
	 * @param key The key associated with the specified {@link Diffuser}
	 * @param diffuser The {@link Diffuser} to add to the repository
	 * @return any {@link Diffuser} that might have been associated with the key prior to this call
	 */
	public final synchronized Diffuser putDiffuser( final String key, final Diffuser diffuser )
	{
		final Diffuser oldDiffuser = diffusers.put( key, diffuser );
		firePropertyChange( DIFFUSER_SET_PROPERTY, oldDiffuser, diffuser );
		return oldDiffuser;
	}
	
	/**
	 * Removes the {@link Diffuser} associated with the specified key and returns it
	 * @param key The key associated with the {@link Diffuser} to remove
	 * @return the removed {@link Diffuser}; null the key was not found
	 */
	public final synchronized Diffuser removeDiffuser( final String key )
	{
		final Diffuser diffuser = diffusers.remove( key );
		firePropertyChange( DIFFUSER_DELETE_PROPERTY, diffuser, diffuser );
		return diffuser;
	}
	
	/**
	 * Adds a listener to receive property change events (adding, removing, setting diffusers)
	 * @param listener The {@link Listener}
	 */
	public void addPropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.addPropertyChangeListener( listener );
	}
	
	/**
	 * Removes a listener from receiving property change events (adding, removing, setting diffusers)
	 * @param listener The {@link Listener}
	 */
	public void removePropertyChangeListener( final PropertyChangeListener listener )
	{
		propertyChangeSupport.removePropertyChangeListener( listener );
	}
	
	/**
	 * Fires a property change event with the specified property name, old and new value.
	 * @param propertyName The name of the property that changed
	 * @param oldValue The old value of the property
	 * @param newValue The new value of the property
	 */
	private void firePropertyChange( final String propertyName, final Object oldValue, final Object newValue )
	{
		propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
	}
}
