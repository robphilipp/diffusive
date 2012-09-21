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
package org.microtitan.diffusive.diffuser.serializer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

public class SerializerFactory {

	private static final Logger LOGGER = Logger.getLogger( SerializerFactory.class );
	
	/**
	 * SerializerFactoryHolder is loaded on the first execution of
	 * SerializerFactory.getInstance() or the first access to SerializerFactoryHolder.INSTANCE,
	 * not before.
	 */
	private static class SerializerFactoryHolder {
		
		public static final SerializerFactory INSTANCE = new SerializerFactory();
	}

	/**
	 * @return The singleton instance of the {@link SerializerFactory}
	 */
	public static SerializerFactory getInstance()
	{
		return SerializerFactoryHolder.INSTANCE;
	}

	private Map< String, Class< ? extends Serializer > > serializerClasses;
	
	/*
	 * Private constructor prevents instantiation from other classes
	 */
	private SerializerFactory() 
	{
		serializerClasses = createDefaultSerializerClassMap();
	}
	
	/*
	 * @return Creates and returns the default mapping between the serializer type and its class.
	 */
	private static Map< String, Class< ? extends Serializer > > createDefaultSerializerClassMap()
	{
		final Map< String, Class< ? extends Serializer > > serializerClasses = new LinkedHashMap<>();
		
		for( SerializerType type : SerializerType.values() )
		{
			serializerClasses.put( type.getName(), type.getSerialzierClass() );
		}
		return serializerClasses;
	}
	
	public synchronized static final Class< ? extends Serializer > getSerializerClass( final String name )
	{
		return getInstance().serializerClasses.get( name );
	}
	
	public synchronized static final String getSerializerName( final Class< ? extends Serializer > clazz )
	{
		String name = null;
		for( Map.Entry< String, Class< ? extends Serializer > > entry : getInstance().serializerClasses.entrySet() )
		{
			if( entry.getValue().getName().equals( clazz.getName() ) )
			{
				name = entry.getKey();
				break;
			}
		}
		return name;
	}
	
	/**
	 * Puts a new {@link Serializer} {@link Class} and its associated name into
	 * the {@link SerializerFactory}.
	 * @param name The name of the {@link Serializer}
	 * @param clazz The {@link Class} of the {@link Serializer}
	 * @return The previous {@link Serializer} {@link Class} associated with the name; null
	 * if the name was previously unused.
	 */
	public synchronized Class< ? extends Serializer > putSerializer( final String name, final Class< ? extends Serializer > clazz )
	{
		return serializerClasses.put( name, clazz );
	}
	
	/**
	 * Creates the {@link Serializer} whose {@link Class} is associated with the specified name
	 * @param name The name of the {@link Serializer} to create
	 * @return The newly created {@link Serializer} whose {@link Class} is associated with the 
	 * specified name
	 */
	public synchronized Serializer createSerializer( final String name )
	{
		// grab the class from the mapping
		final Class< ? extends Serializer > clazz = serializerClasses.get( name );
		if( clazz == null )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to create a serializer of the specified name because there is no associated class." + Constants.NEW_LINE );
			message.append( "  Specified Name: " + name + Constants.NEW_LINE );
			message.append( "  Available Names: " + Constants.NEW_LINE );
			for( Map.Entry< String, Class< ? extends Serializer > > entry : serializerClasses.entrySet() )
			{
				message.append( "    " + entry.getKey() + ": " + entry.getValue().getName() + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// create the new instance of the class
		Serializer serializer = null;
		try
		{
			serializer = clazz.newInstance();
		}
		catch( InstantiationException | IllegalAccessException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to create a serializer of the specified name." + Constants.NEW_LINE );
			message.append( "  Specified Name: " + name + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalStateException( message.toString(), e );
		} 
		
		return serializer;
			
	}
	
	public enum SerializerType {
		
		OBJECT( "object_serialization", ObjectSerializer.class ),
		PERSISTENCE_KEY_VALUE( "persistence_key_value", KeyValuePersistenceSerializer.class ),
		PERSISTENCE_XML( "persistence_xml", XmlPersistenceSerializer.class ),
		PERSISTENCE_JSON( "persistence_json", JsonPersistenceSerializer.class );
		
		private String name;
		private Class< ? extends Serializer > clazz;
		
		private SerializerType( final String name, final Class< ? extends Serializer > clazz )
		{
			this.name = name;
			this.clazz = clazz;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Class< ? extends Serializer > getSerialzierClass()
		{
			return clazz;
		}
		
		public static String getSerializerName( final Class< ? > clazz )
		{
			String name = null;
			for( SerializerType type : values() )
			{
				if( clazz.equals( type.clazz ) )
				{
					name = type.name;
					break;
				}
			}
			return name;
		}
	}
	
}
