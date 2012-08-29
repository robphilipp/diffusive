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
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
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

	private final String baseSignature;
	private final boolean isUseSignature;
	
	/**
	 * Constructs the method intercepter. If the {@link #isUseSignature} is true then the
	 * method call to the method is replaced by a diffuser that is attached to a specific 
	 * diffuser method signature. If false, then the diffuser is the default diffuser in 
	 * the {@link KeyedDiffuserRepository}.
	 * 
	 * <p>For the application-attached diffuser, we want to use the one and only, the default, 
	 * diffuser. However, for the diffusers attached to the restful diffuser manager resource, 
	 * there is one diffuser per diffuser method signature, and so we want to use the signature.
	 * 
	 * <p>Effectively, the rule-of-thumb is that for application-attached diffusers set to false;
	 * and for diffusers managed by the {@link RestfulDiffuserManagerResource} set to true.
	 * 
	 * @param baseSignature The base signature is the signature associated with the specified diffuser.
	 * When the meth
	 * @param isUseSignature true then the method call to the method is replaced by a diffuser 
	 * that is attached to a specific diffuser method signature. If false, then the diffuser 
	 * is the default diffuser in the {@link KeyedDiffuserRepository}.
	 */
	public MethodIntercepterEditor( final String baseSignature )//, final boolean isUseSignature )
	{
		this.baseSignature = baseSignature;
		this.isUseSignature = ( baseSignature != null && !baseSignature.isEmpty() );
	}
	
	/**
	 * Default no arg constructor that sets the {@link #isUseSignature} to false.
	 * @see #MethodIntercepterEditor(boolean)
	 */
	public MethodIntercepterEditor()
	{
		this( null );//, false );
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
			// if the method itself is annotated with @Diffusive, AND, the method making the call is not
			// TODO does the check for nested diffusion have to be recursive? if so, how to do that with this framework
			if( methodCall.getMethod().getAnnotation( Diffusive.class ) != null )
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
					final String signature = DiffuserId.createId( returnType, className, methodName, argumentTypes );
					
					// if the signature equals the base signature, then we don't want to diffuse it further...so we leave it.
					// recall that the base signature is the signature associated with the diffuser that is responsible for
					// calling the method, and that means the method came from a remote address space, and shouldn't be diffused.
					// however, if the signatures aren't equal, then it is valid to be diffused as a nested diffusion.
					if( !baseSignature.equals( signature ) )
					{
						// the actual Diffusive call
						code.append( "    $_ = ($r)" + repoClassName + "." + getInstance );
						code.append( ".getDiffuser( \"" + signature + "\" ).runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $$ );" );
	
						// make the call to replace the code in the method call
						methodCall.replace( code.toString() );
					}
				}
				else
				{
					// the actual Diffusive call
					code.append( "    $_ = ($r)" + repoClassName + "." + getInstance ); 
					code.append( ".getDiffuser().runObject( " + Double.MAX_VALUE + ", $type, $0, \"" + methodName + "\", $$ );" );

					// make the call to replace the code in the method call
					methodCall.replace( code.toString() );
				}
				
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
			message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
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
