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

import java.io.IOException;
import java.util.Date;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class AbderaFactory {

	private static Abdera instance = null;
	
	private AbderaFactory() {}
	
	public static Abdera getInstance()
	{
		synchronized( AbderaFactory.class )
		{
			if( instance == null )
			{
				// lazily create abdera
				instance = Abdera.getInstance();
			}

			return instance;
		}
	}
	
	public static void main( String[] args ) throws IOException
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		// create the atom
		final Abdera abdera = AbderaFactory.getInstance();
		final Feed feed = abdera.newFeed();
		feed.setId( "tag:example.org,2007:/foo" );
		feed.setTitle( "Test Feed" );
		feed.setSubtitle( "Feed subtitle" );
		feed.setUpdated( new Date() );
		feed.addAuthor( "James Snell" );
		feed.addLink( "http://example.com" );
		feed.addLink( "http://example.com/foo", "self" );

		final Entry feedEntry = feed.addEntry();
		feedEntry.setId( "tag:example.org,2007:/foo/entries/1" );
		feedEntry.setTitle( "Entry title" );
		feedEntry.setSummaryAsHtml( "<p>This is the entry title</p>" );
		feedEntry.setUpdated( new Date() );
		feedEntry.setPublished( new Date() );
		feedEntry.addLink( "http://example.com/foo/entries/1" );
		
		System.out.println( abdera.getWriter().write( feed ) );
	}
}
