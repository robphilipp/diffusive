package org.microtitan.diffusive.diffuser.restful.response;

import org.apache.abdera.model.Feed;

public abstract class AbstractDiffuserResponse implements DiffuserResponse {

	private final Feed feed;
	
	/**
	 * Constructor of the {@link DiffuserResponse} that calls the {@link #parse(Feed)} method
	 * on the subclass. The subclass' {@link #parse(Feed)} method is responsible for setting the
	 * appropriate fields in the response
	 * @param feed The Atom feed
	 */
	public AbstractDiffuserResponse( final Feed feed )
	{
		this.feed = feed;
		
		parse( feed );
	}
	
	/**
	 * Parses the {@link Feed} into the appropriate fields of the {@link DiffuserResponse}
	 * @param feed
	 */
	protected abstract void parse( final Feed feed );
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.DiffuserResponse#getFeed()
	 */
	@Override
	public Feed getFeed()
	{
		return feed;
	}

}
