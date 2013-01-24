///*
//(c) 2005, Binildas C. A., biniljava<at>yahoo.co.in
//relased under terms of the GNU public license
//http://www.gnu.org/licenses/licenses.html#TOCGPL
// */
//package org.microtitan.diffusive.classloaders;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
//import org.apache.log4j.Logger;
//import org.microtitan.diffusive.Constants;
//
///**
// * Modified from the above base code.
// * 
// * @author Robert Philipp Jul 17, 2012
// * 
// */
//public class FileSystemClassLoader extends ClassLoader {
//	
//	private static final Logger LOGGER = Logger.getLogger( FileSystemClassLoader.class );
//
//	private String currentRoot = null;
//
//	public FileSystemClassLoader() throws FileNotFoundException
//	{
//		this( FileSystemClassLoader.class.getClassLoader(), System.getProperties().getProperty( "java.home" ) );
//	}
//
//	public FileSystemClassLoader( final String root ) throws FileNotFoundException
//	{
//		this( FileSystemClassLoader.class.getClassLoader(), root );
//	}
//
//	public FileSystemClassLoader( final ClassLoader parent ) throws FileNotFoundException
//	{
//		this( parent, System.getProperties().getProperty( "java.home" ) );
//	}
//
//	public FileSystemClassLoader( final ClassLoader parent, String root ) throws FileNotFoundException
//	{
//		super( parent );
//		File f = new File( root );
//		if( f.isDirectory() )
//		{
//			currentRoot = root;
//		}
//		else
//		{
//			throw new FileNotFoundException();
//		}
//	}
//
//	public byte[] findClassBytes( final String className )
//	{
//		final String pathName = currentRoot + File.separatorChar + className.replace( '.', File.separatorChar ) + ".class";
//
//		byte[] classBytes = null;
//		try( final FileInputStream inFile = new FileInputStream( pathName ) )
//		{
//			classBytes = new byte[ inFile.available() ];
//			inFile.read( classBytes );
//		}
//		catch( IOException e )
//		{
//			final StringBuffer message = new StringBuffer();
//			message.append( "Error reading class file" + Constants.NEW_LINE );
//			message.append( "  Class Name: " + className + Constants.NEW_LINE );
//			message.append( "  Path: " + pathName );
//			LOGGER.error( message.toString(), e );
//			throw new IllegalArgumentException( message.toString(), e );
//		}
//		return classBytes;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.ClassLoader#findClass(java.lang.String)
//	 */
//	@Override
//	public Class< ? > findClass( final String name ) throws ClassNotFoundException
//	{
//		final byte[] classBytes = findClassBytes( name );
//		if( classBytes == null )
//		{
//			throw new ClassNotFoundException();
//		}
//		else
//		{
//			return defineClass( name, classBytes, 0, classBytes.length );
//		}
//	}
//
//	public Class< ? > findClass( final String name, final byte[] classBytes ) throws ClassNotFoundException
//	{
//		if( classBytes == null )
//		{
//			throw new ClassNotFoundException( "(classBytes==null)" );
//		}
//		else
//		{
//			return defineClass( name, classBytes, 0, classBytes.length );
//		}
//	}
//}