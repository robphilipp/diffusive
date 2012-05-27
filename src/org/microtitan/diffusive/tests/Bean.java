package org.microtitan.diffusive.tests;

import org.microtitan.diffusive.annotations.Diffusive;


public class Bean {
	
	private String a;
	private String b;
	
	public Bean( final String a, final String b )
	{
		this.a = a;
		this.b = b;
	}
	
	/**
	 * @return the a
	 */
	@Diffusive
	public String getA()
	{
		return a;
	}
	
	/**
	 * @param a the a to set
	 */
	@Diffusive
	public void setA( final String a )
	{
		this.a = a;
		System.out.println( "Set A to: " + a );
	}
	/**
	 * @return the b
	 */
	public String getB()
	{
		return b;
	}
	/**
	 * @param b the b to set
	 */
	public void setB( final String b )
	{
		this.b = b;
	}

	
}
