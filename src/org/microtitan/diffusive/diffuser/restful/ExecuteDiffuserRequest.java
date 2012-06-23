package org.microtitan.diffusive.diffuser.restful;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.containers.Pair;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

@XmlRootElement
public class ExecuteDiffuserRequest {

	private List< String > argumentTypes;
	private List< byte[] > argumentValues;
	private String serializedObjectType;
	private byte[] serializedObject;
	private String serializerType;
	
	public ExecuteDiffuserRequest( final List< String> argumentTypes, 
								   final List< byte[] > argumentValues, 
								   final byte[] serializedObject,
								   final String serializedObjectType,
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
		this.argumentTypes = argumentTypes;
		this.argumentValues = argumentValues;
		this.serializedObject = serializedObject;
		this.serializedObjectType = serializedObjectType;
		this.serializerType = serializerType;
	}
	
//	public void setArguments( final List< String> argumentTypes, final List< byte[] > argumentValues )
//	{
//		if( argumentTypes.size() == argumentValues.size() )
//		{
//			this.argumentTypes = argumentTypes;
//			this.argumentValues = argumentValues;
//		}
//	}
//	
//	public void addArguments( final List< String> argumentTypes, final List< byte[] > argumentValues )
//	{
//		if( argumentTypes.size() == argumentValues.size() )
//		{
//			for( int i = 0; i < argumentTypes.size(); ++i )
//			{
//				this.argumentTypes.add( argumentTypes.get( i ) );
//				this.argumentValues.add( argumentValues.get( i ) );
//			}
//		}
//	}
	
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
	
}
