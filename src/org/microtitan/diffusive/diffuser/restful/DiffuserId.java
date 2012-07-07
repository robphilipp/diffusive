package org.microtitan.diffusive.diffuser.restful;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.copyable.Copyable;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.Diffuser;

/**
 * Encapsulates the key used to identify a {@link Diffuser} for a specific method. The {@link Diffuser} ID
 * is based on:
 * <ul>
 * 	<li>the name of the {@link Class} that contains the diffused method</li>
 * 	<li>the name of the method</li>
 * 	<li>a list of the class names for each of the arguments passed to the method</li>
 * 	<li>the {@link Class} name of the return type</li>
 * </ul>
 * The {@link Diffuser} ID is constructed as follows:<p>
 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
 * is the fully qualified class name of the return type (which could be {@code void.class}.
 * 
 * The {@link DiffuserId} can be instantiated through the constructors, or preferably, through one of the
 * four {@code create(...)} methods, or the {@link #parse(String)} method.
 * 
 * The {@link DiffuserId} objects are immutable.
 * 
 * @author Robert Philipp
 */
public class DiffuserId implements Copyable< DiffuserId > {
	
	private static final Logger LOGGER = Logger.getLogger( DiffuserId.class );
	
	// signature punctuation
	public static final String CLASS_METHOD_SEPARATOR = ":";
	public static final String ARGUMENT_SEPARATOR = ",";
	public static final String ARGUMENT_OPEN = "(";
	public static final String ARGUMENT_CLOSE = ")";
	public static final String RETURN_TYPE_SEPARATOR = "-";
	
	// signature
	private final String className;
	private final String methodName;
	private final String returnTypeClassName;
	private final List< String > argumentTypes;
	private final String signature;
	
	/**
	 * Constructs a {@link DiffuserId} based on the specified return type name, the name of the class containing the
	 * diffusive method, the method to diffuse, and the list of argument type names. 
	 * @param returnTypeClassName The name of the return type
	 * @param className The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 */
	public DiffuserId( final String returnTypeClassName, final String className, final String methodName, final List< String > argumentTypes )
	{
		this.className = className;
		this.methodName = methodName;
		this.returnTypeClassName = ( returnTypeClassName == null ? void.class.getName() : returnTypeClassName );
		this.argumentTypes = argumentTypes;
		
		// construct the signature
		this.signature = createId( returnTypeClassName, className, methodName, argumentTypes );
	}
	
	/**
	 * Constructs a {@link DiffuserId} for a method that does not return a value, the name of the class containing the
	 * diffusive method, the method to diffuse, and the list of argument type names. 
	 * @param className The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 */
	public DiffuserId( final String className, final String methodName, final List< String > argumentTypes )
	{
		this( void.class.getName(), className, methodName, argumentTypes );
	}
	
	/**
	 * Constructs a {@link DiffuserId} by parsing an existing diffuser ID string. The {@link Diffuser} ID 
	 * is constructed as follows:<p>
	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
	 * is the fully qualified class name of the return type (which could be {@code void.class}.
	 * @param id The diffuser ID string
	 */
	public DiffuserId( final String id )
	{
		this( parse( id ) );
	}
	
	/**
	 * Copy constructor that creates a copy of the specified diffuser ID. {@link DiffuserId} objects are
	 * immutable, so this isn't terribly necessary. 
	 * @param id The {@link DiffuserId} to copy
	 */
	public DiffuserId( final DiffuserId id )
	{
		this( id.returnTypeClassName, id.className, id.methodName, new ArrayList<>( id.argumentTypes ) );
//		this.className = id.className;
//		this.methodName = id.methodName;
//		this.returnTypeClassName = id.returnTypeClassName;
//		this.argumentTypes = new ArrayList<>( id.argumentTypes );
	}
	
	/**
	 * @return the class name of the return value
	 */
	public String getReturnTypeClassName()
	{
		return returnTypeClassName;
	}
	
	/**
	 * @return The {@link Class} of the return value
	 */
	public Class< ? > getReturnTypeClazz()
	{
		return getClazz( returnTypeClassName );
	}
	
	/**
	 * @return The name of the class that contains the method to be diffused
	 */
	public String getClassName()
	{
		return className;
	}
	
	/**
	 * @return The {@link Class} that contains the method to be diffused
	 */
	public Class< ? > getClazz()
	{
		return getClazz( className );
	}
	
	/*
	 * Utility method that returns the {@link Class} object for the specified class name.
	 * @param className The name of the {@link Class} for which to return the {@link Class} object.
	 * @return the {@link Class} object for the specified class name.
	 */
	private static Class< ? > getClazz( final String className )
	{
		Class< ? > clazz = null;
		try
		{
			clazz = Class.forName( className );
		}
		catch( ClassNotFoundException e )
		{
			final String message = "Could not instantiate class from specified class name: " + className;
			LOGGER.error( message, e );
			throw new IllegalArgumentException( message, e );
		}
		return clazz;
	}
	
	/**
	 * @return the name of the method that is diffused
	 */
	public String getMethodName()
	{
		return methodName;
	}
	
	/**
	 * @return A list of {@link Class} names corresponding the to arguments passed to the diffused method
	 */
	public List< String > getArgumentTypeNames()
	{
		return argumentTypes;
	}
	
	/**
	 * @return A list of {@link Class} objects corresponding the to arguments passed to the diffused method
	 */
	public List< Class< ? > > getArgumentTypes()
	{
		final List< Class< ? > > types = new ArrayList<>();
		for( String type : argumentTypes )
		{
			types.add( getClazz( type ) );
		}
		return types;
	}
	
	/**
	 * Constructs a diffuser ID string for a method that doesn't have a return value
	 * @param clazz The {@link Class} that contains the diffused method
	 * @param methodName The name of the diffused method
	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
	 * @return a diffuser ID string for a method that doesn't have a return value
	 */
	public static final String createId( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		return createId( void.class, clazz, methodName, argumentTypes );
	}
	
	/**
	 * Constructs a diffuser ID string
	 * @param returnType The {@link Class} object representing the method's return type
	 * @param clazz The {@link Class} that contains the diffused method
	 * @param methodName The name of the diffused method
	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
	 * @return a diffuser ID string
	 */
	public static final String createId( final Class< ? > returnType, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		final List< String > argumentTypeNames = new ArrayList<>();
		for( Class< ? > argType : argumentTypes )
		{
			argumentTypeNames.add( argType.getName() );
		}
		final String returnTypeClassName = ( returnType == null ? void.class.getName() : returnType.getName() );
		return createId( returnTypeClassName, clazz.getName(), methodName, argumentTypeNames );
	}

	/**
	 * Constructs the diffuser ID string, for a method that does not return a value, based on 
	 * the specified values
	 * @param containingClassName The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 * @return the diffuser ID string based on the specified values
	 */
	public static final String createId( final String containingClassName, 
			 							 final String methodName, 
			 							 final List< String > argumentTypes )
	{
		return createId( void.class.getName(), containingClassName, methodName, argumentTypes );
	}
	
	/**
	 * Constructs the diffuser ID string based on the specified values
	 * @param returnType The name of the return type
	 * @param containingClassName The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 * @return the diffuser ID string based on the specified values
	 */
	public static final String createId( final String returnType, 
										 final String containingClassName, 
										 final String methodName, 
										 final List< String > argumentTypes )
	{
		// create the name/id for the diffuser
		final StringBuffer buffer = new StringBuffer();
		buffer.append( containingClassName + CLASS_METHOD_SEPARATOR );
		buffer.append( methodName + ARGUMENT_OPEN );
		for( int i = 0; i < argumentTypes.size(); ++i )
		{
			buffer.append( argumentTypes.get( i ) );
			if( i < argumentTypes.size()-1 )
			{
				buffer.append( ARGUMENT_SEPARATOR );
			}
		}
		buffer.append( ARGUMENT_CLOSE );
		buffer.append( RETURN_TYPE_SEPARATOR + returnType );
		
		return buffer.toString();
	}
	
	/**
	 * @return The diffuser ID string
	 */
	public String getId()
	{
		return signature;
	}

	/**
	 * Constructs a {@link DiffuserId} for a method that doesn't have a return value
	 * @param clazz The {@link Class} that contains the diffused method
	 * @param methodName The name of the diffused method
	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
	 * @return a {@link DiffuserId} for a method that doesn't have a return value
	 */
	public static final synchronized DiffuserId create( final Class< ? > clazz, 
														final String methodName, 
														final Class< ? >...argumentTypes )
	{
		return create( void.class, clazz, methodName, argumentTypes );
	}
	
	/**
	 * Constructs a {@link DiffuserId}
	 * @param returnType The {@link Class} object representing the method's return type
	 * @param clazz The {@link Class} that contains the diffused method
	 * @param methodName The name of the diffused method
	 * @param argumentTypes The {@link Class} objects of the arguments passed into the diffused method
	 * @return a {@link DiffuserId}
	 */
	public static final synchronized DiffuserId create( final Class< ? > returnType, 
														final Class< ? > clazz, 
														final String methodName, 
														final Class< ? >...argumentTypes )
	{
		final List< String > argumentTypeNames = new ArrayList<>();
		for( Class< ? > argType : argumentTypes )
		{
			argumentTypeNames.add( argType.getName() );
		}
		final String returnTypeClassName = ( returnType == null ? void.class.getName() : returnType.getName() );
		return create( returnTypeClassName, clazz.getName(), methodName, argumentTypeNames );
	}

	/**
	 * Constructs the {@link DiffuserId}, for a method that does not return a value, based on 
	 * the specified values
	 * @param containingClassName The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 * @return the {@link DiffuserId} based on the specified values
	 */
	public static final synchronized DiffuserId create( final String containingClassName, 
														final String methodName, 
														final List< String > argumentTypes )
	{
		return create( void.class.getName(), containingClassName, methodName, argumentTypes );
	}
	
	/**
	 * Constructs the {@link DiffuserId} based on the specified values
	 * @param returnType The name of the return type
	 * @param containingClassName The name of the class that contains the method to diffuse
	 * @param methodName The name of the method to diffuse
	 * @param argumentTypes The class names of the arguments passed to the method
	 * @return the {@link DiffuserId} based on the specified values
	 */
	public static final synchronized DiffuserId create( final String returnType, 
														final String containingClassName, 
														final String methodName, 
														final List< String > argumentTypes )
	{
		return new DiffuserId( returnType, containingClassName, methodName, argumentTypes );
	}
	
	/**
	 * Parses the specified diffuser ID string into a {@link DiffuserId} object. The {@link Diffuser} ID 
	 * is constructed as follows:<p>
	 * {@code class:method(argument1,argument2,argument3,...,argumentN)-returnType}<p> 
	 * where the "{@code class}" is the fully qualified class name; the method is the name of the method;
	 * the arguments are all the fully qualified class names of the argument; and the {@code returnType}
	 * is the fully qualified class name of the return type (which could be {@code void.class}.
	 * @param signature The diffuser ID string representing the signature
	 * @return a {@link DiffuserId} object based on the specified diffuser ID string
	 */
	public static final synchronized DiffuserId parse( final String signature )
	{
		String className = null;
		String methodName = null;
		String returnClassName = null;
		List< String > argumentTypes = null;
		
		// parse
		final String validName = "[a-zA-Z]+[\\w]*";
		final String validClassName = validName + "(\\." + validName + ")*";
		final String validMethodName = validName;
		final String argumentTypeList = "(" + validClassName + "(" + Pattern.quote( ARGUMENT_SEPARATOR ) + validClassName +")*)*";
		final String returnTypeClassName = "(" + Pattern.quote( RETURN_TYPE_SEPARATOR ) + validClassName + ")?";
		final String regex = "^" + 
								validClassName + 
								Pattern.quote( CLASS_METHOD_SEPARATOR )+ 
								validMethodName + 
								Pattern.quote( ARGUMENT_OPEN ) +
									argumentTypeList +
								Pattern.quote( ARGUMENT_CLOSE ) + 
								returnTypeClassName +
							 "$";
		
		final Pattern pattern = Pattern.compile( regex );
		Matcher matcher = pattern.matcher( signature );
		if( matcher.find() )
		{
			// grab the class name
			matcher = Pattern.compile( "^" + validClassName ).matcher( signature );
			matcher.find();
			className = matcher.group();
			
			// grab the method name (we know at this point that we have at least "class:method"
			matcher = Pattern.compile( "^" + validMethodName ).matcher( signature.split( Pattern.quote( CLASS_METHOD_SEPARATOR ) )[ 1 ] );
			matcher.find();
			methodName = matcher.group();
			
			// now parse out the argument types
			final String argString = "^" + argumentTypeList + Pattern.quote( ARGUMENT_CLOSE );// + "$";
			matcher = Pattern.compile( argString ).matcher( signature.split( Pattern.quote( ARGUMENT_OPEN ) )[ 1 ] );
			matcher.find();
			final String endString = matcher.group();
			if( endString.equals( ARGUMENT_CLOSE ) )
			{
				argumentTypes = new ArrayList<>();
			}
			else
			{
				argumentTypes = Arrays.asList( endString.split( Pattern.quote( ARGUMENT_CLOSE ) )[ 0 ].split( Pattern.quote( ARGUMENT_SEPARATOR ) ) );
			}
			
			// now parse out the return type
			final String[] returnTypes = signature.split( Pattern.quote( RETURN_TYPE_SEPARATOR ) );
			if( returnTypes != null && returnTypes.length > 1 )
			{
				final String returnTypeString = "^" + validClassName + "$";
				matcher = Pattern.compile( returnTypeString ).matcher( returnTypes[ 1 ] );
				matcher.find();
				returnClassName = matcher.group();
			}
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to parse signature: Invalid DiffuserId" + Constants.NEW_LINE );
			message.append( "  Specified DiffuserId: " + signature );
			try
			{
				final DiffuserId sig = DiffuserId.parse( signature.replaceAll( "\\s", "" ) );
				message.append( Constants.NEW_LINE + "  Hint: try removing spaces from signature" + Constants.NEW_LINE );
				message.append( "  Recommended DiffuserId: " + sig.getId() );
			}
			catch( IllegalArgumentException e ) {}
			
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}
		
		// return
		return new DiffuserId( returnClassName, className, methodName, argumentTypes );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public DiffuserId getCopy()
	{
		return new DiffuserId( this );
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "ID: " + getId() + Constants.NEW_LINE );
		buffer.append( "  Class Name: " + className + Constants.NEW_LINE );
		buffer.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		buffer.append( "  Return Class Name: " + returnTypeClassName + Constants.NEW_LINE );
		if( argumentTypes == null || argumentTypes.isEmpty() )
		{
			buffer.append( "  [No Method Arguements] " + Constants.NEW_LINE );
		}
		else
		{
			buffer.append( "  Argument Type List" + Constants.NEW_LINE );
			for( String argumentType : argumentTypes )
			{
				buffer.append( "    " + argumentType + Constants.NEW_LINE );
			}
		}
		return buffer.toString();
	}
	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );

		System.out.println( "1   " + DiffuserId.parse( "java.lang.String:concat(java.lang.String,java.lang.String)" ).toString() );
		System.out.println( "2   " + DiffuserId.parse( "java.lang.String:concat(java.lang.String)" ).toString() );
		System.out.println( "3   " + DiffuserId.parse( "java.lang.String:concat()" ).toString() );
		System.out.println( "3a  " + DiffuserId.parse( "java.lang.String:concat();java.lang.Double" ).toString() );
		System.out.println( "4   " + DiffuserId.parse( "java.lang.String:concat( java.lang.String, java.lang.String )" ).toString() );
		System.out.println( "5   " + DiffuserId.parse( "java.lang.String:concat.test(java.lang.String,java.lang.String)" ).toString() );
	}
}