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
package org.microtitan.diffusive.diffuser.restful.atom;

import java.net.URI;
import java.util.Date;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Text;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;

public class Atom {

	// names for generating the Atom feeds
	public static final String VERSION = "0.1";
	public static final String GENERATOR_NAME = RestfulDiffuser.class.getName();

	/**
	 * @return a newly created feed
	 */
	public static Feed createFeed()
	{
		return AbderaFactory.getInstance().newFeed();
	}

	/**
	 * Creates a new feed whose ID is the specified resource URI, the title is a text title
	 * specified by the title, and the specified published date, and has a link to the
	 * resource with a relationship of {@link #LINK_RELATION_SELF} = {@value #LINK_RELATION_SELF}
	 * @param resourceUri The URI of the resource used as the ID and is provided in the link
	 * @param id The title of the feed
	 * @param created The date the feed was published (created)
	 * @return the newly created feed
	 */
	public static Feed createFeed( final URI resourceUri, final String id, final Date created )
	{
		return createFeed( resourceUri, id, Text.Type.TEXT, created, null );
	}

	public static Feed createFeed( final URI resourceUri, final String id, final Date created, final URI generatorUri )
	{
		return createFeed( resourceUri, id, Text.Type.TEXT, created, generatorUri );
	}
	
	public static Feed createFeed( final URI resourceUri, final String id, final Text.Type titleType, final Date created )
	{
		return createFeed( resourceUri, id, titleType, created, null );
	}

	public static Feed createFeed( final URI resourceUri, final String id, final Text.Type titleType, final Date created, final URI generatorUri )
	{
		final String uri = resourceUri.toString();
		
		final Feed feed = createFeed();
//		feed.setId( uri );
		feed.setId( id );
		feed.setTitle( id, titleType );
		feed.setUpdated( created );
		feed.addLink( uri, Link.REL_SELF );
		if( generatorUri != null )
		{
			feed.setGenerator( generatorUri.toString(), VERSION, GENERATOR_NAME );
		}
		return feed;
	}
	
	public static Entry createEntry()
	{
		return AbderaFactory.getInstance().newEntry();
	}
	
	public static Entry createEntry( final URI resourceUri, final String id, final Date created )
	{
		return createEntry( resourceUri, id, Text.Type.TEXT, created );
	}
	
	public static Entry createEntry( final URI resourceUri, final String id, final Text.Type titleType, final Date created )
	{
		final String uri = resourceUri.toString();
		
		final Entry entry = createEntry();
//		entry.setId( uri );
		entry.setId( id );
		entry.setTitle( id, titleType );
		entry.setUpdated( created );
		entry.setPublished( created );
		entry.addLink( uri, Link.REL_SELF );
		
		return entry;
	}
}
