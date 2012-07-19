package org.microtitan.diffusive.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.classloaders.RestfulClassLoader;
import org.microtitan.diffusive.tests.BeanTest;

public class ClassLoaderUtils {

	private static final Logger LOGGER = Logger.getLogger( ClassLoaderUtils.class );
	
	/**
	 * Converts the {@link Class} into a {@code byte[]}.
	 * @param clazz The {@link Class} to convert into a {@code byte[]}
	 * @return a {@code byte[]} representation of the {@link Class}
	 */
	public static byte[] convertClassToByteArray( final String className )
	{
		byte[] bytes = null;
		try
		{
			// mangle the class name as specified by the ClassLoader.getSystemResourceAsStream docs
			final String classAsPath = className.replace('.', '/') + ".class";

			// grab the input stream
			final InputStream input = ClassLoader.getSystemResourceAsStream( classAsPath );
			
			// convert to bytes
			bytes = IOUtils.toByteArray( input );
		}
		catch( IOException e )
		{
        	final StringBuffer message = new StringBuffer();
        	message.append( "Error writing out to the Class< ? > to a byte array." + Constants.NEW_LINE );
        	message.append( "  Class: " + className );
        	LOGGER.error( message.toString(), e );
        	throw new IllegalStateException( message.toString(), e );
		}
		
		return bytes;
	}
	
	public static void main( String[] args )
	{
		final String className = BeanTest.class.getName();
		byte[] bytes = convertClassToByteArray( className );
		
		Class< ? > clazz = new RestfulClassLoader( null, ClassLoaderUtils.class.getClassLoader() ).getClazz( className, bytes );
		try
		{
			final Method method = clazz.getMethod( "print" );
			method.invoke( clazz.newInstance() );
		}
		catch( IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException e )
		{
			e.printStackTrace();
		}
		
		System.out.println( clazz.getName() );
	}
}
