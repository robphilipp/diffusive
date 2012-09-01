package org.microtitan.diffusive.diffuser;

public abstract class AbstractDiffuser implements Diffuser {

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName )
	{
		return runObject( load, returnType, object, methodName, (Object[])null );
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public Object runObject( final double load, final Class< ? > returnType, final Object object, final String methodName, final Object argument )
	{
		return runObject( load, returnType, object, methodName, new Object[] { argument } );
	}
}
