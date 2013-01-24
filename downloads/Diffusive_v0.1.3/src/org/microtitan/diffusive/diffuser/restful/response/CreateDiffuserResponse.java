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
