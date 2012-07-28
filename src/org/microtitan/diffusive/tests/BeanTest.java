package org.microtitan.diffusive.tests;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;





public class BeanTest implements Serializable {

//	private static final Logger LOGGER = Logger.getLogger( BeanTest.class );
	
	private Bean bean;
	
	public BeanTest()
	{
		bean = new Bean( "--original--A", "--original--B" );
	}
	
	public void print()
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
		Logger.getRootLogger().setLevel( Level.WARN );
				
		final BeanTest test = new BeanTest();
		test.print();
		test.changeValues( "--new--" );
		test.print();
		test.concat();
	}
}
