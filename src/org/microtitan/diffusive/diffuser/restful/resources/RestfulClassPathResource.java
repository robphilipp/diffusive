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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.utils.ClassLoaderUtils;

@Path( RestfulClassPathResource.CLASSPATH_PATH )
public class RestfulClassPathResource {

	private static final Logger LOGGER = Logger.getLogger( RestfulClassPathResource.class );

	public static final String CLASSPATH_PATH = "classpath/"; 

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
		Class< ? > clazz = null;
		try
        {
			clazz = Class.forName( className );
        }
        catch( ClassNotFoundException e )
        {
        	// in this case, the class isn't found, which is a problem, because it should
        	// be on the class path...we could add in the file system class loader...?
	        e.printStackTrace();
        }
		final byte[] classBytes = ClassLoaderUtils.convertClassToByteArray( clazz );
		
		// grab the date for time stamp
		final Date date = new Date();

		Response response = null;
		if( classBytes != null && classBytes.length > 0 )
		{
			Feed feed = null;
			
			// create the atom feed 
			final URI requestUri = uriInfo.getRequestUri();
			final String resultKey = UUID.nameUUIDFromBytes( classBytes ).toString();
			feed = Atom.createFeed( requestUri, resultKey, date, uriInfo.getBaseUri() );
			
			// create an entry for the feed and set the results as the content
			final Entry entry = Atom.createEntry();
			final ByteArrayInputStream input = new ByteArrayInputStream( classBytes );
			entry.setId( resultKey );
			entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
			feed.addEntry( entry );
			
			// create the response
			response = Response.ok( classBytes )
							   .status( Status.OK )
							   .location( requestUri )
							   .entity( feed.toString() )
							   .type( MediaType.APPLICATION_OCTET_STREAM )
							   .build();
		}
		else
		{
			response = Response.noContent().build();
		}
		return response;
	}
}
