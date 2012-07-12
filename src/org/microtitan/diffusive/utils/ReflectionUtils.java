package org.microtitan.diffusive.utils;

import org.apache.log4j.Logger;

public class ReflectionUtils {
	
	private static final Logger LOGGER = Logger.getLogger( ReflectionUtils.class );

	/**
	 * Utility method that returns the {@link Class} object for the specified class name.
	 * @param className The name of the {@link Class} for which to return the {@link Class} object.
	 * @return the {@link Class} object for the specified class name.
	 */
	public static Class< ? > getClazz( final String className )
	{
		// check first if the class is a primitive
		if( void.class.getName().equals( className ) )
		{
			return void.class;
		}
		if( int.class.getName().equals( className ) )
		{
			return int.class;
		}
		if( short.class.getName().equals( className ) )
		{
			return short.class;
		}
		if( long.class.getName().equals( className ) )
		{
			return long.class;
		}
		if( float.class.getName().equals( className ) )
		{
			return float.class;
		}
		if( double.class.getName().equals( className ) )
		{
			return double.class;
		}
		if( byte.class.getName().equals( className ) )
		{
			return byte.class;
		}
		if( char.class.getName().equals( className ) )
		{
			return char.class;
		}
		if( boolean.class.getName().equals( className ) )
		{
			return boolean.class;
		}
		
		// not a primitive, so do the normal reflective thing
		Class< ? > clazz = null;
		try
		{
			clazz = Class.forName( className );
		}
		catch( ClassNotFoundException e )
		{
			final String message = "Could not instantiate class from specified class name: " + className;
			LOGGER.error( message, e );
			throw new IllegalArgumentException( message, e );
		}
		return clazz;
	}
}
