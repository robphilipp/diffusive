package org.microtitan.diffusive.diffuser.restful.client;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.sun.jersey.api.client.Client;

public class RestfulClientFactory {

	private static Client instance = null;
	
	private RestfulClientFactory() {}
	
	public static Client getInstance()
	{
		synchronized( RestfulClientFactory.class )
		{
			if( instance == null )
			{
				// lazily create abdera
				instance = Client.create();
			}

			return instance;
		}
	}
	
	public static void main( String[] args ) throws IOException
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
	}
}
