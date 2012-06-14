package org.microtitan.diffusive.tests;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserApplication;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.serializer.ObjectSerializer;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;





public class BeanTest {

//	private static final Logger LOGGER = Logger.getLogger( BeanTest.class );
	
	private Bean bean;
	
	private BeanTest()
	{
		bean = new Bean( "--original--A", "--original--B" );
	}
	
	private void print()
	{
//		LOGGER.debug( " Bean value are " + bean.getA() + " and " + bean.getB() );
		System.out.println( " Bean value are " + bean.getA() + " and " + bean.getB() );
	}
	
	private void concat()
	{
//		LOGGER.debug( " Concatenated beans: " + bean.getA() + bean.getB() );
		System.out.println( " Concatenated beans: " + bean.getA() + bean.getB() );
	}
	
	private void changeValues( final String prefix )
	{
		bean.setA( prefix + "A" );
		bean.setB( prefix + "B" );
	}
	
	public static void main( String[] args )
	{
//		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		// TODO this should be in a config file that is inserted here from the launcher...
		// set the use of the RESTful diffuser
		final URI serverUri = URI.create( "http://localhost:8182/" );
//		final List< String > resourcePackages = Arrays.asList( RestfulDiffuser.class.getPackage().getName() );
//		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, resourcePackages );
		final RestfulDiffuserManagerResource resource = new RestfulDiffuserManagerResource();
		final RestfulDiffuserApplication application = new RestfulDiffuserApplication();
		application.addSingletonResource( resource );
		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, application );
		
//		final Serializer serializer = new ObjectSerializer();
//		final List< URI > clientEndpoints = Arrays.asList( URI.create( serverUri.toString() ) );
//		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
//		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );

		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
		final List< URI > clientEndpoints = Arrays.asList( URI.create( "http://localhost:8183" ) );
		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );


		final BeanTest test = new BeanTest();
		test.print();
		test.changeValues( "--new--" );
		test.print();
		test.concat();
	}
}
