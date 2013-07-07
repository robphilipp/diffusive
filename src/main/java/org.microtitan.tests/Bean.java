/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.microtitan.tests;

import java.io.Serializable;

import org.microtitan.diffusive.annotations.Diffusive;


public class Bean implements Serializable {
	
	private String a;
	private String b;
	
	public Bean( final String a, final String b )
	{
		this.a = a;
		this.b = b;
	}
	
	public Bean()
	{
		a = "null a";
		b = "null b";
	}
	
	/**
	 * @return the a
	 */
	@Diffusive
	public String getA()
	{
		long j = 0;
		for( long i = 0; i < 50; ++i )
		{
			for( long k = 0; k < 99_999_999; ++k )
			{
				j += i;
			}
			System.out.println( i );
		}
		System.out.println( "Done loooping" );
		return new Double( j ).toString();
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


	public String toString()
	{
		return "(" + a + ", " + b + ")";
	}
}
