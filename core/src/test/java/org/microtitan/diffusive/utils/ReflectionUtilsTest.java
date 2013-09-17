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
package org.microtitan.diffusive.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ReflectionUtilsTest {

	@Before
	public void setUp() throws Exception
	{
	}

	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testGetClazz() throws Exception
	{
		org.junit.Assert.assertEquals( "String", ReflectionUtils.getClazz( String.class.getName() ), String.class );
		org.junit.Assert.assertEquals( "Integer", ReflectionUtils.getClazz( Integer.class.getName() ), Integer.class );
		org.junit.Assert.assertEquals( "Double", ReflectionUtils.getClazz( Double.class.getName() ), Double.class );
		org.junit.Assert.assertEquals( "Boolean", ReflectionUtils.getClazz( Boolean.class.getName() ), Boolean.class );

		org.junit.Assert.assertEquals( "int", ReflectionUtils.getClazz( int.class.getName() ), int.class );
		org.junit.Assert.assertEquals( "long", ReflectionUtils.getClazz( long.class.getName() ), long.class );
		org.junit.Assert.assertEquals( "short", ReflectionUtils.getClazz( short.class.getName() ), short.class );
		org.junit.Assert.assertEquals( "double", ReflectionUtils.getClazz( double.class.getName() ), double.class );
		org.junit.Assert.assertEquals( "float", ReflectionUtils.getClazz( float.class.getName() ), float.class );
		org.junit.Assert.assertEquals( "boolean", ReflectionUtils.getClazz( boolean.class.getName() ), boolean.class );
		org.junit.Assert.assertEquals( "byte", ReflectionUtils.getClazz( byte.class.getName() ), byte.class );
		org.junit.Assert.assertEquals( "char", ReflectionUtils.getClazz( char.class.getName() ), char.class );
		org.junit.Assert.assertEquals( "void", ReflectionUtils.getClazz( void.class.getName() ), void.class );

		org.junit.Assert.assertEquals( "int[]", ReflectionUtils.getClazz( int[].class.getName() ), int[].class );
		org.junit.Assert.assertEquals( "double[]", ReflectionUtils.getClazz( double[].class.getName() ), double[].class );
		org.junit.Assert.assertEquals( "boolean[]", ReflectionUtils.getClazz( boolean[].class.getName() ), boolean[].class );
	}

	@Test
	public void testWrapPrimitive() throws Exception
	{
		org.junit.Assert.assertEquals( "int", ReflectionUtils.wrapPrimitive( int.class ), Integer.class );
		org.junit.Assert.assertEquals( "long", ReflectionUtils.wrapPrimitive( long.class ), Long.class );
		org.junit.Assert.assertEquals( "short", ReflectionUtils.wrapPrimitive( short.class ), Short.class );
		org.junit.Assert.assertEquals( "double", ReflectionUtils.wrapPrimitive( double.class ), Double.class );
		org.junit.Assert.assertEquals( "float", ReflectionUtils.wrapPrimitive( float.class ), Float.class );
		org.junit.Assert.assertEquals( "boolean", ReflectionUtils.wrapPrimitive( boolean.class ), Boolean.class );
		org.junit.Assert.assertEquals( "byte", ReflectionUtils.wrapPrimitive( byte.class ), Byte.class );
		org.junit.Assert.assertEquals( "char", ReflectionUtils.wrapPrimitive( char.class ), Character.class );
		org.junit.Assert.assertEquals( "void", ReflectionUtils.wrapPrimitive( void.class ), Void.class );
	}

	@Test
	public void testUnwrapPrimitive() throws Exception
	{
		org.junit.Assert.assertEquals( "int", ReflectionUtils.unwrapPrimitive( Integer.class ), int.class );
		org.junit.Assert.assertEquals( "long", ReflectionUtils.unwrapPrimitive( Long.class ), long.class );
		org.junit.Assert.assertEquals( "short", ReflectionUtils.unwrapPrimitive( Short.class ), short.class );
		org.junit.Assert.assertEquals( "double", ReflectionUtils.unwrapPrimitive( Double.class ), double.class );
		org.junit.Assert.assertEquals( "float", ReflectionUtils.unwrapPrimitive( Float.class ), float.class );
		org.junit.Assert.assertEquals( "boolean", ReflectionUtils.unwrapPrimitive( Boolean.class ), boolean.class );
		org.junit.Assert.assertEquals( "byte", ReflectionUtils.unwrapPrimitive( Byte.class ), byte.class );
		org.junit.Assert.assertEquals( "char", ReflectionUtils.unwrapPrimitive( Character.class ), char.class );
		org.junit.Assert.assertEquals( "void", ReflectionUtils.unwrapPrimitive( Void.class ), void.class );
	}

	@Test
	public void testCast() throws Exception
	{
		org.junit.Assert.assertEquals( "int", ReflectionUtils.cast( int.class, new Integer( 10 ) ), new Integer( 10 ) );
		org.junit.Assert.assertNotEquals( "int", ReflectionUtils.cast( int.class, new Integer( 10 ) ), new Double( 10 ) );
	}

	@Test
	public void testGetPrimitive() throws Exception
	{

	}
}
