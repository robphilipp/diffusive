package org.microtitan.diffusive.diffuser.restful.resources;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.utils.ClassLoaderUtils;

@Path( RestfulClassPathResource.CLASSPATH_PATH )
public class RestfulClassPathResource {

	private static final Logger LOGGER = Logger.getLogger( RestfulClassPathResource.class );

	public static final String CLASSPATH_PATH = "/classpath"; 

	// parameters for retrieving a Class for a specific diffuser
	public static final String FULLY_QUALIFIED_CLASS_NAME = "classname";
	
	/**
	 * 
	 */
	public RestfulClassPathResource()
	{
		
	}
	
	/**
	 * 
	 * @param uriInfo
	 * @param className
	 * @return
	 */
	@GET @Path( "{" + FULLY_QUALIFIED_CLASS_NAME + ": [a-zA-Z0-9\\.\\-]*}" )
	public Response getClass( @Context final UriInfo uriInfo,
							  @PathParam( FULLY_QUALIFIED_CLASS_NAME ) final String className )
	{
		// find the class and convert it to a byte[] for transport across the network
		final byte[] classBytes = ClassLoaderUtils.convertClassToByteArray( className );
		if( classBytes == null || classBytes.length == 0 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Could not find class: " + className + Constants.NEW_LINE );
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// grab the date for time stamp
		final Date date = new Date();

		// create the atom feed 
		final URI requestUri = uriInfo.getRequestUri();
		final String resultKey = UUID.randomUUID().toString();
		final Feed feed = Atom.createFeed( requestUri, resultKey, date, uriInfo.getBaseUri() );
		
		// create an entry for the feed and set the byte[] representing the class as the content
		final Entry entry = Atom.createEntry();
		final ByteArrayInputStream input = new ByteArrayInputStream( classBytes );
		entry.setId( resultKey );
		entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
		feed.addEntry( entry );
		
		// create the response
		final Response response = Response.ok()
										  .location( requestUri )
										  .entity( feed.toString() )
										  .type( MediaType.APPLICATION_ATOM_XML )
										  .build();
		return response;
	}
}
