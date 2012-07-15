package org.microtitan.diffusive.diffuser.restful.request;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.containers.Pair;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.utils.ReflectionUtils;

/**
 * Represents the request issued to the RESTful service to execute a method within an object.
 * This class wraps the information needed to execute that method.
 *  
 * @author Robert Philipp
 * Jul 15, 2012
 */
@XmlRootElement
public class ExecuteDiffuserRequest {

	private String returnType;
	
	@XmlElement
	private List< String > argumentTypes;
	@XmlElement
	private List< byte[] > argumentValues;

	@XmlElement
	private String serializedObjectType;
	
	@XmlElement
	private byte[] serializedObject;
	private String serializerType;
	
	private final String requestId;
	
	/**
	 * Default constructor that sets the base defaults for the request
	 */
	public ExecuteDiffuserRequest()
	{
		this.returnType = void.class.getName();
		this.argumentTypes = new ArrayList< String >();
		this.argumentValues =  new ArrayList< byte[] >();
		this.requestId = UUID.randomUUID().toString();
	}

	/**
	 * Factory method for creating a request to execute a method.
	 * @param returnType The return type class name of the method.
	 * @param argumentTypes The class names of the methods formal parameters
	 * @param argumentValues The serialized values of the actual parameters passed to the method
	 * @param serializedObjectType The class name of the object whose class holds the method to be executed
	 * @param serializedObject The serialized object that contains the method
	 * @param serializerType The serializer type name (see {@link SerializerFactory})
	 * @return The request to execute the method
	 */
	public static final ExecuteDiffuserRequest create( final String returnType,
													   final List< String > argumentTypes,
													   final List< byte[] > argumentValues, 
													   final String serializedObjectType, 
													   final byte[] serializedObject,
													   final String serializerType )
	{
		if( argumentTypes.size() != argumentValues.size() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The number of method argument types must equal the number of method argument values" + Constants.NEW_LINE );
			final int argTypesSize = ( argumentTypes == null ? 0 : argumentTypes.size() );
			message.append( "  Number of Argument Types: " + argTypesSize + Constants.NEW_LINE );
			final int argValuesSize = ( argumentValues == null ? 0 : argumentValues.size() );
			message.append( "  Number of Argument Values: " + argValuesSize );
			throw new IllegalArgumentException( message.toString() );
		}
		
		final ExecuteDiffuserRequest request = create( returnType, serializedObjectType, serializedObject, serializerType );
		for( int i = 0; i < argumentTypes.size(); ++i )
		{
			request.addArgument( argumentTypes.get( i ), argumentValues.get( i ) );
		}
		return request;
	}

	/**
	 * Factory method for creating a request to execute a method.
	 * @param returnType The return type class name of the method.
	 * @param serializedObjectType The class name of the object whose class holds the method to be executed
	 * @param serializedObject The serialized object that contains the method
	 * @param serializerType The serializer type name (see {@link SerializerFactory})
	 * @return The request to execute the method
	 */
	public static final ExecuteDiffuserRequest create( final String returnType,
													   final String serializedObjectType, 
													   final byte[] serializedObject, 
													   final String serializerType )
	{
		final ExecuteDiffuserRequest request = new ExecuteDiffuserRequest();
		request.setObject( serializedObjectType, serializedObject )
			   .setSerializerType( serializerType )
			   .setReturnType( returnType );
		return request;
	}
	
	/**
	 * Sets the method's return type class name
	 * @param returnType The return type for the method to execute
	 * @return this object for chaining
	 */
	public ExecuteDiffuserRequest setReturnType( final String returnType )
	{
		this.returnType = returnType;
		return this;
	}
	
	/**
	 * @return the class name of the return type from the method to execute
	 */
	public String getReturnType()
	{
		return returnType;
	}
	
	/**
	 * @return the {@link Class} of the methods return type
	 */
	public Class< ? > getReturnTypeClass()
	{
		return ReflectionUtils.getClazz( returnType );
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public ExecuteDiffuserRequest addArgument( final String key, final byte[] value )
	{
		argumentTypes.add( key );
		argumentValues.add( value );
		return this;
	}
	
	public List< Pair< String, byte[] > > getArguments()
	{
		final List< Pair< String, byte[] > > arguments = new ArrayList<>();
		for( int i = 0; i < argumentTypes.size(); ++i )
		{
			arguments.add( new Pair< String, byte[] >( argumentTypes.get( i ), argumentValues.get( i ) ) );
		}
		return arguments;
	}
	
	public List< String > getArgumentTypes()
	{
		return argumentTypes;
	}
	
	public List< byte[] > getArgumentValues()
	{
		return argumentValues;
	}
	
	public ExecuteDiffuserRequest setObject( final String objectType, final byte[] serializedObject )
	{
		this.serializedObjectType = objectType;
		this.serializedObject = serializedObject;
		return this;
	}
	
	public byte[] getObject()
	{
		return serializedObject;
	}
	
	public String getObjectType()
	{
		return serializedObjectType;
	}
	
	public ExecuteDiffuserRequest setSerializerType( final String serializerType )
	{
		this.serializerType = serializerType;
		return this;
	}
	
	public String getSerializerType()
	{
		return serializerType;
	}
	
	public Serializer getSerializer()
	{
		return SerializerFactory.getInstance().createSerializer( serializerType );
	}
	
	public String getRequestId()
	{
		return requestId;
	}
}
