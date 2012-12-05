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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

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
	
//	/**
//	 * Loads and the converts the {@link Class} into a {@code byte[]}.
//	 * @param class The name of the {@link Class} to convert into a {@code byte[]}
//	 * @param classLoader The {@link URLClassLoader} used to load the class from a JAR path
//	 * @return a {@code byte[]} representation of the {@link Class}
//	 */
//	public static byte[] convertClassToByteArray( final String classname, final URLClassLoader classLoader )
//	{
//		byte[] bytes = null;
//		try( final PipedOutputStream pos = new PipedOutputStream();
//			 final PipedInputStream pis = new PipedInputStream( pos );
//			 final ObjectOutputStream oos = new ObjectOutputStream( pos );
//			 final ObjectInputStream ois = new ObjectInputStream( pis ) )
//		{
//			// load the class using the URL class loader that should already be set up with the JAR information
//			final Class< ? > clazz = Class.forName( classname, true, classLoader );
//
//			// write the Class object into the output stream
//			oos.writeObject( clazz );
//			
//			// convert the input stream (which was filled from the piped input stream)
//			bytes = IOUtils.toByteArray( ois );
//		}
//		catch( ClassNotFoundException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Failed to load class using URL class loader." + Constants.NEW_LINE );
//			message.append( "  Class Name: " + classname + Constants.NEW_LINE );
//			message.append( "  Class Loader: " + classLoader.getClass().getName() + Constants.NEW_LINE );
//			message.append( "  URL Class Path: " );
//			for( URL url : classLoader.getURLs() )
//			{
//				message.append( Constants.NEW_LINE + "    " + url.toString() );
//			}
//			LOGGER.info( message.toString(), e );
//			throw new IllegalArgumentException( message.toString(), e );
//		}
//		catch( IOException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Failed create, write, and/or read I/O streams." + Constants.NEW_LINE );
//			message.append( "  Class Name: " + classname + Constants.NEW_LINE );
//			message.append( "  Class Loader: " + classLoader.getClass().getName() + Constants.NEW_LINE );
//			message.append( "  URL Class Path: " );
//			for( URL url : classLoader.getURLs() )
//			{
//				message.append( Constants.NEW_LINE + "    " + url.toString() );
//			}
//			LOGGER.info( message.toString(), e );
//			throw new IllegalArgumentException( message.toString(), e );
//		}
//		
//		return bytes;
//	}

	/**
	 * Loads and the converts the {@link Class} into a {@code byte[]}.
	 * @param class The name of the {@link Class} to convert into a {@code byte[]}
	 * @param classLoader The {@link URLClassLoader} used to load the class from a JAR path
	 * @return a {@code byte[]} representation of the {@link Class}
	 */
	public static byte[] convertClassToByteArray( final String classname, final URLClassLoader classLoader )
	{
		byte[] bytes = null;
		try( final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 final ObjectOutput out = new ObjectOutputStream( bos ) )
		{
			// load the class using the URL class loader that should already be set up with the JAR information
			final Class< ? > clazz = Class.forName( classname, true, classLoader );

			// write the Class object into the output stream
			out.writeObject( clazz );
			out.close();
			
			// convert the input stream (which was filled from the piped input stream)
			bytes = bos.toByteArray();
		}
		catch( ClassNotFoundException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to load class using URL class loader." + Constants.NEW_LINE );
			message.append( "  Class Name: " + classname + Constants.NEW_LINE );
			message.append( "  Class Loader: " + classLoader.getClass().getName() + Constants.NEW_LINE );
			message.append( "  URL Class Path: " );
			for( URL url : classLoader.getURLs() )
			{
				message.append( Constants.NEW_LINE + "    " + url.toString() );
			}
			LOGGER.info( message.toString(), e );
//			throw new IllegalArgumentException( message.toString(), e );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed create, write, and/or read I/O streams." + Constants.NEW_LINE );
			message.append( "  Class Name: " + classname + Constants.NEW_LINE );
			message.append( "  Class Loader: " + classLoader.getClass().getName() + Constants.NEW_LINE );
			message.append( "  URL Class Path: " );
			for( URL url : classLoader.getURLs() )
			{
				message.append( Constants.NEW_LINE + "    " + url.toString() );
			}
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		
		return bytes;
	}

	public static void main( String[] args ) throws IOException
	{
//		Class< ? > clazz = null;
//		final URL url = new URL( "file", null, "//C:/Users/desktop/workspace/diffusive/Diffusive_v0.2.0/examples/example_0.2.0.jar" );
//		final URLClassLoader urlClassLoader = new URLClassLoader( new URL[] { url } );
//		final String classname = "org.microtitan.tests.threaded.MultiThreadedCalc";
//		byte[] bytes = null;
//		try
//		{
//			clazz = Class.forName( classname, true, urlClassLoader );
//			final PipedOutputStream pos = new PipedOutputStream();
//			final PipedInputStream pis = new PipedInputStream( pos );
//			final ObjectOutputStream oos = new ObjectOutputStream( pos );s
//			oos.writeObject( clazz );
//			bytes = IOUtils.toByteArray( new ObjectInputStream( pis ) );
////			final InputStream input = urlClassLoader.getResourceAsStream( classname + ".class" );
////			bytes = IOUtils.toByteArray( input );
//		}
//		catch( ClassNotFoundException e2 )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Failed to load class using URL class loader, attempting to use specific diffuser URL class loader." + Constants.NEW_LINE );
//			message.append( "  Class Name: " + classname + Constants.NEW_LINE );
//			message.append( "  Class Loader: " + urlClassLoader.getClass().getName() + Constants.NEW_LINE );
//			message.append( "  URL Class Path: " );
//			for( URL goturl : urlClassLoader.getURLs() )
//			{
//				message.append( Constants.NEW_LINE + "    " + goturl.toString() );
//			}
//			LOGGER.info( message.toString(), e2 );
//		}
//		catch( IOException e )
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		final String classname = "org.microtitan.tests.threaded.MultiThreadedCalc";
		final byte[] systemBytes = convertClassToByteArray( classname );
		if( systemBytes == null )
		{
			System.out.println( "null" );
		}
		else
		{
			System.out.println( systemBytes.length );
		}


		final URL url = new URL( "file", null, "//C:/Users/desktop/workspace/diffusive/Diffusive_v0.2.0/examples/example_0.2.0.jar" );
		final URLClassLoader urlClassLoader = new URLClassLoader( new URL[] { url } );
		System.out.println( convertClassToByteArray( classname, urlClassLoader ).length );
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
