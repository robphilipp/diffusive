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
package org.microtitan.diffusive.diffuser.restful.response;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.microtitan.diffusive.Constants;

public abstract class AbstractDiffuserResponse implements DiffuserResponse {

	private final Feed feed;
	
	private final URI id;
	private final String title;
	private final Calendar updated;
	private final URI self;
	
	/**
	 * Constructor of the {@link org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse} that calls the {@link #parse(org.apache.abdera.model.Feed)} method
	 * on the subclass. The subclass' {@link #parse(org.apache.abdera.model.Feed)} method is responsible for setting the
	 * appropriate fields in the response
	 * @param feed The Atom feed
	 */
	public AbstractDiffuserResponse( final Feed feed )
	{
		this.feed = feed;

		// pull out the feed ID as a URI
		try
		{
			id = feed.getId().toURI();
		}
		catch( URISyntaxException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Could not parse the Atom feed's ID into a URI." + Constants.NEW_LINE );
			message.append( "  ID: " + feed.getId().toString() + Constants.NEW_LINE );
			message.append( "  Feed: " + feed.toString() );
			
			throw new IllegalArgumentException( message.toString(), e );
		}
		
		// set the title
		title = feed.getTitle();

		// set the update/create date
		updated = Calendar.getInstance();
		updated.setTime( feed.getUpdated() );
		
		// grab the link to the newly created resource
		try
		{
			final Link selfLink = feed.getLink( Link.REL_SELF );
			if( selfLink != null )
			{
				self = selfLink.getHref().toURI();
			}
			else
			{
				final StringBuffer message = new StringBuffer();
				message.append( "No link to the resource (relation=\"self\") was specified." + Constants.NEW_LINE );
				message.append( "  Feed: " + feed.toString() );
				
				throw new IllegalArgumentException( message.toString() );
			}
		}
		catch( URISyntaxException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Could not parse the link to the resource (relation=\"self\") into a URI." + Constants.NEW_LINE );
			message.append( "  Feed: " + feed.toString() );
			
			throw new IllegalArgumentException( message.toString(), e );
		}

		// any response specific parsing that needs to be done in subclasses
		parse( feed );
	}
	
	/**
	 * Parses the {@link org.apache.abdera.model.Feed} into the appropriate fields of the {@link org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse}
	 * @param feed
	 */
	protected void parse( final Feed feed )
	{
		// purposefully empty (should be overridden to add additional parsing specified to a response)
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getId()
	 */
	@Override
	public URI getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return title;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getUpdated()
	 */
	@Override
	public Calendar getUpdated()
	{
		return updated;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getResourceUri()
	 */
	@Override
	public URI getResourceUri()
	{
		return self;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getFeed()
	 */
	@Override
	public Feed getFeed()
	{
		return feed;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "ID: " + id.toString() + Constants.NEW_LINE );
		buffer.append( "Title: " + title.toString() + Constants.NEW_LINE );
		buffer.append( "URI: " + self.toString() + Constants.NEW_LINE );
		buffer.append( "Updated: " + new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS" ).format( updated.getTime() ) + Constants.NEW_LINE );
		buffer.append( "Feed: " + feed.toString() );
		return buffer.toString();
	}
}
