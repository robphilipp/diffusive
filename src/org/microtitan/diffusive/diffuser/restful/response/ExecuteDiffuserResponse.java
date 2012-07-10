package org.microtitan.diffusive.diffuser.restful.response;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;

public class ExecuteDiffuserResponse extends AbstractDiffuserResponse {

	private static final Logger LOGGER = Logger.getLogger( ExecuteDiffuserResponse.class );
	
	private URI resultUri;
	private String signature;
	private Calendar publishedDate;
	private String resultId;
	private String requestId;
	
	/**
	 * Constructs a {@link ExecuteDiffuserResponse} object by parsing the feed for the relevant entry information.
	 * The parent class parse the containing feed information.
	 * @param feed The Atom feed to parse for the relevant entry information 
	 */
	public ExecuteDiffuserResponse( final Feed feed )
	{
		super( feed );
	}

	/* (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.AbstractDiffuserResponse#parse(org.apache.abdera.model.Feed)
	 */
	@Override
	protected void parse( final Feed feed )
	{
		// grab the entry holding the result ID and ensure its actually there
		final Entry resultIdEntry = feed.getEntry( RestfulDiffuserManagerResource.RESULT_ID );
		if( resultIdEntry == null )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error parsing feed into response object. Feed must have one entry containing the result ID." + Constants.NEW_LINE );
			message.append( "  Required ID: " + RestfulDiffuserManagerResource.RESULT_ID + Constants.NEW_LINE );
			message.append( "  Specified IDs: " + Constants.NEW_LINE );
			for( Entry entry : feed.getEntries() )
			{
				try
				{
					message.append( "    " + entry.getId().toURI().toString() + Constants.NEW_LINE );
				}
				catch( URISyntaxException e ) 
				{ 
					// do nothing at this point
				}
			}
			LOGGER.warn( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}

		// grab the entry holding the original request ID and ensure its actually there
		final Entry requestIdEntry = feed.getEntry( RestfulDiffuserManagerResource.REQUEST_ID );
		if( requestIdEntry == null )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error parsing feed into response object. Feed must have one entry containing the request ID." + Constants.NEW_LINE );
			message.append( "  Required ID: " + RestfulDiffuserManagerResource.REQUEST_ID + Constants.NEW_LINE );
			message.append( "  Specified IDs: " + Constants.NEW_LINE );
			for( Entry entry : feed.getEntries() )
			{
				try
				{
					message.append( "    " + entry.getId().toURI().toString() + Constants.NEW_LINE );
				}
				catch( URISyntaxException e ) 
				{ 
					// do nothing at this point
				}
			}
			LOGGER.warn( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// parse the entry information
		try
		{
			final Link resultLink = resultIdEntry.getLink( Link.REL_SELF );
			if( resultLink != null )
			{
				resultUri = resultLink.getHref().toURI();
				publishedDate = Calendar.getInstance();
				publishedDate.setTime( resultIdEntry.getPublished() );
				resultId = resultIdEntry.getContent();
				requestId = requestIdEntry.getContent();
				signature = resultId.split( Pattern.quote( "/" ) )[ 0 ];
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Diffuser " + signature + " is missing a link to its resource representation." );
				LOGGER.warn( message.toString() );
			}
		}
		catch( URISyntaxException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error parsing URI for the following diffuser: " + Constants.NEW_LINE );
			message.append( "  Diffuser ID: " + resultIdEntry.getId().toString() );
			LOGGER.warn( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
	}

	/**
	 * @return the URI to the diffuser resource representation
	 */
	public URI getResultUri()
	{
		return resultUri;
	}

	/**
	 * @return the signature (ID) of the diffuser
	 */
	public String getSignature()
	{
		return signature;
	}

	/**
	 * @return the date-time when the entry was published
	 */
	public Calendar getPublishedDate()
	{
		return publishedDate;
	}

	/**
	 * @return the ID of the result which is a concatenation of the signature and the
	 * request ID. Specifically, the result ID = {signature}/{requestID}.
	 */
	public String getResultId()
	{
		return resultId;
	}
	
	/**
	 * @return the ID of the original request
	 */
	public String getRequestId()
	{
		return requestId;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.AbstractDiffuserResponse#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer message = new StringBuffer();
		message.append( super.toString() );
		message.append( "  Result URI: " + resultUri + Constants.NEW_LINE );
		message.append( "  Result ID: " + resultId + Constants.NEW_LINE );
		message.append( "  Request ID: " + requestId + Constants.NEW_LINE );
		message.append( "  Signature: " + signature + Constants.NEW_LINE );
		message.append( "  Published Date: " + new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS" ).format( publishedDate.getTime() ) );
		return message.toString();
	}
}
