package org.microtitan.diffusive.diffuser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.serializer.ObjectSerializer;
import org.microtitan.diffusive.diffuser.serializer.Serializer;


public class KeyedDiffuserRepository {

	private static final Logger LOGGER = Logger.getLogger( KeyedDiffuserRepository.class );
	
	public static final String DIFFUSER_SET_PROPERTY = "KeyedDiffuserRepository:diffuser_set";
	
	// registries
	private static final Map< Object, KeyedDiffuserRepository > instances;

	// create a shared calc-model registry that can be accessed by anyone
	public static class SharedRegistry {}
	static 
	{
		instances = new HashMap< Object, KeyedDiffuserRepository >();
	}
	
	private Diffuser diffuser;
	
	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * 
	 * @param diffuser
	 * @return
	 */
	public static KeyedDiffuserRepository getInstance()
	{
		return getInstance( SharedRegistry.class );
	}
	
	/**
	 * Returns the named INSTANCE if the key exists, or a new object
	 * with the key name if the key doesn't already exist. 
	 * @param key The key associated, or to be associated, with the registry INSTANCE
	 * @return the named INSTANCE if the key exists, or a new object if the key 
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
				// lazily create INSTANCE
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
			// a key to the same INSTANCE
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

	public static KeyedDiffuserRepository deleteRepository( final Object key )
	{
		return instances.remove( key );
	}
	
	public KeyedDiffuserRepository deleteRepository()
	{
		return instances.remove( getKey() );
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
	
	/*
	 * 
	 */
	private KeyedDiffuserRepository()
	{
		this.diffuser = createDefaultDiffuser();
		this.propertyChangeSupport = new PropertyChangeSupport( this );
	}
	
	private static Diffuser createDefaultDiffuser()
	{
//		final Serializer serializer = new ObjectSerializer();
//		final List< URI > endpoints = Arrays.asList( URI.create( "http://localhost:8182/diffuser" ) );
//		return new RestfulDiffuser( serializer, endpoints );
		return new LocalDiffuser();
	}
	
	public Diffuser getDiffuser()
	{
		return diffuser;
	}
	
	public Diffuser setDiffuser( final Diffuser diffuser )
	{
		final Diffuser oldDiffuser = this.diffuser;
		this.diffuser = diffuser;
		firePropertyChange( DIFFUSER_SET_PROPERTY, oldDiffuser, this.diffuser );
		return oldDiffuser;
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
