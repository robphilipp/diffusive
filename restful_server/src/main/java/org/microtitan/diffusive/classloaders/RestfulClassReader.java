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
package org.microtitan.diffusive.classloaders;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.request.ClassRequest;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

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
	 *
	 * @param className The fully-qualified class name
	 * @return the {@code byte} array representing the serialized {@link Class} object
	 * @see #readClassData(String, URI)
	 */
	public final byte[] readClassData( final String className, final List<URI> classPaths )
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
	 *
	 * @param className The fully-qualified class name for the {@link Class} object to load
	 * @param uri       The {@link URI} of the web service from which to retrieve the serialized version
	 *                  of the {@link Class} object
	 * @return the {@code byte} array representing the serialized {@link Class} object
	 * @see #readClassData(String, java.net.URI)
	 */
	public synchronized final byte[] readClassData( final String className, final URI uri )
	{
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final ClassRequest request = ClassRequest.create( uri.toString(), className );

		// create the web resource for making the call, make the call to PUT the create-request to the server
		final WebResource resource = client.resource( request.getUri().toString() );
		final ClientResponse classResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );

		// parse the response into an Atom feed object and return it
		byte[] classBytes;
		try( InputStream response = classResponse.getEntity( InputStream.class ) )
		{
			// grab the feed
			final Feed feed = abdera.getParser().<Feed>parse( response ).getRoot();

			// grab the entries, and throw an exception if there are no entries
			final List<Entry> entries = feed.getEntries();
			if( entries == null || entries.isEmpty() )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Message has no class data. Somethings seems to have gone with the class server." ).append( Constants.NEW_LINE );
				message.append( "  Class Name: " ).append( className ).append( Constants.NEW_LINE );
				message.append( "  Base URI: " ).append( uri ).append( Constants.NEW_LINE );
				message.append( "  Feed: " ).append( feed.toString() );
				LOGGER.error( message.toString() );
				throw new IllegalStateException( message.toString() );
			}

			// grab the content from the entry and convert it to a byte array, closing the stream when done
			try( final InputStream objectStream = entries.get( 0 ).getContentStream() )
			{
				classBytes = IOUtils.toByteArray( objectStream );
			} catch( IOException e )
			{
				final StringBuilder message = new StringBuilder();
				message.append( "Failed to open input stream from the Atom feed that holds the object data for the class" ).append( Constants.NEW_LINE );
				message.append( "  Class Name: " ).append( className ).append( Constants.NEW_LINE );
				message.append( "  Base URI: " ).append( uri );
				LOGGER.error( message.toString(), e );
				throw new IllegalStateException( message.toString(), e );
			}
		} catch( ParseException | IOException e )
		{
			final StringBuilder message = new StringBuilder();
			message.append( "Failed to parse the class-request response into an Atom feed" ).append( Constants.NEW_LINE );
			message.append( "  Class Name: " ).append( className ).append( Constants.NEW_LINE );
			message.append( "  Base URI: " ).append( uri );
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return classBytes;
	}
}
