package org.microtitan.diffusive.utils;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 9/20/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectionUtilsTest {
	@Test
	public void testSizesMatch() throws Exception
	{
		final String[] array1 = new String[] { "1", "2", "3", "4" };
		final String[] array2 = new String[] { "1", "2", "3" };
		final Integer[] array3 = new Integer[] { 1, 2, 3, 4 };

		org.junit.Assert.assertEquals( "Equal size", CollectionUtils.sizesMatch( array1, array3 ), true );
		org.junit.Assert.assertEquals( "Unequal size", CollectionUtils.sizesMatch( array1, array2 ), false );
	}
}
