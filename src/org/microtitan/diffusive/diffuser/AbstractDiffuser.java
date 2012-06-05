package org.microtitan.diffusive.diffuser;

public abstract class AbstractDiffuser implements Diffuser {

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object runObject( final Object object, final String methodName )
	{
		return runObject( object, methodName, (Object[])null );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@Override
	public Object runObject( final Object object, final String methodName, final Object argument )
	{
		return runObject( object, methodName, new Object[] { argument } );
	}
}
