package org.microtitan.diffusive.launcher.config;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

public class RestfulDiffuserRepositoryConfig {

	public static void configure()
	{
		// load the diffuser repository
		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
		final List< URI > clientEndpoints = Arrays.asList( URI.create( "http://localhost:8183" ) );
		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );
	}
}
