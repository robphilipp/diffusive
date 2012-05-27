package org.microtitan.diffusive.tests;

public class BeanTest {

	private Bean bean;
	
	private BeanTest()
	{
		bean = new Bean( "--original--A", "--original--B" );
	}
	
	private void print()
	{
		System.out.println( "Bean value are " + bean.getA() + " and " + bean.getB() );
	}
	
	private void concat()
	{
		System.out.println( "Concatenated beans: " + bean.getA() + bean.getB() );
	}
	
	private void changeValues( final String prefix )
	{
		bean.setA( prefix + "A" );
		bean.setB( prefix + "B" );
	}
	
	public static void main( String[] args )
	{
		final BeanTest test = new BeanTest();
		test.print();
		test.changeValues( "--new--" );
		test.print();
		test.concat();
	}
}
