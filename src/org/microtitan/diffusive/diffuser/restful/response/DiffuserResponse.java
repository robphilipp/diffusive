package org.microtitan.diffusive.diffuser.restful.response;

import org.apache.abdera.model.Feed;

public interface DiffuserResponse {

	/**
	 * @return The Atom feed wrapped by the response
	 */
	Feed getFeed();
}
