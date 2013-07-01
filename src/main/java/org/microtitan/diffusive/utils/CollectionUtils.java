package org.microtitan.diffusive.utils;


public class CollectionUtils {

	/**
	 * Checks to see if the sizes of the list and array are the same. If both are null,
	 * or if one is null and the other empty, then returns true. And, of course if both
	 * have the same size.
	 * @param arrayOne The array
	 * @param arrayTwo The array
	 * @return true if both are null, or if one is null and the other empty, or both
	 * have the same size.
	 */
	public static boolean sizesMatch( final Object[] arrayOne, final Object[] arrayTwo )
	{
		boolean sizesMatch = false;
		// avoid null pointer exception (are both null)
		if( arrayOne == null && arrayTwo == null )
		{
			sizesMatch = true;
		}
		// one is null and the other has no elements
		else if( arrayOne == null )
		{
			if( arrayTwo.length == 0 )
			{
				sizesMatch = true;
			}
			else
			{
				sizesMatch = false;
			}
		}
		else if( arrayTwo == null )
		{
			if( arrayOne.length == 0 )
			{
				sizesMatch = true;
			}
			else
			{
				sizesMatch = false;
			}
		}
		// neither is null and they have the same size
		else if( arrayOne.length == arrayTwo.length )
		{
			sizesMatch = true;
		}
		return sizesMatch;
	}
}
