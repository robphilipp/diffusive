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
package org.microtitan.diffusive.diffuser;

/**
 * Abstract diffuser that implements the convenience methods, {@link #runObject(double, Class, Object, String)}
 * and {@link #runObject(double, Class, Object, String, Class[], Object...)} so that the sub classes only need to implement
 * {@link #runObject(double, Class, Object, String, Class[], Object...)}.
 */
public abstract class AbstractDiffuser implements Diffuser {

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName )
	{
		return runObject( load, returnType, object, methodName, (Class< ? >[])null, (Object[])null );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String, java.lang.Class, java.lang.Object)
	 */
	@Override
	public Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName, final Class< ? > argType, final Object argument )
	{
		return runObject( load, returnType, object, methodName, new Class< ? >[] { argType }, new Object[] { argument } );
	}
}
