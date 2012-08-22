package org.microtitan.diffusive.classloaders;

import java.net.URI;
import java.util.List;

import org.microtitan.diffusive.diffuser.restful.atom.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.client.RestfulClientFactory;

/**
 * Class loader that accesses a web service to load a {@link Class} object from a {@code byte} array
 * that represents a serialized {@link Class} object. The serialized {@link Class} object is returned 
 * the web service. Logic is contained it the overridden {@link #findClass(String)} method. The 
 * {@link #getClazz(String, byte[])} method is a convenience method.
 * 
 * @author Robert Philipp
 */
public class RestfulClassLoader extends ClassLoader {
	
	private List< URI > classPaths;
	private final RestfulClassReader classReader;

	/**
	 * 
	 * @param classPaths The {@link List} of class path URI
	 * @param parent The class loader that is set to be this parents class loader
	 */
	public RestfulClassLoader( final List< URI > classPaths, final ClassLoader parent )
	{
		super( parent );
		
		// the base URI of the resource
		this.classPaths = classPaths;

		// create the RESTful class data reader
		this.classReader = new RestfulClassReader( AbderaFactory.getInstance(), RestfulClientFactory.getInstance() );
	}

	/**
	 * RESTful class loader that uses its class loader as the parent class loader
	 * @param classPaths The {@link List} of class path URI
	 */
	public RestfulClassLoader( final List< URI > classPaths )
	{
		this( classPaths, RestfulClassLoader.class.getClassLoader() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	public Class< ? > findClass( final String className ) throws ClassNotFoundException
	{
		// read the bytes from the network
		final byte[] bytes = classReader.readClassData( className, classPaths );
		if( bytes == null || bytes.length == 0 )
		{
			throw new ClassNotFoundException( className );
		}
		
		// define the class 
		final Class< ? > clazz = defineClass( className, bytes, 0, bytes.length );
		if( clazz == null )
		{
			throw new ClassFormatError( className );
		}

		return clazz;
	}

	/**
	 * Returns the {@link Class} object for the specified, fully-qualified class name and
	 * the {@code byte} array containing the serialized {@link Class} object.
	 * @param className The fully-qualified class name
	 * @param bytes The serialized {@link Class} object
	 * @return
	 */
	public Class< ? > getClazz( final String className, final byte[] bytes )
	{
		final Class< ? > clazz = defineClass( className, bytes, 0, bytes.length );
		resolveClass( clazz );
		return clazz;
	}
}
