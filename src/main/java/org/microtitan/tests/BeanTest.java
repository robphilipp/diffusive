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
