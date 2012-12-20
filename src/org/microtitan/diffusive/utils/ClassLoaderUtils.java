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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;

/**
 * Utilities for converting a {@link Class}, reference by name, into a {@code byte[]} for transporting
 * across the network. The {@link #loadClassToByteArray(String)} method uses the application
 * class loader and the application's class path to search for the {@link Class} file and load it
 * as a resource. The {@link #loadClassToByteArray(String, URLClassLoader)} uses the specified
 * {@link URLClassLoader} to search the JAR files held in the {@link URLClassLoader} and loads the
 * {@link Class} file as a resource from that JAR.
 * 
 * @author Robert Philipp
 */
public class ClassLoaderUtils {

	private static final Logger LOGGER = Logger.getLogger( ClassLoaderUtils.class );
	
	/**
	 * Converts the {@link Class} into a {@code byte[]}.
	 * @param clazz The {@link Class} to convert into a {@code byte[]}
	 * @return a {@code byte[]} representation of the {@link Class}
	 */
	public static byte[] loadClassToByteArray( final String className )
	{
		byte[] bytes = null;
		try
		{
			// mangle the class name as specified by the ClassLoader.getSystemResourceAsStream docs
			final String classAsPath = className.replace('.', '/') + ".class";

			// grab the input stream
			final InputStream input = ClassLoader.getSystemResourceAsStream( classAsPath );
			
			// convert to bytes if the input stream exists
			if( input != null )
			{
				bytes = IOUtils.toByteArray( input );
			}
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

	/**
	 * Converts the {@link Class} into a {@code byte[]}. The method allows you to load a {@link Class} file,
	 * as a resource,that is found in a JAR file.
	 * @param clazz The {@link Class} to convert into a {@code byte[]}
	 * @return a {@code byte[]} representation of the {@link Class}
	 */
	public static byte[] loadClassToByteArray( final String className, final URLClassLoader classLoader )
	{
		byte[] bytes = null;
		try
		{
			// mangle the class name as specified by the ClassLoader.getSystemResourceAsStream docs
			final String classAsPath = className.replace( '.', '/' ) + ".class";

			// grab the input stream
			InputStream input = classLoader.getResourceAsStream( classAsPath );
			if( input == null )
			{
				input = classLoader.getResourceAsStream( "/" + classAsPath );
			}
			
			// convert to bytes if the input stream exists
			if( input != null )
			{
				bytes = IOUtils.toByteArray( input );
			}
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
	
	public static void main( String[] args ) throws IOException
	{
		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		final String classname = "org.microtitan.tests.threaded.MultiThreadedCalc";
		final byte[] systemBytes = loadClassToByteArray( classname );
		if( systemBytes == null )
		{
			System.out.println( "null" );
		}
		else
		{
			System.out.println( systemBytes.length );
		}

		final URL url = new URL( "file", null, "///C:/Users/desktop/workspace/diffusive/Diffusive_v0.2.0/examples/example_0.2.0.jar" );
//		final URL url = new URL( "file", null, "/Users/rob/Documents/workspace/diffusive/Diffusive_v0.2.0/examples/example_0.2.0.jar" );
		System.out.println( url.toString() );
		final URLClassLoader urlClassLoader = new URLClassLoader( new URL[] { url } );
		final byte[] urlBytes = loadClassToByteArray( classname, urlClassLoader );
		if( urlBytes == null )
		{
			System.out.println( "null" );
		}
		else
		{
			System.out.println( urlBytes.length );
		}
		
		final InputStream input = urlClassLoader.getResourceAsStream( classname.replace( '.', '/' ) + ".class" );
		if( input == null )
		{
			System.out.println( "null" );
		}
		else
		{
			System.out.println( IOUtils.toByteArray( input ).length );
		}
		urlClassLoader.close();
		
//		final String className = BeanTest.class.getName();
//		byte[] bytes = convertClassToByteArray( className );
//		
//		Class< ? > clazz = new RestfulClassLoader( null, ClassLoaderUtils.class.getClassLoader() ).getClazz( className, bytes );
//		try
//		{
//			final Method method = clazz.getMethod( "print" );
//			method.invoke( clazz.newInstance() );
//		}
//		catch( IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | InstantiationException e )
//		{
//			e.printStackTrace();
//		}
//		
//		System.out.println( clazz.getName() );
	}
}
