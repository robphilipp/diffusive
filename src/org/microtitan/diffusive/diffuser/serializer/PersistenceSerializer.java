package org.microtitan.diffusive.diffuser.serializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.Persistence;
import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.tests.TestClassA;
import org.microtitan.diffusive.tests.TestClassB;

/**
 * Serializes objects into a serializes for specified by the {@link Persistence} engine, and deserializes
 * that form back into objects using the same {@link Persistence} engine.
 * 
 * @author Robert Philipp
 */
public class PersistenceSerializer implements Serializer {
	
	private static final Logger LOGGER = Logger.getLogger( PersistenceSerializer.class );

	private Persistence persistence;
	
	/**
	 * Constructs an object serializer that uses the FreezeDry persistence framework to 
	 * serialize the objects
	 * @param persistence The {@link Persistence} used to serialize and deserialize objects
	 */
	public PersistenceSerializer( final Persistence persistence )
	{
		this.persistence = persistence;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#serialize(java.lang.Object, java.io.OutputStream)
	 */
	@Override
	public void serialize( final Object object, final OutputStream output )
	{
		// convert the OutputStream to a Writer
		try( final OutputStreamWriter out = new OutputStreamWriter( output ) )
		{
			// have the Persistence write the object to the output stream
			if( object != null )
			{
				persistence.write( object, out );
			}
		
			out.close();
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
	 * @see org.microtitan.diffusive.diffuser.serializer.Serializer#deserialize(java.io.InputStream, java.lang.Class)
	 */
	@Override
	public < T > T deserialize( final InputStream input, final Class< T > clazz )
	{
		T object = null;
		// convert the InputStream to a Reader
		try( final InputStreamReader in = new InputStreamReader( input ) )
		{
			// create the object from the input stream
			object = persistence.read( clazz, in );
		}
		catch( IOException e )
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

	/**
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		final TestClassB b = new TestClassB();
		b.setMessage(  "test_class_bee" );
		final TestClassA a = new TestClassA( b );

//		final Serializer serializer = new PersistenceSerializer( new XmlPersistence() );
		final Serializer serializer = new XmlPersistenceSerializer();
//		final Serializer serializer = new JsonPersistenceSerializer();
//		final Serializer serializer = new KeyValuePersistenceSerializer();
		try
		{
			serializer.serialize( a, new FileOutputStream( "test_a.xml" ) );
			
			final TestClassA serA = serializer.deserialize( new FileInputStream( "test_a.xml" ), TestClassA.class );
			
			System.out.println( serA.toString() );
		}
		catch( FileNotFoundException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
