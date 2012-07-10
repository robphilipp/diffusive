package org.microtitan.diffusive.diffuser.restful.response;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

public class ExecuteDiffuserResponse extends AbstractDiffuserResponse {

	private static final Logger LOGGER = Logger.getLogger( ExecuteDiffuserResponse.class );
	
	private URI resultUri;
	private String signature;
	private Calendar publishedDate;
	private String resultId; 
	
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
		// grab the one and only one entry
		final List< Entry > entries = feed.getEntries();
		if( entries == null || entries.size() != 1 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error parsing feed into response object. Feed must have exactly one entry." + Constants.NEW_LINE );
			message.append( "  Number of Entries: " + ( entries == null ? "[null" : entries.size() ) );
			LOGGER.warn( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		final Entry entry = entries.get( 0 );
		
		// parse the entry information
		try
		{
			signature = entry.getId().toString();
			final Link resultLink = entry.getLink( Link.REL_SELF );
			if( resultLink != null )
			{
				resultUri = resultLink.getHref().toURI();
				publishedDate = Calendar.getInstance();
				publishedDate.setTime( entry.getPublished() );
				resultId = entry.getContent();
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
			message.append( "  Diffuser ID: " + entry.getId().toString() );
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
	 * @return the ID of the result.
	 */
	public String getResultId()
	{
		return resultId;
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
		message.append( "  Signature: " + signature + Constants.NEW_LINE );
		message.append( "  Published Date: " + new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS" ).format( publishedDate.getTime() ) );
		return message.toString();
	}
}
