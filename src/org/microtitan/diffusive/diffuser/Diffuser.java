package org.microtitan.diffusive.diffuser;

import java.lang.reflect.Method;


public interface Diffuser {

	Object runObject( final Object object, final String methodName );
	
	Object runObject( final Object object, final String methodName, final Object argument );
	
	/**
	 * Runs the specified method on the specified object
	 * 
	 * @param object The object on which to make the method call given by the specified method name
	 * @param methodName The name of the method to call on the object
	 * @param arguments The arguments to be passed to the method. Typically the implementing class will
	 * use reflection to determine the {@link Method}, and so the parameter types will be used as part
	 * of the signature. 
	 * @return The result of the method on the specified object
	 */
	Object runObject( final Object object, final String methodName, final Object...arguments );
}
