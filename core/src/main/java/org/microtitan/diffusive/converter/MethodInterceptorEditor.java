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
package org.microtitan.diffusive.converter;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.annotations.Diffusive;
import org.microtitan.diffusive.diffuser.Diffuser;
import org.microtitan.diffusive.diffuser.DiffuserSignature;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Method interceptor editor is responsible for writing the method-call replacement code that is
 * used to replace and method calls to methods annotated with {@link Diffusive}. The replacement
 * code uses a {@link Diffuser} to run the method instead of it running directly. The {@link Diffuser}
 * is taken from the {@link KeyedDiffuserRepository}, and then its {@link Diffuser#runObject(double, Class, Object, String, Class, Object)}
 * method (or a derivative) is called with the appropriate information.
 * 
 * @author Robert Philipp
 */
public class MethodInterceptorEditor extends ExprEditor {
	
	private static final Logger LOGGER = Logger.getLogger( MethodInterceptorEditor.class );

	private final DiffuserSignature diffuserId;
	private final boolean isUseSignature;
	
	/**
	 * Constructs the method interceptor.
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
	 * <p>Effectively, the rule-of-thumb is that application-attached diffusers use the other constructor,
	 * the one with no arguments; and for diffusers managed by the by resources or otherwise, set
	 * the base signature to the signature associated with the diffuser that uses this method interceptor
	 * to instrument method calls.
	 * 
	 * @param baseSignature The base signature is the signature associated with the specified diffuser.
	 * If the base signature is specified, and the method call is to a method that has the same signature,
	 * then the method call is not replaced by a call to the diffuser's runObject(...) method.
	 */
	public MethodInterceptorEditor(final String baseSignature)
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
	 * @see #MethodInterceptorEditor(String)
	 */
	public MethodInterceptorEditor()
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

	/**
	 * (non-Javadoc)
	 * @see javassist.expr.ExprEditor#edit(javassist.expr.MethodCall)
	 */
	@Override
	public void edit( final MethodCall methodCall )
	{
		// grab the class and method names for the called method
		final String className = methodCall.getClassName();
		final String methodName = methodCall.getMethodName();

		// grab the method from the method call. if the method doesn't exist, then
		// it isn't a diffused method, so we leave it unchanged
		CtMethod method;
		try
		{
			method = getMethod( methodCall );

		}
		catch( NotFoundException exception )
		{
			final String header = "The method associated with the diffusive method call, or its parameter types, could not be found.";
			final String message = createMessage( header, className, methodName, methodCall );
			LOGGER.warn( message );
			return;
		}

		// create the code that replaces the call the the method.
		final StringBuilder code = new StringBuilder();
		try
		{
			// if the method itself is annotated with @Diffusive, AND, the method making the call does not have the base
			// signature, then we want to diffuse it further. recall that the base signature is the signature associated 
			// with the diffuser that is responsible for calling the method, and that means the method came from a remote 
			// address space, and shouldn't be diffused. however, if the signatures aren't equal, then it is valid to be 
			// diffused as a nested diffusion.
			if( method.getAnnotation( Diffusive.class ) != null && !isBaseMethod( className, methodName ) )
			{
				// write the code to replace the method call with a Diffusive call
				// TODO replace this with a logger, which will require adding a logger field
				code.append( "    System.out.println( \"(diffused): " ).append( className ).append( "." ).append( methodName ).append( "\" );\n" );
				code.append( "    System.out.println( \"  Class Loader Name: " ).append( "\" + $0.getClass().getClassLoader().getClass().getName() );\n" );
				code.append( "    System.out.println( \"  Class Loader Instance: " ).append( "\" + $0.getClass().getClassLoader().toString() );\n" );
				code.append( "    System.out.println( \"  Object: " ).append( "\" + $0.getClass().getName() );\n" );

				// grab the parameter types
				final CtClass[] parametertypes = getParameterTypes(methodCall);

				int i = 1;
				for( CtClass arg : parametertypes )
				{
                    code.append("    System.out.println( \"  Method Param: value=\" + $" ).append( i ).append(" + \" ; type=").append( arg.getName() ).append( "\" );\n" );
					++i;
				}
				code.append( "    System.out.println( \"  Return: " ).append( "\" + $type.getName() );\n" );

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
					for( CtClass arg : parametertypes )
					{
						argumentTypes.add( arg.getName() );
					}
					
					// grab the class name of the return type
					final String returnType = getReturnType( methodCall ).getName();
	
					// create the signature of the method call
					final String signature = DiffuserSignature.createId( returnType, className, methodName, argumentTypes );
					
					// spit out the signature and the base method associated with the diffuser
					code.append( "    System.out.println( \"  Diffused Signature: " ).append( signature ).append( "\" );\n" );
					code.append( "    System.out.println( \"  Base Signature Used in Repository: " ).append( diffuserId.getId() ).append( "\" );\n" );

					// make the call to grab the diffuser to ensure that it is actually returning a diffuser.
					// we do this because if the class loader for the diffusion is different from the class loader of the
					// restful diffuser resource manager, then it returns null, and we want to see this.
					final String getDiffuser = repoClassName + "." + getInstance + ".getDiffuser( \"" + diffuserId.getId() + "\" )";
					code.append( "    System.out.println( \"  Diffuser from Repository: \" + " ).append (getDiffuser ).append (" );\n" );
					
					// the actual Diffusive call
					final String diffusiveCall = getDiffuser + ".runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $sig, $args );";
					code.append( "    $_ = ($r)" ).append( diffusiveCall );
				}
				else
				{
					// make the call to grab the diffuser to ensure that it is actually returning a diffuser.
					// we do this because if the class loader for the diffusion is different from the class loader of the
					// restful diffuser resource manager, then it returns null, and we want to see this.
					final String getDiffuser = repoClassName + "." + getInstance + ".getDiffuser()";
					code.append( "    System.out.println( \"  Diffuser from Repository: \" + " ).append( getDiffuser ).append (" );\n" );
					
					// the actual Diffusive call
					final String diffusiveCall = getDiffuser + ".runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $sig, $args );";
					code.append( "    $_ = ($r)" ).append( diffusiveCall );
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
	 * Returns the {@link javassist.CtMethod} associated with the method call. Throws an
	 * {@link java.lang.IllegalArgumentException} if the method cannot be found.
	 * @param methodCall The {@link javassist.expr.MethodCall}
	 * @return The {@link javassist.CtMethod} associated with the method call.
	 */
	private CtMethod getMethod( final MethodCall methodCall ) throws NotFoundException
	{
		CtMethod method;
		try
		{
			method = methodCall.getMethod();
		}
		catch( NotFoundException exception )
		{
			final String className = methodCall.getClassName();
			final String methodName = methodCall.getMethodName();
			final String header = "The method could not be found.";
			final String message = createMessage( header, className, methodName, methodCall );
			LOGGER.error( message, exception );
			throw new NotFoundException( message, exception );
		}
		return method;
	}

	/**
	 * Returns the parameter types associated with the method call
	 * @param methodCall The {@link javassist.expr.MethodCall}
	 * @return the parameter types associated with the method call
	 */
	private CtClass[] getParameterTypes( final MethodCall methodCall ) throws NotFoundException
	{
		CtClass[] parametertypes;
		try
		{
			parametertypes = getMethod( methodCall ).getParameterTypes();
		}
		catch( NotFoundException exception )
		{
			final String className = methodCall.getClassName();
			final String methodName = methodCall.getMethodName();
			final String header = "The parameter types associated with the diffusive method call could not be found.";
			final String message = createMessage( header, className, methodName, methodCall );
			LOGGER.error( message, exception );
			throw new NotFoundException( message, exception );
		}
		return parametertypes;
	}

	/**
	 * Returns the method's return type as a {@link javassist.CtClass}
	 * @param methodCall The {@link javassist.expr.MethodCall}
	 * @return The method's return type
	 */
	private CtClass getReturnType( final MethodCall methodCall ) throws NotFoundException
	{
		CtClass returnClass;
		try
		{
			returnClass = getMethod( methodCall ).getReturnType();
		}
		catch( NotFoundException exception )
		{
			final String className = methodCall.getClassName();
			final String methodName = methodCall.getMethodName();
			final String header = "The return type associated with the diffusive method call could not be found.";
			final String message = createMessage( header, className, methodName, methodCall );
			LOGGER.error( message, exception );
			throw new NotFoundException( message, exception );
		}
		return returnClass;
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
		return header + Constants.NEW_LINE +
				"  Class Name: " + className + Constants.NEW_LINE +
				"  Method Name: " + methodName + Constants.NEW_LINE +
				"  Method In Super Class: " + methodCall.isSuper() + Constants.NEW_LINE +
				"  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE +
				"  Source Method Signature: " + methodCall.where().getSignature() + Constants.NEW_LINE +
				"  Source File: " + methodCall.getFileName() + Constants.NEW_LINE +
				"  Line Number: " + methodCall.getLineNumber() + Constants.NEW_LINE;
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
		return header + Constants.NEW_LINE +
				"  Annotation Class Name: " + annotation.getName() + Constants.NEW_LINE +
				"  Class Name: " + className + Constants.NEW_LINE +
				"  Method Name: " + methodName + Constants.NEW_LINE +
				"  Method In Super Class: " + methodCall.isSuper() + Constants.NEW_LINE +
				"  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE +
				"  Source Method Signature: " + methodCall.where().getSignature() + Constants.NEW_LINE +
				"  Source File: " + methodCall.getFileName() + Constants.NEW_LINE +
				"  Line Number: " + methodCall.getLineNumber() + Constants.NEW_LINE;
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
		return header + Constants.NEW_LINE +
				"  Class Name: " + className + Constants.NEW_LINE +
				"  Method Name: " + methodName + Constants.NEW_LINE +
				"  Method In Super Class: " + methodCall.isSuper() + Constants.NEW_LINE +
				"  Source Method: " + methodCall.where().getName() + Constants.NEW_LINE +
				"  Source Method Signature: " + methodCall.where().getSignature() + Constants.NEW_LINE +
				"  Source File: " + methodCall.getFileName() + Constants.NEW_LINE +
				"  Line Number: " + methodCall.getLineNumber() +
				"  Replacement Code: " + Constants.NEW_LINE + code + Constants.NEW_LINE;
	}
}
