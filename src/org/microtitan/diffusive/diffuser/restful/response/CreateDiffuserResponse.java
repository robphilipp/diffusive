package org.microtitan.diffusive.diffuser.restful.response;

import org.apache.abdera.model.Feed;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;

/**
 * The response when creating a new {@link RestfulDiffuser}.
 *  
 * @author Robert Philipp
 */
public class CreateDiffuserResponse extends AbstractDiffuserResponse {

	public CreateDiffuserResponse( final Feed feed )
	{
		super( feed );
	}
}
