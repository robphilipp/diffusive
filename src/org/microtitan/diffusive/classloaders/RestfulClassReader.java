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
import org.microtitan.diffusive.diffuser.restful.request.ClassRequest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for reading the {@code byte} array that represents the serialized {@link Class} object returned
 * by the web service.
 *  
 * @author Robert Philipp
 */
public class RestfulClassReader {

	private static final Logger LOGGER = Logger.getLogger( RestfulClassReader.class );

	private final Abdera abdera;
	private final Client client;

	public RestfulClassReader( final Abdera abdera, final Client client )
	{
		this.abdera = abdera;
		this.client = client;
	}
	
	/**
	 * Reads the serialized {@link Class} object into a {@code byte} array. Searches through the
	 * {@link URI} listed in the class-paths (list of {@link URI} to search).
	 * @param className The fully-qualified class name
	 * @return the {@code byte} array representing the serialized {@link Class} object
	 * @see #readClassData(String, URI)
	 */
	public final byte[] readClassData( final String className, final List< URI > classPaths )
	{
		byte[] classBytes = null;
		
		for( URI uri : classPaths )
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

	/**
	 * Reads the {@link Class} object for the specified fully-qualified class name from the web service
	 * specified by the {@link URI}.
	 * @param className The fully-qualified class name for the {@link Class} object to load
	 * @param uri The {@link URI} of the web service from which to retrieve the serialized version
	 * of the {@link Class} object 
	 * @return the {@code byte} array representing the serialized {@link Class} object
	 * @see #readClassData(String)
	 */
	public final byte[] readClassData( final String className, final URI uri )
	{
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final ClassRequest request = ClassRequest.create( uri.toString(), className );
		
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
			message.append( "  Base URI: " + uri );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return classBytes;
	}
}
