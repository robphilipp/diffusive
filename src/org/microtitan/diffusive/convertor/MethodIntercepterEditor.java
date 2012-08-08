package org.microtitan.diffusive.convertor;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.Diffusive;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;

/**
 * Method intercepter editor is responsible for writing the method-call replacement code that is
 * used to replace and method calls to methods annotated with {@link Diffusive}. The replacement
 * code uses a {@link Diffuser} to run the method instead of it running directly. The {@link Diffuser}
 * is taken from the {@link KeyedDiffuserRepository}, and then its {@link Diffuser#runObject(double, Class, Object, String, Object...)}
 * method (or a derivative) is called with the appropriate information.
 * 
 * @author Robert Philipp
 */
public class MethodIntercepterEditor extends ExprEditor {
	
	private static final Logger LOGGER = Logger.getLogger( MethodIntercepterEditor.class );

	/**
	 * Default no arg constructor
	 */
	public MethodIntercepterEditor()
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see javassist.expr.ExprEditor#edit(javassist.expr.MethodCall)
	 */
	@Override
	public void edit( final MethodCall methodCall )
	{
		final String className = methodCall.getClassName();
		final String methodName = methodCall.getMethodName();
		final StringBuffer code = new StringBuffer();
		try
		{
			if( methodCall.getMethod().getAnnotation( Diffusive.class ) != null )
			{
				// write the code to replace the method call with a Diffusive call
//				final StringBuffer code = new StringBuffer( "{\n" );

//				code.append( "{\n" );
//				
//				code.append( "  org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( $class );\n" );
//				code.append( "  if( logger.isDebugEnabled() )\n" );
//				code.append( "  {\n " );
//				code.append( "    java.lang.StringBuffer message = new java.lang.StringBuffer();\n" );
//				code.append( "    message.append( \"(diffused): " + className + "." + methodName + "\" );\n" );
//				code.append( "    message.append( \"  Object: \" + $0.getClass().getName() );\n" );
//				int i = 1;
//				for( CtClass arg : methodCall.getMethod().getParameterTypes() )
//				{
//					code.append( "    message.append( \"  Method Param: value=\" + $" + i + " + \"; type=" + arg.getName() + "\" );\n" );
//					++i;
//				}
//				code.append( "    message.append( \"  Return: \" + $type.getName() );\n" );
//				code.append( "    logger.debug( message.toString() );\n" );
//				code.append( "  }\n" );

				// TODO replace this with a logger, which will require adding a logger field
				code.append( "    System.out.println( \"(diffused): " + className + "." + methodName + "\" );\n" );
				code.append( "    System.out.println( \"  Class Loader Name: \" + $0.getClass().getClassLoader().getClass().getName() );\n" );
				code.append( "    System.out.println( \"  Class Loader Instance: \" + $0.getClass().getClassLoader().toString() );\n" );
				code.append( "    System.out.println( \"  Object: \" + $0.getClass().getName() );\n" );
				int i = 1;
				for( CtClass arg : methodCall.getMethod().getParameterTypes() )
				{
					code.append( "    System.out.println( \"  Method Param: value=\" + $" + i + " + \"; type=" + arg.getName() + "\" );\n" );
					++i;
				}
				code.append( "    System.out.println( \"  Return: \" + $type.getName() );\n" );

				// the actual Diffusive call
				code.append( "    $_ = ($r)org.microtitan.diffusive.diffuser.KeyedDiffuserRepository.getInstance().getDiffuser().runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $$ );" );
				
//				code.append( "\n}" );
				
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
					message.append( "  Line Number: " + methodCall.getLineNumber() + Constants.NEW_LINE );
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
			message.append( "  Line Number: " + methodCall.getLineNumber() + Constants.NEW_LINE );
			message.append( "  Replacement Code: " + Constants.NEW_LINE );
			message.append( code.toString() );
			LOGGER.debug( message.toString(), exception );
			
			throw new IllegalArgumentException( message.toString(), exception );
		}
	}
}
