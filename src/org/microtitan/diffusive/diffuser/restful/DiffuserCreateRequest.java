package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.diffuser.serializer.Serializer;

@XmlRootElement
public class DiffuserCreateRequest {
	
	@XmlElement
	@XmlJavaTypeAdapter( ClassNameAdapter.class )
	private Class< ? > clazz;
	
	@XmlElement
	private String methodName;
	
	@XmlElement
	@XmlJavaTypeAdapter( ClassNameListAdapter.class )
	private List< Class< ? > > argumentTypes;
	
	@XmlElement
	@XmlJavaTypeAdapter( UriListAdapter.class )
	private List< URI > classPaths;
	
	@XmlElement
	@XmlJavaTypeAdapter( SerializerNameAdapter.class )
	private Serializer serializer;
	
	@XmlElement
	@XmlJavaTypeAdapter( UriListAdapter.class )
	private List< URI > clientEndpoints;

	/**
	 * 
	 * @param clazz The {@link Class} of the object containing the method to be called
	 * @param methodName The name of the method, in the specified {@link Class}, to be called
	 * @param argumentTypes The type of each argument accepted by the specified method. The types
	 * must be in the same order as in the method signature.
	 * @param classPaths The list of class paths
	 */
	public DiffuserCreateRequest( final Class< ? > clazz, 
								  final String methodName, 
								  final List< Class< ? > > argumentTypes, 
								  final List< URI > classPaths,
								  final Serializer serializer,
								  final List< URI > clientEndpoints )
	{
		this.clazz = clazz;
		this.methodName = methodName;
		this.argumentTypes = argumentTypes;
		this.classPaths = classPaths;
	}
	
	public Class< ? > getContainingClass()
	{
		return clazz;
	}
	
	public DiffuserCreateRequest setContainingClass( final Class< ? > clazz )
	{
		this.clazz = clazz;
		return this;
	}
	
	public String getMethodName()
	{
		return methodName;
	}
	
	public DiffuserCreateRequest setMethodName( final String methodName )
	{
		this.methodName = methodName;
		return this;
	}
	
	public List< Class< ? > > getArgumentTypes()
	{
		return argumentTypes;
	}
	
	public DiffuserCreateRequest setArgumentTypes( final List< Class< ? > > argumentTypes )
	{
		this.argumentTypes = argumentTypes;
		return this;
	}
	
	public DiffuserCreateRequest appendArgumentType( final Class< ? > argumentType )
	{
		if( argumentTypes == null )
		{
			argumentTypes = new ArrayList<>();
		}
		argumentTypes.add( argumentType );
		return this;
	}

	public List< URI > getClassPaths()
	{
		return classPaths;
	}
	
	public DiffuserCreateRequest setClassPaths( final List< URI > classPaths )
	{
		this.classPaths = classPaths;
		return this;
	}
	
	public DiffuserCreateRequest appendClassPath( final URI classPath )
	{
		if( classPaths == null )
		{
			classPaths = new ArrayList<>();
		}
		classPaths.add( classPath );
		return this;
	}
	
	public Serializer getSerializer()
	{
		return serializer;
	}
	
	public DiffuserCreateRequest setSerializer( final Serializer serializer )
	{
		this.serializer = serializer;
		return this;
	}
	
	public List< URI > getClientEndpoints()
	{
		return clientEndpoints;
	}
	
	public DiffuserCreateRequest setClientEndpoints( final List< URI > clientEndpoints )
	{
		this.clientEndpoints = clientEndpoints;
		return this;
	}
	
	public DiffuserCreateRequest appendClientEndpoints( final URI clientEndpoint )
	{
		if( clientEndpoints == null )
		{
			clientEndpoints = new ArrayList<>();
		}
		clientEndpoints.add( clientEndpoint );
		return this;
	}
	
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( DiffuserCreateRequest.class.getName() + Constants.NEW_LINE );
		buffer.append( "  Containing Class: " + clazz.getName() + Constants.NEW_LINE );
		buffer.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		buffer.append( "  Argument Types: " + Constants.NEW_LINE );
		for( Class< ? > argType : argumentTypes )
		{
			buffer.append( "    " + argType.getName() + Constants.NEW_LINE );
		}
		buffer.append( "  Class Paths: " + Constants.NEW_LINE );
		for( URI uri : classPaths )
		{
			buffer.append( "    " + uri.toString() + Constants.NEW_LINE );
		}
		return buffer.toString();
	}
}
