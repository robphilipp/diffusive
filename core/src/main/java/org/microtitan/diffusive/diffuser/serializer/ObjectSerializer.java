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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

/**
 * Serializes objects into Java serializes form and deserializes them back into objects.
 * 
 * @author Robert Philipp
 */
public class ObjectSerializer implements Serializer {
	
	private static final Logger LOGGER = Logger.getLogger( ObjectSerializer.class );

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#serialize(java.lang.Object, java.io.OutputStream)
	 */
	@Override
	public synchronized void serialize( final Object object, final OutputStream output )
	{
		try( final ObjectOutputStream out = new ObjectOutputStream( output ) )
		{
			out.writeObject( object );
		}
		catch( IOException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to serialize object to output stream:" ).append( Constants.NEW_LINE );
			message.append( "  Output Stream Type: " ).append( output.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Object Type: " ).append( object.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Object: " ).append( object.toString() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#deserialize(java.io.InputStream)
	 */
	@Override
	public synchronized < T > T deserialize( final InputStream input, final Class< T > clazz ) 
	{
		// read the input stream into an object. we use the the (apache commons-io) ClassLoaderObjectInputStream
		// to read the object because we need to be able to use the same class loader that loaded the class in
		// the first place (for example, the RestfulClassLoader).
		T object;
		try( final ClassLoaderObjectInputStream in = new ClassLoaderObjectInputStream( clazz.getClassLoader(), input ) )
		{
			object = clazz.cast( in.readObject() );
		}
		catch( IOException | ClassNotFoundException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Unable to serialize object to output stream:" ).append( Constants.NEW_LINE );
			message.append( "  Input Stream Type: " ).append( input.getClass().getName() ).append( Constants.NEW_LINE );
			message.append( "  Object Type: " ).append( clazz.getName() ).append( Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return object;
	}
	
//	/**
//	 * 
//	 * @param args
//	 */
//	public static void main( String[] args )
//	{
//		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.DEBUG );
//		
//		final TestClassB b = new TestClassB();
//		b.setMessage(  "test_class_bee" );
//		final TestClassA a = new TestClassA( b );
//		
////		final Serializer serializer = new ObjectSerializer();
//		final Serializer serializer = new XmlPersistenceSerializer();
//		
//		// write the object to a byte array and the reconstitute the object
//		try
//		{
//			final ByteArrayOutputStream out = new ByteArrayOutputStream();
//			serializer.serialize( a, out );
//			out.flush();
//			final byte[] bytes = out.toByteArray();
//			final String object = new String( bytes );
//			System.out.println( "StringWriter: " + object );
//			System.out.println( "StringWriter: " + object.getBytes() );
//			
//			final TestClassA desA = serializer.deserialize( new ByteArrayInputStream( bytes ), TestClassA.class );
//			System.out.println( desA.toString() );
//		}
//		catch( IOException e )
//		{
//			e.printStackTrace();
//		}
//
////		// write the object to a String and the reconstitute the object
////		try
////		{
////			final StringWriter stringWriter = new StringWriter();
////			final WriterOutputStream writer = new WriterOutputStream( stringWriter );
////			serializer.serialize( a, writer );
////			writer.flush();
////			final String serializedClass = stringWriter.toString();
////			System.out.println( "StringWriter: " + serializedClass );
////			
////			final TestClassA desA = serializer.deserialize( new ReaderInputStream( new StringReader( serializedClass ) ), TestClassA.class );
////			System.out.println( desA.toString() );
////		}
////		catch( IOException e )
////		{
////			e.printStackTrace();
////		}
//
////		// write object to a file and then read it in again and reconstitute the object
////		try
////		{
////			serializer.serialize( a, new FileOutputStream( "test_a" ) );
////			
////			final TestClassA desA = serializer.deserialize( new FileInputStream( "test_a" ), TestClassA.class );
////			System.out.println( desA.toString() );
////		}
////		catch( FileNotFoundException e )
////		{
////			e.printStackTrace();
////		}
//	}
}
