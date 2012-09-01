package org.microtitan.diffusive.diffuser;

import java.lang.reflect.Method;

import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoadCalc;

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
 * The {@code runObject(...)} methods defined in this interface are expected to handle local calls and remote
 * calls. In the case of local calls, the implementation of these methods are intended to diffuse the code
 * to a {@link Diffuser} running remotely. In the case of remote calls, the implementation of these methods
 * should either run the method locally and return the results, or diffuse the code to another remote
 * {@link Diffuser}, which in turn would do the same.
 * 
 * @see LocalDiffuser
 * @see RestfulDiffuser
 * 
 * @author Robert Philipp
 */
public interface Diffuser {

	/**
	 * Runs the specified no-arg method on the specified object.
	 * @param load The CPU load gives the load (under a measure specified by the implementation of the 
	 * {@link DiffuserLoadCalc}). The load is compared to a configured load threshold. If the load is
	 * less than the threshold, then the {@link Diffuser} ought to execute the task locally. On the other
	 * hand, if the load is larger than the threshold, then the {@link Diffuser} ought to forward the 
	 * task to a remote {@link Diffuser}. The remote {@link Diffuser} is selected based on a configured
	 * {@link DiffuserStrategy}, which returns the end-point of the remove {@link Diffuser} to which to
	 * forward the task.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String, Object)
	 * @see #runObject(Object, String, Object...)
	 */
	Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName );
	
	/**
	 * Runs the specified single-argument method on the specified object.
	 * @param load The CPU load gives the load (under a measure specified by the implementation of the 
	 * {@link DiffuserLoadCalc}). The load is compared to a configured load threshold. If the load is
	 * less than the threshold, then the {@link Diffuser} ought to execute the task locally. On the other
	 * hand, if the load is larger than the threshold, then the {@link Diffuser} ought to forward the 
	 * task to a remote {@link Diffuser}. The remote {@link Diffuser} is selected based on a configured
	 * {@link DiffuserStrategy}, which returns the end-point of the remove {@link Diffuser} to which to
	 * forward the task.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @param argument The argument to be passed to the method. Typically the implementing class will
	 * use reflection to determine the {@link Method}, and so the parameter type will be used as part
	 * of the signature. 
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String)
	 * @see #runObject(Object, String, Object...)
	 */
	Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName, final Object argument );
	
	/**
	 * Runs the specified method on the specified object.
	 * @param load The CPU load gives the load (under a measure specified by the implementation of the 
	 * {@link DiffuserLoadCalc}). The load is compared to a configured load threshold. If the load is
	 * less than the threshold, then the {@link Diffuser} ought to execute the task locally. On the other
	 * hand, if the load is larger than the threshold, then the {@link Diffuser} ought to forward the 
	 * task to a remote {@link Diffuser}. The remote {@link Diffuser} is selected based on a configured
	 * {@link DiffuserStrategy}, which returns the end-point of the remove {@link Diffuser} to which to
	 * forward the task.
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @param arguments The arguments to be passed to the method. Typically the implementing class will
	 * use reflection to determine the {@link Method}, and so the parameter types will be used as part
	 * of the signature. 
	 * @return The result of the method on the specified object
	 * @see #runObject(Object, String)
	 * @see #runObject(Object, String, Object)
	 */
	Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName, final Object...arguments );
}
