package org.microtitan.diffusive.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

public class ClassLoaderUtils {

	private static final Logger LOGGER = Logger.getLogger( ClassLoaderUtils.class );
	
	/**
	 * Converts the {@link Class} into a {@code byte[]}.
	 * @param clazz The {@link Class} to convert into a {@code byte[]}
	 * @return a {@code byte[]} representation of the {@link Class}
	 */
	public static byte[] convertClassToByteArray( final Class< ? > clazz )
	{
		byte[] bytes = null;
		try( final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			 final ObjectOutputStream objectOutputStream = new ObjectOutputStream( byteOutputStream ) )
		{
			objectOutputStream.writeObject( clazz );
			bytes = byteOutputStream.toByteArray();
		}
        catch( IOException e )
        {
        	final StringBuffer message = new StringBuffer();
        	message.append( "Error writing out to the Class< ? > to a byte array." + Constants.NEW_LINE );
        	message.append( "  Class: " + clazz.getName() );
        	LOGGER.error( message.toString(), e );
        	throw new IllegalStateException( message.toString(), e );
        }
		return bytes;
	}
}
