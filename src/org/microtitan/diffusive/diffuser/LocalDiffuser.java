package org.microtitan.diffusive.diffuser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LocalDiffuser implements Diffuser {

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

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object runObject( final Object object, final String methodName, final Object...arguments )
	{
		final Class< ? > clazz = object.getClass();
		
		Class< ? >[] params = null;
		if( arguments != null && arguments.length > 0 )
		{
			final List< Class< ? > > parameterTypes = new ArrayList<>();
			for( Object argument : arguments )
			{
				parameterTypes.add( argument.getClass() );
			}
			params = parameterTypes.toArray( new Class< ? >[ 0 ] );
		}
		Object returnResult = null;
		try
		{
			System.out.println( "**" + LocalDiffuser.class.getName() + ".runObject: " + object.getClass().getName() + "." + methodName );
			final Method method = clazz.getMethod( methodName, params );
			returnResult = method.invoke( object, arguments );
		}
		catch( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnResult;
	}
}
