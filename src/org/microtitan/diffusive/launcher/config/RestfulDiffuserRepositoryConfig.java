package org.microtitan.diffusive.launcher.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

public class RestfulDiffuserRepositoryConfig {
	
	// holds the list of client endpoints...for testing we use the same address as specified
	// in the RestfulDiffuserServer as the default server URI.
	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( RestfulDiffuserServer.DEFAULT_SERVER_URI );
	
	public static final void configure()
	{
		// load the diffuser repository
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
		final List< URI > clientEndpoints = createEndpointList();
		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );
	}
	
	public static final List< URI > createEndpointList()
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : CLIENT_ENDPOINTS )
		{
			endpoints.add( URI.create( client + RestfulDiffuserManagerResource.DIFFUSER_PATH ) );
		}
		return endpoints;
	}
}
