package org.microtitan.diffusive.tests;




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

		final BeanTest test = new BeanTest();
		test.print();
		test.changeValues( "--new--" );
		test.print();
		test.concat();
	}
}
