/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.microtitan.diffusive.utils;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ReflectionUtils {
	
	private static final Logger LOGGER = Logger.getLogger( ReflectionUtils.class );
	
	private static Map< String, Class< ? > > TYPE_MAP = new HashMap<>();
	static {
		TYPE_MAP.put( void.class.getName(), void.class );
		TYPE_MAP.put( int.class.getName(), int.class );
		TYPE_MAP.put( short.class.getName(), short.class );
		TYPE_MAP.put( long.class.getName(), long.class );
		TYPE_MAP.put( double.class.getName(), double.class );
		TYPE_MAP.put( float.class.getName(), float.class );
		TYPE_MAP.put( byte.class.getName(), byte.class );
		TYPE_MAP.put( char.class.getName(), char.class );
		TYPE_MAP.put( boolean.class.getName(), boolean.class );
	}
	
	private static Map< Class< ? >, Class< ? > > WRAP_MAP = new HashMap<>();
	static {
		WRAP_MAP.put( void.class, Void.class );
		WRAP_MAP.put( int.class, Integer.class );
		WRAP_MAP.put( short.class, Short.class );
		WRAP_MAP.put( long.class, Long.class );
		WRAP_MAP.put( double.class, Double.class );
		WRAP_MAP.put( float.class, Float.class );
		WRAP_MAP.put( byte.class, Byte.class );
		WRAP_MAP.put( char.class, Character.class );
		WRAP_MAP.put( boolean.class, Boolean.class );
	}
	
	private static Map< String, Class< ? > > ENCODED_TYPE_MAP = new HashMap<>();
	static {
		ENCODED_TYPE_MAP.put( "I", int.class );
		ENCODED_TYPE_MAP.put( "S", short.class );
		ENCODED_TYPE_MAP.put( "J", long.class );
		ENCODED_TYPE_MAP.put( "D", double.class );
		ENCODED_TYPE_MAP.put( "F", float.class );
		ENCODED_TYPE_MAP.put( "B", byte.class );
		ENCODED_TYPE_MAP.put( "C", char.class );
		ENCODED_TYPE_MAP.put( "Z", boolean.class );
	}

	/**
	 * Utility method that returns the {@link Class} object for the specified class name.
	 * @param className The name of the {@link Class} for which to return the {@link Class} object.
	 * @return the {@link Class} object for the specified class name.
	 * @throws IllegalArgumentException
	 */
	public static Class< ? > getClazz( final String className )
	{
		// check first if the class is a primitive
		Class< ? > clazz = TYPE_MAP.get( className );
		
		try
		{
			if( clazz == null )
			{
				final int arrayDimension = className.lastIndexOf( "[" )+1;
				
				// if we have an array get the array class
				if( arrayDimension > 0 )
				{
					// grab the type of the array either a primitive or an object at this point
					String arrayType = null;
					if( className.substring( arrayDimension, arrayDimension+1 ).equals( "L" ) )
					{
						arrayType = className.substring( arrayDimension+1 );
					}
					else
					{
						arrayType = className.substring( arrayDimension );
					}
					Class< ? > arrayClazz = ENCODED_TYPE_MAP.get( arrayType );
					if( arrayClazz == null )
					{
						arrayClazz = Class.forName( arrayType );
					}
	
					final int[] dims = new int[ arrayDimension ];
					for( int i = 0; i < arrayDimension; i++ )
					{
						dims[ i ] = 1;
					}
					clazz = Array.newInstance( arrayClazz, dims ).getClass();
				}
			}
			
			// not a primitive, so do the normal reflective thing
			if( clazz == null )
			{
				clazz = Class.forName( className );
			}
		}
		catch( ClassNotFoundException e )
		{
			final String message = "Could not instantiate class from specified class name: " + className;
			LOGGER.error( message, e );
			throw new IllegalArgumentException( message, e );
		}
		return clazz;
	}
	
	/**
	 * If the specified {@link Class} is a primitive, then this function will return the wrapped
	 * version. For example, if the specified {@link Class} is {@code int.class} or {@code Integer.TYPE}
	 * this method will return {@code Integer.class}
	 * @param clazz The {@link Class} to wrap
	 * @return The specified {@link Class} or the wrapped version of the {@link Class} if it is a primitive
	 */
	public static Class< ? > wrapPrimitive( final Class< ? > clazz )
	{
		if( WRAP_MAP.containsKey( clazz ) )
		{
			return WRAP_MAP.get( clazz ); 
		}
		else
		{
			return clazz;
		}
	}
	
	public static void main( String...args )
	{
		System.out.println( double.class.getName() );
		System.out.println( Double.TYPE.getName() );
		System.out.println( getClazz( "double" ) );
	}
}
