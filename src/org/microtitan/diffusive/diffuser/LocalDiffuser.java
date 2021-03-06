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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.utils.CollectionUtils;

/**
 * Executes the specified method on the specified object in the local process.
 * 
 * @author Robert Philipp
 */
public class LocalDiffuser extends AbstractDiffuser {
	
	private static final Logger LOGGER = Logger.getLogger( LocalDiffuser.class );

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(double, java.lang.Class, java.lang.Object, java.lang.String, java.lang.Class<?>[], java.lang.Object[])
	 */
	@Override
	public Object runObject( final double laod, final Class< ? > returnType, final Object object, final String methodName, final Class< ? >[] argTypes, final Object...arguments )
	{
		// check to make sure that if argTypes and arguments aren't both empty or null, that they 
		// have the same number of elements.
		if( !CollectionUtils.sizesMatch( argTypes, arguments ) )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The number of arguments and argument types for the method do not match." + Constants.NEW_LINE );
			message.append( Constants.NEW_LINE );
			message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
			message.append( "  Arguments: " );
			if( arguments.length > 0 )
			{
				for( int i = 0; i < arguments.length; ++i )
				{
					message.append( Constants.NEW_LINE + "    " + arguments[ i ].getClass().getName() );
					if( argTypes[ i ].isPrimitive() )
					{
						message.append( " (primitive)" );
					}
				}
			}
			else
			{
				message.append( "[none]" );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		final Class< ? > clazz = object.getClass();
		
		// attempt to call the method
		Object returnResult = null;
		Object returnValue = null;
		try
		{
			// grab the method and invoke it, if the method doesn't exist, then an exception is thrown
			returnValue = clazz.getMethod( methodName, argTypes ).invoke( object, arguments );
			if( returnType.isPrimitive() )
			{
				returnResult = returnValue;
			}
			else
			{
				// ensure that the return type and the return value are of the same type
				returnResult = returnType.cast( returnValue );
			}

			if( LOGGER.isDebugEnabled() )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Diffused method call to:" + Constants.NEW_LINE );
				message.append( "  Diffuser: " + LocalDiffuser.class.getName() + Constants.NEW_LINE );
				message.append( "  Class of Object to Run: " + object.getClass().getName() + Constants.NEW_LINE );
				message.append( "  Name of Method to Run: " + methodName + Constants.NEW_LINE );
				if( returnResult == null )
				{
					message.append( "  Returned Result Object: [null]" + Constants.NEW_LINE );
				}
				else
				{
					message.append( "  Returned Result Object: " + returnResult.getClass().getName() + Constants.NEW_LINE );
					message.append( "  Returned Result Value: " + returnResult.toString() );
				}
				LOGGER.debug( message.toString() );
				System.out.println( message.toString() );
			}
		}
		catch( ClassCastException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Return type from the method and the intended result type do not match." + Constants.NEW_LINE );
			message.append( "  Expected Return Type: " + returnType.getName() + Constants.NEW_LINE );
			message.append( "  Actual Return Type: " + returnValue.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Expected Return Type Class Loader: " + returnType.getClassLoader() + Constants.NEW_LINE );
			message.append( "  Actual Return Type Class Loader: " + returnValue.getClass().getClassLoader() );
			LOGGER.error( message.toString(), e );
			
			throw new IllegalStateException( message.toString(), e );
		}
		catch( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Diffuser is unable to invoke the method on the object:" + Constants.NEW_LINE );
			message.append( "  Diffuser: " + LocalDiffuser.class.getName() + Constants.NEW_LINE );
			message.append( "  Class of Object to Run: " + object.getClass().getName() + Constants.NEW_LINE );
			message.append( "  Name of Method to Run: " + methodName + Constants.NEW_LINE );
			message.append( "  Method Arguments: " );
			if( argTypes != null )
			{
				for( Class< ? > param : argTypes )
				{
					message.append( Constants.NEW_LINE + "    " + param.getName() );
				}
			}
			else
			{
				message.append( "[none]" );
			}
			message.append( Constants.NEW_LINE + "  Return Type: " );
			if( returnType != null )
			{
				message.append( returnType.getName() );
			}
			else
			{
				message.append( "void" );
			}
			message.append( Constants.NEW_LINE + "  Containing Object: " + object.toString() );
			LOGGER.error( message.toString(), e );
			
			throw new IllegalArgumentException( message.toString(), e );
		}
		return returnResult;
	}
}
