package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.freezedry.persistence.utils.Constants;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory.SerializerType;

@XmlRootElement
public class CreateDiffuserRequest {
	
	private String containingClassName;
	private String methodName;
	private List< String > argumentTypes;
	private List< String > classPaths;
	private String serializerType;
	private List< String > clientEndpoints;

	/**
	 * 
	 * @param containingClassName The {@link Class} of the object containing the method to be called
	 * @param methodName The name of the method, in the specified {@link Class}, to be called
	 * @param argumentTypes The type of each argument accepted by the specified method. The types
	 * must be in the same order as in the method signature.
	 * @param classPaths The list of class paths
	 */
	public CreateDiffuserRequest( final String className, 
								  final String methodName, 
								  final List< String > argumentTypes, 
								  final List< String > classPaths,
								  final String serializerType,
								  final List< String > clientEndpoints )
	{
		this.containingClassName = className;
		this.methodName = methodName;
		this.argumentTypes = (argumentTypes == null ? new ArrayList< String >() : argumentTypes );
		this.classPaths = (classPaths == null ? new ArrayList< String >() : classPaths );
		this.serializerType = ( serializerType == null || serializerType.isEmpty() ? SerializerType.PERSISTENCE_XML.getName() : serializerType );
		this.clientEndpoints = (clientEndpoints == null ? new ArrayList< String >() : clientEndpoints );
	}

	/**
	 * Creates a default request to create a diffuser. The name of the containing class, the method name are 
	 * set to empty strings, the argument types, class paths, serializer type, and client endpoints are set to null.
	 */
	public CreateDiffuserRequest()
	{
		this( "", "", null, null, null, null );
	}
	
	/**
	 * Creates a request to create a diffuser that acts on the specified method of the class name, and has the
	 * specified argument types.
	 * @param className The name of 
	 * @param methodName
	 * @param argumentTypes
	 * @return
	 */
	public static CreateDiffuserRequest create( final String className, final String methodName, final String...argumentTypes )
	{
		final CreateDiffuserRequest request = new CreateDiffuserRequest();
		request.setContainingClass( className )
			   .setMethodName( methodName )
			   .setArgumentTypes( Arrays.asList( argumentTypes ) );
		return request;
	}
	
	public String getContainingClass()
	{
		return containingClassName;
	}
	
	public CreateDiffuserRequest setContainingClass( final String className )
	{
		this.containingClassName = className;
		return this;
	}
	
	public String getMethodName()
	{
		return methodName;
	}
	
	public CreateDiffuserRequest setMethodName( final String methodName )
	{
		this.methodName = methodName;
		return this;
	}
	
	public List< String > getArgumentTypes()
	{
		return argumentTypes;
	}
	
	public CreateDiffuserRequest setArgumentTypes( final List< String > argumentTypes )
	{
		this.argumentTypes = argumentTypes;
		return this;
	}
	
	public CreateDiffuserRequest appendArgumentType( final String argumentType )
	{
		if( argumentTypes == null )
		{
			argumentTypes = new ArrayList<>();
		}
		argumentTypes.add( argumentType );
		return this;
	}

	public List< String > getClassPaths()
	{
		return classPaths;
	}
	
	public CreateDiffuserRequest setClassPaths( final List< String > classPaths )
	{
		this.classPaths = classPaths;
		return this;
	}
	
	public CreateDiffuserRequest appendClassPath( final String classPath )
	{
		if( classPaths == null )
		{
			classPaths = new ArrayList<>();
		}
		classPaths.add( classPath );
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
	
	public CreateDiffuserRequest setSerializerType( final String serializer )
	{
		this.serializerType = serializer;
		return this;
	}
	
	public List< String > getClientEndpoints()
	{
		return clientEndpoints;
	}
	
	public List< URI > getClientEndpointsUri()
	{
		final List< URI > uri = new ArrayList<>();
		for( String endpoint : clientEndpoints )
		{
			uri.add( URI.create( endpoint ) );
		}
		return uri;
	}
	
	public CreateDiffuserRequest setClientEndpoints( final List< String > clientEndpoints )
	{
		this.clientEndpoints = clientEndpoints;
		return this;
	}
	
	public CreateDiffuserRequest appendClientEndpoints( final String clientEndpoint )
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
		buffer.append( CreateDiffuserRequest.class.getName() + Constants.NEW_LINE );
		buffer.append( "  Containing Class: " + containingClassName + Constants.NEW_LINE );
		buffer.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		buffer.append( "  Argument Types: " + Constants.NEW_LINE );
		for( String argType : argumentTypes )
		{
			buffer.append( "    " + argType + Constants.NEW_LINE );
		}
		buffer.append( "  Class Paths: " + Constants.NEW_LINE );
		for( String uri : classPaths )
		{
			buffer.append( "    " + uri + Constants.NEW_LINE );
		}
		return buffer.toString();
	}
}
