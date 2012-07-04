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

public class DiffuserId implements Copyable< DiffuserId > {
	
	private static final Logger LOGGER = Logger.getLogger( DiffuserId.class );
	
	// signature
	public static final String CLASS_METHOD_SEPARATOR = ":";
	public static final String ARGUMENT_SEPARATOR = ",";
	public static final String ARGUMENT_OPEN = "(";
	public static final String ARGUMENT_CLOSE = ")";
	public static final String RETURN_TYPE_SEPARATOR = "-";
	
	private final String className;
	private final String methodName;
	private final String returnTypeClassName;
	private final List< String > argumentTypes;
	
	public DiffuserId( final String returnTypeClassName, final String className, final String methodName, final List< String > argumentTypes )
	{
		this.className = className;
		this.methodName = methodName;
		this.returnTypeClassName = ( returnTypeClassName == null ? void.class.getName() : returnTypeClassName );
		this.argumentTypes = argumentTypes;
	}
	
	public DiffuserId( final String className, final String methodName, final List< String > argumentTypes )
	{
		this( void.class.getName(), className, methodName, argumentTypes );
	}
	
	public DiffuserId( final String id )
	{
		this( parse( id ) );
	}
	
	public DiffuserId( final DiffuserId id )
	{
		this.className = id.className;
		this.methodName = id.methodName;
		this.returnTypeClassName = id.returnTypeClassName;
		this.argumentTypes = new ArrayList<>( id.argumentTypes );
	}
	
	public String getReturnTypeClassName()
	{
		return returnTypeClassName;
	}
	
	public Class< ? > getReturnTypeClazz()
	{
		return getClazz( returnTypeClassName );
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public Class< ? > getClazz()
	{
		return getClazz( className );
	}
	
	public static Class< ? > getClazz( final String className )
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
	
	public String getMethodName()
	{
		return methodName;
	}
	
	public List< String > getArgumentTypeNames()
	{
		return argumentTypes;
	}
	
	public List< Class< ? > > getArgumentTypes()
	{
		final List< Class< ? > > types = new ArrayList<>();
		for( String type : argumentTypes )
		{
			types.add( getClazz( type ) );
		}
		return types;
	}
	
	public String getId()
	{
		return create( returnTypeClassName, className, methodName, argumentTypes );
	}

	public static String create( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		return create( void.class, clazz, methodName, argumentTypes );
	}
	
	public static String create( final Class< ? > returnType, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		final List< String > argumentTypeNames = new ArrayList<>();
		for( Class< ? > argType : argumentTypes )
		{
			argumentTypeNames.add( argType.getName() );
		}
		final String returnTypeClassName = ( returnType == null ? void.class.getName() : returnType.getName() );
		return DiffuserId.create( returnTypeClassName, clazz.getName(), methodName, argumentTypeNames );
	}

	public static String create( final String containingClassName, final String methodName, final List< String > argumentTypes )
	{
		return create( void.class.getName(), containingClassName, methodName, argumentTypes );
	}
	
	public static String create( final String returnType, final String containingClassName, final String methodName, final List< String > argumentTypes )
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
		
		// if there is a return type, then we add it, otherwise we don't
//		if( !returnType.equals( void.class.getName() ) )
//		{
			buffer.append( RETURN_TYPE_SEPARATOR + returnType );
//		}
		
		return buffer.toString();
	}
	
	public static DiffuserId parse( final String signature )
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
	
	@Override
	public DiffuserId getCopy()
	{
		return new DiffuserId( this );
	}

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