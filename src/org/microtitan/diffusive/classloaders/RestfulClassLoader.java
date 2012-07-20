package org.microtitan.diffusive.classloaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.atom.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.client.RestfulClientFactory;
import org.microtitan.diffusive.diffuser.restful.request.ClassRequest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestfulClassLoader extends ClassLoader {
	
	private static final Logger LOGGER = Logger.getLogger( RestfulClassLoader.class );

	private List< URI > baseUri;
	private final Abdera abdera;
	private final Client client;

	/**
	 * 
	 * @param baseUri The {@link List} of class path URI
	 * @param parent The class loader that is set to be this parents class loader
	 */
	public RestfulClassLoader( final List< URI > baseUri, final ClassLoader parent )
	{
		super( parent );
		
		// the base URI of the resource
		this.baseUri = baseUri;

		// atom parser/create
		this.abdera = AbderaFactory.getInstance();

		// create the Jersey RESTful client
		this.client = RestfulClientFactory.getInstance();
	}

	/**
	 * RESTful class loader that uses its class loader as the parent class loader
	 * @param baseUri The {@link List} of class path URI
	 */
	public RestfulClassLoader( final List< URI > baseUri )
	{
		this( baseUri, RestfulClassLoader.class.getClassLoader() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	public Class< ? > findClass( final String className ) throws ClassNotFoundException
	{
		// read the bytes from the network
		final byte[] bytes = readClassData( className );
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
	
	public Class< ? > getClazz( final String className, final byte[] bytes )
	{
		final Class< ? > clazz = defineClass( className, bytes, 0, bytes.length );
		resolveClass( clazz );
		return clazz;
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	private byte[] readClassData( final String className )
	{
		byte[] classBytes = null;
		
		for( URI uri : baseUri )
		{
			// load the class data from the URI in the list, if found, then we're done
			classBytes = readClassData( className, uri );
			if( classBytes != null )
			{
				break;
			}
		}

		return classBytes;
	}

	private byte[] readClassData( final String className, final URI baseUri )
	{
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final ClassRequest request = ClassRequest.create( baseUri.toString(), className );
		
		// create the web resource for making the call, make the call to PUT the create-request to the server
		final WebResource resource = client.resource( request.getUri().toString() );
		final ClientResponse classResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );
		
		// parse the response into an Atom feed object and return it
		byte[] classBytes = null;
		try( InputStream response = classResponse.getEntity( InputStream.class ) )
		{
			final Feed feed = abdera.getParser().< Feed >parse( response ).getRoot();

			// grab the content from the entry and convert it to a byte array
			final InputStream objectStream = feed.getEntries().get( 0 ).getContentStream();
			classBytes = new byte[ objectStream.available() ];
			objectStream.read( classBytes );
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to parse the class-request response into an Atom feed" + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Base URI: " + baseUri );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return classBytes;
	}
}
