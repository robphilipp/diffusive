package org.microtitan.diffusive.diffuser;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

public class LocalDiffuser extends AbstractDiffuser {
	
	private static final Logger LOGGER = Logger.getLogger( LocalDiffuser.class );

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object runObject( final Object object, final String methodName, final Object...arguments )
	{
		final Class< ? > clazz = object.getClass();
		
		// grab the array of parameter types so that we know the signature of the method we want to call
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
		
		// attempt to call the method
		Object returnResult = null;
		try
		{
			// grab the method and invoke it, if the method doesn't exist, then an exception is thrown
			returnResult = clazz.getMethod( methodName, params ).invoke( object, arguments );

			if( LOGGER.isDebugEnabled() )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Diffused method call to:" + Constants.NEW_LINE );
				message.append( "  Diffuser: " + LocalDiffuser.class.getName() + Constants.NEW_LINE );
				message.append( "  Class of Object to Run: " + object.getClass().getName() + Constants.NEW_LINE );
				message.append( "  Name of Method to Run: " + methodName + Constants.NEW_LINE );
				message.append( "  Returned Result Object: " + returnResult.getClass().getName() + Constants.NEW_LINE );
				message.append( "  Returned Result Value: " + returnResult.toString() );
				LOGGER.debug( message.toString() );
				System.out.println( message.toString() );
			}
		}
		catch( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Diffuser is unable to invoke the method on the object:" + Constants.NEW_LINE );
			message.append( "  Diffuser: " + LocalDiffuser.class.getName() + Constants.NEW_LINE );
			message.append( "  Class of Object to Run: " + object.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Name of Method to Run: " + methodName );
			LOGGER.error( message.toString(), e );
			
			throw new IllegalArgumentException( message.toString(), e );
		}
		return returnResult;
	}
}
