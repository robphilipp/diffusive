package org.microtitan.diffusive.diffuser.restful.response;

import org.apache.abdera.model.Feed;

public class CreateDiffuserResponse extends AbstractDiffuserResponse {

	public CreateDiffuserResponse( final Feed feed )
	{
		super( feed );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.AbstractDiffuserResponse#parse(org.apache.abdera.model.Feed)
	 */
	@Override
	protected void parse( Feed feed )
	{
	}

}
