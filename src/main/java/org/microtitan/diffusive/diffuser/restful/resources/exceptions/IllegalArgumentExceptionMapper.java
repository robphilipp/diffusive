package org.microtitan.diffusive.diffuser.restful.resources.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;

public class IllegalArgumentExceptionMapper implements ExceptionMapper< IllegalArgumentException > {

	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	@Override
	public Response toResponse( final IllegalArgumentException e )
	{
		final Feed feed = Atom.createFeed();
		
		final Entry entry = Atom.createEntry();
		entry.setContent( e.getMessage() );
		feed.addEntry( entry );
		
		final Response response = Response.status( Status.INTERNAL_SERVER_ERROR )
				   						  .entity( feed.toString() )
				   						  .build();

		return response;
	}
}
