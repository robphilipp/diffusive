package org.microtitan.diffusive.convertor;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.Diffusive;

/**
 *  
 * @author Robert Philipp
 */
public class MethodInterceptorEditor extends ExprEditor {
	
	private static final Logger LOGGER = Logger.getLogger( MethodInterceptorEditor.class );

	/*
	 * (non-Javadoc)
	 * @see javassist.expr.ExprEditor#edit(javassist.expr.MethodCall)
	 */
	@Override
	public void edit( final MethodCall methodCall )
	{
		final String className = methodCall.getClassName();
		final String methodName = methodCall.getMethodName();
		try
		{
			if( methodCall.getMethod().getAnnotation( Diffusive.class ) != null )
			{
				// write the code to replace the method call with a Diffusive call
				final StringBuffer code = new StringBuffer( "{\n" );
				
				// TODO replace this with a logger, which will require adding a logger field
				code.append( "\tSystem.out.println( \"(diffused): " + className + "." + methodName + "\" );\n" );
				code.append( "\tSystem.out.println( \"  Object: \" + $0.getClass().getName() );\n" );
				code.append( "\tSystem.out.println( \"  A: \" + $0.getA() );\n" );
				int i = 1;
				for( CtClass arg : methodCall.getMethod().getParameterTypes() )
				{
					code.append( "\tSystem.out.println( \"  Method Param: value=\" + $" + i + " + \"; type=" + arg.getName() + "\" );\n" );
					++i;
				}
				code.append( "\tSystem.out.println( \"  Return: \" + $type.getName() );\n" );

				// the actual Diffusive call
				code.append( "\t$_ = ($r)org.microtitan.diffusive.diffuser.KeyedDiffuserRepository.getInstance().getDiffuser().runObject( $0, \"" + methodName + "\", $$ );" );
				
				code.append( "\n}" );
				
				// make the call to replace the code in the method call
				methodCall.replace( code.toString() );
				
				if( LOGGER.isDebugEnabled() )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Diffusive method intercepted and replaced:" + Constants.NEW_LINE );
					message.append( "  Class Name: " + className + Constants.NEW_LINE );
					message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
					message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
					message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
					message.append( "  Line Number: " + methodCall.getLineNumber() );
					message.append( "  Replacement Code: " + Constants.NEW_LINE );
					message.append( code.toString() );
					LOGGER.debug( message.toString() );
				}
			}
			else
			{
				if( LOGGER.isTraceEnabled() )
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Method is not a diffusive method:" + Constants.NEW_LINE );
					message.append( "  Class Name: " + className + Constants.NEW_LINE );
					message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
					message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
					message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
					message.append( "  Line Number: " + methodCall.getLineNumber() );
					LOGGER.trace( message.toString() );
				}
			}
		}
		catch( ClassNotFoundException exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The annotation class could not be found:" + Constants.NEW_LINE );
			message.append( "  Annotation Class Name: " + Diffusive.class.getName() + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
			message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
			message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
			message.append( "  Line Number: " + methodCall.getLineNumber() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
		catch( NotFoundException exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The method associated with the diffusive method call, or its parameter types, could not be found:" + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Method Name: " + className + Constants.NEW_LINE );
			message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
			message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
			message.append( "  Line Number: " + methodCall.getLineNumber() );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
		catch( CannotCompileException exception )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The source code that is to replace the byte code of the diffusive method call did not compile:" + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Method Name: " + className + Constants.NEW_LINE );
			message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
			message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
			message.append( "  Line Number: " + methodCall.getLineNumber() );
			message.append( "  Replacement Code: " + Constants.NEW_LINE );
			LOGGER.debug( message.toString() );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
	}
}
