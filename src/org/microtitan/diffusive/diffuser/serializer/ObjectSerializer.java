package org.microtitan.diffusive.diffuser.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.tests.TestClassA;
import org.microtitan.diffusive.tests.TestClassB;

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
	public void serialize( final Object object, final OutputStream output )
	{
		try( final ObjectOutputStream out = new ObjectOutputStream( output ) )
		{
			out.writeObject( object );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to serialize object to output stream:" + Constants.NEW_LINE );
			message.append( "  Output Stream Type: " + output.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Object Type: " + object.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Object: " + object.toString() + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#deserialize(java.io.InputStream)
	 */
	@Override
	public < T > T deserialize( final InputStream input, final Class< T > clazz ) 
	{
		T object = null;
		try( final ObjectInputStream in = new ObjectInputStream( input ) )
		{
			object = clazz.cast( in.readObject() );
		}
		catch( IOException | ClassNotFoundException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Unable to serialize object to output stream:" + Constants.NEW_LINE );
			message.append( "  Input Stream Type: " + input.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Object Type: " + clazz.getName() + Constants.NEW_LINE );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return object;
	}

	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		final TestClassB b = new TestClassB();
		b.setMessage(  "test_class_bee" );
		final TestClassA a = new TestClassA( b );
		
//		final Serializer serializer = new ObjectSerializer();
		final Serializer serializer = new XmlPersistenceSerializer();
		
		// write the object to a byte array and the reconstitute the object
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize( a, out );
			out.flush();
			final byte[] bytes = out.toByteArray();
			final String object = new String( bytes );
			System.out.println( "StringWriter: " + object );
			System.out.println( "StringWriter: " + object.getBytes() );
			
			final TestClassA desA = serializer.deserialize( new ByteArrayInputStream( bytes ), TestClassA.class );
			System.out.println( desA.toString() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

//		// write the object to a String and the reconstitute the object
//		try
//		{
//			final StringWriter stringWriter = new StringWriter();
//			final WriterOutputStream writer = new WriterOutputStream( stringWriter );
//			serializer.serialize( a, writer );
//			writer.flush();
//			final String serializedClass = stringWriter.toString();
//			System.out.println( "StringWriter: " + serializedClass );
//			
//			final TestClassA desA = serializer.deserialize( new ReaderInputStream( new StringReader( serializedClass ) ), TestClassA.class );
//			System.out.println( desA.toString() );
//		}
//		catch( IOException e )
//		{
//			e.printStackTrace();
//		}

//		// write object to a file and then read it in again and reconstitute the object
//		try
//		{
//			serializer.serialize( a, new FileOutputStream( "test_a" ) );
//			
//			final TestClassA desA = serializer.deserialize( new FileInputStream( "test_a" ), TestClassA.class );
//			System.out.println( desA.toString() );
//		}
//		catch( FileNotFoundException e )
//		{
//			e.printStackTrace();
//		}
	}
}