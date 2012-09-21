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
import java.util.Calendar;

import org.apache.abdera.model.Feed;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;

/**
 * Essentially wraps the {@link Feed} that is returned as a response from the {@link RestfulDiffuserManagerResource}
 * into a Java class that can be access in a traditional object-oriented manner.
 *  
 * @author Robert Philipp
 */
public interface DiffuserResponse {

	/**
	 * @return the ID that is the unique URI of this object
	 */
	URI getId();

	/**
	 * @return the feed's title
	 */
	String getTitle();

	/**
	 * @return the date-time when this feed was created/updated
	 */
	Calendar getUpdated();
	
	/**
	 * @return The URI of the resource (the "self" relationship)
	 */
	URI getResourceUri();

	/**
	 * @return The Atom feed wrapped by the response
	 */
	Feed getFeed();
}
