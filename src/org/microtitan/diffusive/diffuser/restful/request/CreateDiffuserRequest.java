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
package org.microtitan.diffusive.diffuser.restful.request;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory.SerializerType;

@XmlRootElement
public class CreateDiffuserRequest {
	
	private String containingClassName;
	private String methodName;
	private String returnTypeClassName;
	private List< String > argumentTypes;
	private List< String > classPaths;
	private String serializerType;
	private List< String > clientEndpoints;

	/**
	 * Constructs the request to create a diffuser based on the specified parameters
	 * @param returnTypeClassName The fully qualified class name of the return type
	 * @param className The fully qualified class name of the class containing the specified method
	 * @param methodName The name of the method, in the specified {@link Class}, to be called
	 * @param argumentTypes The type of each argument accepted by the specified method. The types
	 * must be in the same order as in the method signature.
	 * @param classPaths The list of class paths
	 * @param serializerType The name of the serializer. See {@link SerializerType} for a list of
	 * serializer names. The serializers are instantiated using the {@link SerializerFactory}.
	 * @param clientEndpoints The list of end-point {@link URI} to which run/execution is diffused.
	 */
	public CreateDiffuserRequest( final String returnTypeClassName, 
								  final String className,
								  final String methodName,
								  final List< String > argumentTypes, 
								  final List< String > classPaths,
								  final String serializerType,
								  final List< String > clientEndpoints )
	{
		this.containingClassName = className;
		this.methodName = methodName;
		this.returnTypeClassName = returnTypeClassName;
		this.argumentTypes = (argumentTypes == null ? new ArrayList< String >() : argumentTypes );
		this.classPaths = (classPaths == null ? new ArrayList< String >() : classPaths );
		this.serializerType = ( serializerType == null || serializerType.isEmpty() ? SerializerType.PERSISTENCE_XML.getName() : serializerType );
		this.clientEndpoints = (clientEndpoints == null ? new ArrayList< String >() : clientEndpoints );
	}

	/**
	 * Constructs the request to create a diffuser based on the specified parameters. Sets the return
	 * type to be {@code void} using {@code void.class.getName()}.
	 * @param className The fully qualified class name of the class containing the specified method
	 * @param methodName The name of the method, in the specified {@link Class}, to be called
	 * @param argumentTypes The type of each argument accepted by the specified method. The types
	 * must be in the same order as in the method signature.
	 * @param classPaths The list of class paths
	 * @param serializerType The name of the serializer. See {@link SerializerType} for a list of
	 * serializer names. The serializers are instantiated using the {@link SerializerFactory}.
	 * @param clientEndpoints The list of end-point {@link URI} to which run/execution is diffused.
	 */
	public CreateDiffuserRequest( final String className, 
								  final String methodName,
								  final List< String > argumentTypes, 
								  final List< String > classPaths,
								  final String serializerType,
								  final List< String > clientEndpoints )
	{
		this( void.class.getName(), className, methodName, argumentTypes, classPaths, serializerType, clientEndpoints );
	}

	/**
	 * Creates a default request to create a diffuser. The name of the containing class, the method name are 
	 * set to empty strings, the argument types, class paths, serializer type, and client end-points are set to null.
	 */
	public CreateDiffuserRequest()
	{
		this( "", "", null, null, null, null );
	}
	
	/**
	 * Creates a request to create a diffuser that acts on the specified method of the class name, and has the
	 * specified argument types.
	 * @param classPaths A list of URI (represented as strings) used to load classes remotely.
	 * @param className The name of class containing the method that is diffused by this diffuser.
	 * @param methodName The name of the method to diffuse
	 * @param returnTypeClassName The fully qualified class name representing the return type
	 * @param argumentTypes The fully qualified class names of the method's parameters
	 * @return a request to create a diffuser that acts on the specified method of the class name, and has the
	 * specified argument types.
	 */
	public static CreateDiffuserRequest create( final List< String > classPaths, 
												final String className, 
												final String methodName, 
												final String returnTypeClassName, 
												final String...argumentTypes )
	{
		final CreateDiffuserRequest request = new CreateDiffuserRequest();
		request.setContainingClassName( className )
			   .setMethodName( methodName )
			   .setReturnTypeClassName( returnTypeClassName )
			   .setArgumentTypes( Arrays.asList( argumentTypes ) )
			   .setClassPaths( classPaths );
		return request;
	}
	
	/**
	 * @return the fully qualified class name of the class containing the method to diffuse
	 */
	public String getContainingClassName()
	{
		return containingClassName;
	}
	
	/**
	 * Sets the fully qualified class name of the class containing the method to diffuse
	 * @param className The fully qualified class name of the class containing the method to diffuse
	 * @return This object to be used for chaining
	 */
	public CreateDiffuserRequest setContainingClassName( final String className )
	{
		this.containingClassName = className;
		return this;
	}
	
	/**
	 * @return The name of the method to diffuse
	 */
	public String getMethodName()
	{
		return methodName;
	}
	
	public CreateDiffuserRequest setMethodName( final String methodName )
	{
		this.methodName = methodName;
		return this;
	}
	
	public String getReturnTypeClassName()
	{
		return returnTypeClassName;
	}
	
	public CreateDiffuserRequest setReturnTypeClassName( final String returnClass )
	{
		this.returnTypeClassName = returnClass;
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
	
	public List< URI > getClassPathsUri()
	{
		final List< URI > uri = new ArrayList<>();
		for( String classPath : classPaths )
		{
			uri.add( URI.create( classPath ) );
		}
		return uri;
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( CreateDiffuserRequest.class.getName() + Constants.NEW_LINE );
		buffer.append( "  Containing Class: " + containingClassName + Constants.NEW_LINE );
		buffer.append( "  Method Name: " + methodName + Constants.NEW_LINE );
		buffer.append( "  Return Class: " + returnTypeClassName + Constants.NEW_LINE );
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
