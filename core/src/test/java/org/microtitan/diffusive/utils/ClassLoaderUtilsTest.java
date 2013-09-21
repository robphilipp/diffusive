package org.microtitan.diffusive.utils;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 9/20/13
 * Time: 6:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassLoaderUtilsTest {

	@Test
	public void testLoadClassToByteArray() throws Exception
	{
		final byte[] clazzBytes = ClassLoaderUtils.loadClassToByteArray( String.class.getName() );
	}
}
