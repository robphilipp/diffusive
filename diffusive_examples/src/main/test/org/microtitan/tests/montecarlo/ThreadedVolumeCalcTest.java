package org.microtitan.tests.montecarlo;

import org.junit.Test;
import org.microtitan.diffusive.classloaders.RestfulClassLoader;
import org.microtitan.diffusive.utils.ClassLoaderUtils;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ThreadedVolumeCalcTest
{
	@Test
	public void testLoadClassToByteArray() throws Exception
	{
		final String CLASS_URL = "file://diffusive_examples/build/classes/main/org/microtitan/tests/montecarlo/ThreadedVolumeCalc.class";
		final byte[] systemLoadedClass = ClassLoaderUtils.loadClassToByteArray( ThreadedVolumeCalc.class.getName() );

		final URL[] urls = { new URL( CLASS_URL ) };
		final URLClassLoader urlClassLoader = new URLClassLoader( urls, this.getClass().getClassLoader() );
		final byte[] urlLoadedClass = ClassLoaderUtils.loadClassToByteArray( ThreadedVolumeCalc.class.getName(), urlClassLoader );

		assertArrayEquals( systemLoadedClass, urlLoadedClass );

		System.out.println( "declared methods" );
		Arrays.asList( ThreadedVolumeCalc.class.getDeclaredMethods() ).forEach( method -> System.out.println( method.toString()) );
		System.out.println( "methods" );
		Arrays.asList( ThreadedVolumeCalc.class.getMethods() ).forEach( method -> System.out.println( method.toString()) );

		final List<URI> uris = Arrays.asList( new URI( CLASS_URL ) );
		final RestfulClassLoader restfulClassLoader = new RestfulClassLoader( uris, new URLClassLoader( urls, this.getClass().getClassLoader() ) );
		final Class< ? > tvc = restfulClassLoader.getClazz( ThreadedVolumeCalc.class.getName(), urlLoadedClass );
		System.out.println( "loaded methods" );
		Arrays.asList( tvc.getMethods() ).forEach( method -> System.out.println( method.toString()) );

	}
}