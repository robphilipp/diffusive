package org.microtitan.diffusive.diffuser.restful.resources.cache;

/**
 * 
 * @author Robert Philipp
 */
public interface ResultCacheEntry< T > {

	/**
	 * @return the serializer type used to serialize and deserialize objects
	 */
	String getSerializerType();

	/**
	 * Blocks until the result is completed and then returns it
	 * @return the result object
	 */
	T getResult();

	/**
	 * @return true if the result is complete; false otherwise
	 */
	boolean isDone();

}