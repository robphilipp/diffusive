package org.microtitan.diffusive.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 9/20/13
 * Time: 7:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigUtilsTest {

	@Before
	public void init()
	{
		// set the logging level
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
	}

	@Test
	public void testCreateEndpointList() throws Exception
	{
		final List< String > endpoints = new ArrayList<>();
		endpoints.add( "http://localhost:80/this/is/a/path" );
		final List<URI> uri = ConfigUtils.createEndpointList( endpoints );

		final List< URI > referenceEndpoints = new ArrayList<>();
		referenceEndpoints.add( URI.create( "http://localhost:80/this/is/a/path" ) );

		org.junit.Assert.assertThat( "lists equal", uri, new Matcher<List<URI>>() {
			@Override
			public boolean matches( final Object item )
			{
				if( !( item instanceof List ) || ((List)item).size() != uri.size() )
				{
					return false;
				}
				for( int i = 0; i < uri.size(); ++i )
				{
					if( !uri.get( i ).toString().equals( ((List)item).get( i ).toString() ) )
					{
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeMismatch( final Object item, final Description mismatchDescription )
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}

			@Override
			public void _dont_implement_Matcher___instead_extend_BaseMatcher_()
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}

			@Override
			public void describeTo( Description description )
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		} );
	}

	@Test
	public void testValidateEndpoints() throws Exception
	{
		final List< String > endpoints = new ArrayList<>();
		endpoints.add( "http://localhost:80/this/is/a/path" );
		endpoints.add( "http://invalid:**/{}/..." );
		final List< String > uri = ConfigUtils.validateEndpoints( endpoints );

		final List< URI > referenceEndpoints = new ArrayList<>();
		referenceEndpoints.add( URI.create( "http://localhost:80/this/is/a/path" ) );
	}
}
