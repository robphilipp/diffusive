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
package org.microtitan.diffusive.convertor;

import java.util.ArrayList;
import java.util.List;

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
import org.microtitan.diffusive.diffuser.restful.DiffuserSignature;
import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;

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

	private final DiffuserSignature diffuserId;
	private final boolean isUseSignature;
	
	/**
	 * Constructs the method intercepter. 
	 * 
	 * If a base signature is specified, then the, {@link #isUseSignature} is set to true, and the
	 * method call to the method is replaced by a diffuser that is attached to a specific 
	 * diffuser method signature. If not specified, then the diffuser is the default diffuser in 
	 * the {@link KeyedDiffuserRepository}.
	 * 
	 * <p>For the application-attached diffuser, we want to use the one and only, the default, 
	 * diffuser. However, for the diffusers attached to the restful diffuser manager resource, 
	 * there is one diffuser per diffuser method signature, and so we want to use the signature.
	 * 
	 * <p>Effectively, the rule-of-thumb is that for application-attached diffusers use the other constructor,
	 * the one with no arguments; and for diffusers managed by the {@link RestfulDiffuserManagerResource} set
	 * the base signature to the signature associated with the diffuser that uses this method intercepter
	 * to instrument method calls.
	 * 
	 * @param baseSignature The base signature is the signature associated with the specified diffuser.
	 * If the base signature is specified, and the method call is to a method that has the same signature,
	 * then the method call is not replaced by a call to the diffuser's runObject(...) method.
	 */
	public MethodIntercepterEditor( final String baseSignature )
	{
		if( DiffuserSignature.isValid( baseSignature ) )
		{
			diffuserId = DiffuserSignature.parse( baseSignature );
			isUseSignature = true;
		}
		else
		{
			diffuserId = null;
			isUseSignature = false;
		}
	}
	
	/**
	 * Default no arg constructor that sets the {@link #isUseSignature} to false.
	 * @see #MethodIntercepterEditor(boolean)
	 */
	public MethodIntercepterEditor()
	{
		this( null );
	}
	
	/**
	 * Returns true if the class name and method name correspond to the base method name (the
	 * method that is associated with the diffuser, and therefore shouldn't be instrumented)
	 * @param className The name of the class containing the method
	 * @param methodName The name of the method
	 * @return true if the call is in regards to the base method; false otherwise
	 */
	private boolean isBaseMethod( final String className, final String methodName )
	{
		boolean isBaseMethod = false;
		if( diffuserId != null )
		{
			isBaseMethod = diffuserId.getClassName().equals( className ) && diffuserId.getMethodName().equals( methodName );
		}
		return isBaseMethod;
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
			// if the method itself is annotated with @Diffusive, AND, the method making the call does not have the base 
			// signature, then we want to diffuse it further. recall that the base signature is the signature associated 
			// with the diffuser that is responsible for calling the method, and that means the method came from a remote 
			// address space, and shouldn't be diffused. however, if the signatures aren't equal, then it is valid to be 
			// diffused as a nested diffusion.
			if( methodCall.getMethod().getAnnotation( Diffusive.class ) != null && !isBaseMethod( className, methodName ) )
			{
				// write the code to replace the method call with a Diffusive call
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

				// make the appropriate call to the diffuser repository to get the diffuser: either use
				// the signature or use the default diffuser (recall that for the application-attached
				// diffuser, we want to use the one and only, the default, diffuser. however, for the
				// diffusers attached to the restful diffuser manager resource, there is one diffuser per
				// diffuser method signature, and so we want to use the signature).
				final String repoClassName = KeyedDiffuserRepository.class.getName();
				final String getInstance = "getInstance()";
				if( isUseSignature )
				{
					// get the parameter types
					final List< String > argumentTypes = new ArrayList<>();
					for( CtClass arg : methodCall.getMethod().getParameterTypes() )
					{
						argumentTypes.add( arg.getName() );
					}
					
					// grab the class name of the return type
					final String returnType = methodCall.getMethod().getReturnType().getName();
	
					// create the signature of the method call
					final String signature = DiffuserSignature.createId( returnType, className, methodName, argumentTypes );
					
					// spit out the signature and the base method associated with the diffuser
					code.append( "    System.out.println( \"  Diffused Signature: " + signature + "\" );\n" );
					code.append( "    System.out.println( \"  Base Signature Used in Repository: " + diffuserId.getId() + "\" );\n" );

					// make the call to grab the diffuser to ensure that it is actually returning a diffuser.
					// we do this because if the class loader for the diffusion is different from the class loader of the
					// restful diffuser resource manager, then it returns null, and we want to see this.
					final String getDiffuser = repoClassName + "." + getInstance + ".getDiffuser( \"" + diffuserId.getId() + "\" )";
					code.append( "    System.out.println( \"  Diffuser from Repository: \" + " + getDiffuser + " );\n" );
					
					// the actual Diffusive call
					final String diffusiveCall = getDiffuser + ".runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $sig, $args );";
					code.append( "    $_ = ($r)" + diffusiveCall );
				}
				else
				{
					// make the call to grab the diffuser to ensure that it is actually returning a diffuser.
					// we do this because if the class loader for the diffusion is different from the class loader of the
					// restful diffuser resource manager, then it returns null, and we want to see this.
					final String getDiffuser = repoClassName + "." + getInstance + ".getDiffuser()";
					code.append( "    System.out.println( \"  Diffuser from Repository: \" + " + getDiffuser + " );\n" );
					
					// the actual Diffusive call
					final String diffusiveCall = getDiffuser + ".runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $sig, $args );";
					code.append( "    $_ = ($r)" + diffusiveCall );
				}
				
				// make the call to replace the code in the method call
				methodCall.replace( code.toString() );

				if( LOGGER.isInfoEnabled() )
				{
					final String header = "Diffusive method intercepted and replaced.";
					final String message = createMessage( header, className, methodName, methodCall, code.toString() );
					LOGGER.info( message );
				}
			}
			else
			{
				if( LOGGER.isTraceEnabled() )
				{
					final String header = "Method is not a diffusive method.";
					final String message = createMessage( header, className, methodName, methodCall );
					LOGGER.trace( message );
				}
			}
		}
		catch( ClassNotFoundException exception )
		{
			final String header = "The annotation class could not be found.";
			final String message = createMessage( header, Diffusive.class, className, methodName, methodCall );
			LOGGER.error( message, exception );
			throw new IllegalArgumentException( message, exception );
		}
		catch( NotFoundException exception )
		{
			final String header = "The method associated with the diffusive method call, or its parameter types, could not be found.";
			final String message = createMessage( header, className, methodName, methodCall );
			LOGGER.error( message, exception );
			throw new IllegalArgumentException( message, exception );
		}
		catch( CannotCompileException exception )
		{
			final String header = "The source code that is to replace the byte code of the diffusive method call did not compile.";
			final String message = createMessage( header, className, methodName, methodCall, code.toString() );
			LOGGER.error( message, exception );
			throw new IllegalArgumentException( message, exception );
		}
	}
	
	/**
	 * Creates a message for the process of instrumenting the method call.
	 * @param header The explanation of the message
	 * @param className The name of the {@link Class} containing the method to which calls are instrumented 
	 * @param methodName The name of the method to be instrumented
	 * @param methodCall The javassist {@link MethodCall} containing information about the method call
	 * @return the message
	 */
	private static String createMessage( final String header, final String className, final String methodName, final MethodCall methodCall )
	{
		final StringBuffer message = new StringBuffer();
		message.append( header + Constants.NEW_LINE );
		message.append( "  Class Name: " + className + Constants.NEW_LINE );
		message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
		message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
		message.append( "  Line Number: " + methodCall.getLineNumber() );
		return message.toString();
	}
	
	/**
	 * Creates a message for the process of instrumenting the method call.
	 * @param header The explanation of the message
	 * @param annotation The annotation identifying the method as a diffusive method (should be {@link Diffusive}
	 * @param className The name of the {@link Class} containing the method to which calls are instrumented 
	 * @param methodName The name of the method to be instrumented
	 * @param methodCall The javassist {@link MethodCall} containing information about the method call
	 * @return the message
	 */
	private static String createMessage( final String header, final Class< ? > annotation, final String className, final String methodName, final MethodCall methodCall )
	{
		final StringBuffer message = new StringBuffer();
		message.append( header + Constants.NEW_LINE );
		message.append( "  Annotation Class Name: " + annotation.getName() + Constants.NEW_LINE );
		message.append( "  Class Name: " + className + Constants.NEW_LINE );
		message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
		message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
		message.append( "  Line Number: " + methodCall.getLineNumber() );
		return message.toString();
	}

	/**
	 * Creates a message for the process of instrumenting the method call.
	 * @param header The explanation of the message
	 * @param className The name of the {@link Class} containing the method to which calls are instrumented 
	 * @param methodName The name of the method to be instrumented
	 * @param methodCall The javassist {@link MethodCall} containing information about the method call
	 * @param code The code that will replace the method call to the diffusive method
	 * @return the message
	 */
	private static String createMessage( final String header, final String className, final String methodName, final MethodCall methodCall, final String code )
	{
		final StringBuffer message = new StringBuffer();
		message.append( header + Constants.NEW_LINE );
		message.append( "  Class Name: " + className + Constants.NEW_LINE );
		message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		message.append( "  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE );
		message.append( "  Source File: " + methodCall.getFileName() + Constants.NEW_LINE );
		message.append( "  Line Number: " + methodCall.getLineNumber() );
		message.append( "  Replacement Code: " + Constants.NEW_LINE + code );
		return message.toString();
	}
}
