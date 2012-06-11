package org.microtitan.diffusive.diffuser;

import java.lang.reflect.Method;

import org.microtitan.diffusive.diffuser.serializer.Serializer;

/**
 * The {@link Diffuser} is responsible for running the specified object's method. For local {@link Diffuser}s
 * this means that the {@link Diffuser} will run the method in the local process. Remote {@link Diffuser}s are
 * responsible for communicating with {@link Diffuser}s in other processes or across the network, providing them
 * with the information needed to run the specified method, and then gathering the results of the method 
 * invocation.
 * 
 * Remote {@link Diffuser} usually work with {@link Serializer} objects that take the objects into and out of
 * a serialized/persisted form so that they (their entire object tree) can be transported across the network
 * to a {@link Diffuser} that can reconstruct the object and execute the method.
 * 
 * @author Robert Philipp
 */
public interface Diffuser {

	/**
	 * Runs the specified no-arg method on the specified object.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String, Object)
	 * @see #runObject(Object, String, Object...)
	 */
	Object runObject( final Object object, final String methodName );
	
	/**
	 * Runs the specified single-argument method on the specified object.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @param argument The argument to be passed to the method. Typically the implementing class will
	 * use reflection to determine the {@link Method}, and so the parameter type will be used as part
	 * of the signature. 
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String)
	 * @see #runObject(Object, String, Object...)
	 */
	Object runObject( final Object object, final String methodName, final Object argument );
	
	/**
	 * Runs the specified method on the specified object.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @param arguments The arguments to be passed to the method. Typically the implementing class will
	 * use reflection to determine the {@link Method}, and so the parameter types will be used as part
	 * of the signature. 
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String)
	 * @see #runObject(Object, String, Object)
	 */
	Object runObject( final Object object, final String methodName, final Object...arguments );
}
