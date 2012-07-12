package org.microtitan.diffusive.tests;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;





public class BeanTest implements Serializable {

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
	
	public static void main( String[] args ) throws IOException
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		// TODO this should be in a config file that is inserted here from the launcher...
		// set the use of the RESTful diffuser server
//		final URI serverUri = URI.create( "http://localhost:8182/" );
////		final List< String > resourcePackages = Arrays.asList( RestfulDiffuser.class.getPackage().getName() );
////		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, resourcePackages );
//		final RestfulDiffuserManagerResource resource = new RestfulDiffuserManagerResource();
//		final RestfulDiffuserApplication application = new RestfulDiffuserApplication();
//		application.addSingletonResource( resource );
//		final RestfulDiffuserServer server = new RestfulDiffuserServer( serverUri, application );
		
//		final Serializer serializer = new ObjectSerializer();
//		final List< URI > clientEndpoints = Arrays.asList( URI.create( serverUri.toString() ) );
//		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
//		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );

		// TODO this should be in a config file that is inserted here from the launcher...
		// this code sets up the diffuser repository with the local instance of the restful diffuser 
		// set the code to use of RESTful diffuser
//		final Serializer serializer = SerializerFactory.getInstance().createSerializer( SerializerFactory.SerializerType.OBJECT.getName() );
//		final List< URI > clientEndpoints = Arrays.asList( URI.create( "http://localhost:8183" ) );
//		final Diffuser diffuser = new RestfulDiffuser( serializer, clientEndpoints );
//		KeyedDiffuserRepository.getInstance().setDiffuser( diffuser );

		
		final BeanTest test = new BeanTest();
		test.print();
		test.changeValues( "--new--" );
		test.print();
		test.concat();

//		System.out.println( String.format( "Jersy app start with WADL available at %sapplication.wadl\nTry out %shelloworld\nHit enter to stop it...", serverUri, serverUri ) );
//		System.in.read();
//		server.stop();
	}
}
