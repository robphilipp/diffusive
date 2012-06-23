package org.microtitan.diffusive.diffuser.restful;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.containers.Pair;

@XmlRootElement
public class ExecuteDiffuserRequest {

	private List< String > argumentTypes;
	private List< byte[] > argumentValues;
	private byte[] serializedObject;
	
	public ExecuteDiffuserRequest( final List< String> argumentTypes, final List< byte[] > argumentValues, final byte[] serializedObject )
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
	
	public void addArgument( final String key, final byte[] value )
	{
		argumentTypes.add( key );
		argumentValues.add( value );
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
	
	public void setObject( final byte[] serializedObject )
	{
		this.serializedObject = serializedObject; 
	}
	
	public byte[] getObject()
	{
		return serializedObject;
	}
}
