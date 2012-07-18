package org.microtitan.diffusive.launcher.config;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulClassPathResource;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

public class RestfulDiffuserRepositoryConfig {
	
	// holds the list of client endpoints...for testing we use the same address as specified
	// in the RestfulDiffuserServer as the default server URI.
	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( RestfulDiffuserServer.DEFAULT_SERVER_URI );
//	public static final List< String > CLIENT_ENDPOINTS = Arrays.asList( "http://192.168.1.8:8182" );
	
	// holds the base URI of the class path that gets passed to the remote diffuser manager when
	// creating a diffuser. this allows the remote code to load classes from a remote server.
	public static final List< String > CLASSPATH_URI = Arrays.asList( "http://192.168.1.4:8182" );
	
	@DiffusiveConfiguration
	public static final void configure()
	{
		// load the diffuser repository
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
		final List< URI > clientEndpoints = createEndpointList();
		final List< URI > classPaths = createClassPathList();
		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints, classPaths );
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
	
	public static final List< URI > createClassPathList()
	{
		final List< URI > endpoints = new ArrayList<>();
		for( String client : CLASSPATH_URI )
		{
			endpoints.add( URI.create( client + RestfulClassPathResource.CLASSPATH_PATH ) );
		}
		return endpoints;
	}
	
	public static void main( String[] args ) throws Exception
	{
		for( Method m : RestfulDiffuserRepositoryConfig.class.getMethods() )
		{
			if( m.isAnnotationPresent( DiffusiveConfiguration.class ) )
			{
				System.out.println( "Found annotation on method: " + m.getName() );
			}
			else
			{
				System.out.println( "Method not annotaed with @" + DiffusiveConfiguration.class.getName() + ": " + m.getName() );
			}
		}
	}
}
