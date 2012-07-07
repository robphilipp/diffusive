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
	 * @param title The title of the feed
	 * @param created The date the feed was published (created)
	 * @return the newly created feed
	 */
	public static Feed createFeed( final URI resourceUri, final String title, final Date created )
	{
		return createFeed( resourceUri, title, Text.Type.TEXT, created, null );
	}

	public static Feed createFeed( final URI resourceUri, final String title, final Date created, final URI generatorUri )
	{
		return createFeed( resourceUri, title, Text.Type.TEXT, created, generatorUri );
	}
	
	public static Feed createFeed( final URI resourceUri, final String title, final Text.Type titleType, final Date created )
	{
		return createFeed( resourceUri, title, titleType, created, null );
	}

	public static Feed createFeed( final URI resourceUri, final String title, final Text.Type titleType, final Date created, final URI generatorUri )
	{
		final String uri = resourceUri.toString();
		
		final Feed feed = createFeed();
		feed.setId( uri );
		feed.setTitle( title, titleType );
		feed.setUpdated( created );
		feed.addLink( uri, Link.REL_SELF );
//		feed.addLink( generatorUri.toString(), Link.REL_SERVICE );
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
	
	public static Entry createEntry( final URI resourceUri, final String title, final Date created )
	{
		return createEntry( resourceUri, title, Text.Type.TEXT, created );
	}
	
	public static Entry createEntry( final URI resourceUri, final String title, final Text.Type titleType, final Date created )
	{
		final String uri = resourceUri.toString();
		
		final Entry entry = createEntry();
		entry.setId( uri );
		entry.setTitle( title, titleType );
		entry.setUpdated( created );
		entry.setPublished( created );
		entry.addLink( uri, Link.REL_SELF );
		
		return entry;
	}
}
