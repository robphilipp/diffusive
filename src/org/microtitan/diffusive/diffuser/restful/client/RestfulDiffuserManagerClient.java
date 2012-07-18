package org.microtitan.diffusive.diffuser.restful.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
import org.microtitan.diffusive.diffuser.restful.RestfulDiffuserServer;
import org.microtitan.diffusive.diffuser.restful.atom.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.request.CreateDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.request.ExecuteDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.response.CreateDiffuserResponse;
import org.microtitan.diffusive.diffuser.restful.response.DeleteDiffuserResponse;
import org.microtitan.diffusive.diffuser.restful.response.ExecuteDiffuserResponse;
import org.microtitan.diffusive.diffuser.restful.response.ListDiffuserResponse;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.serializer.XmlPersistenceSerializer;
import org.microtitan.diffusive.tests.Bean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * Client that wraps the RESTful API for interacting with RESTful diffusers in a convenient Java wrapper.
 *   
 * @author Robert Philipp
 */
public class RestfulDiffuserManagerClient {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerClient.class );
	
	private URI baseUri;
	private final Abdera abdera;
	private final Client client;
	
	/**
	 * Constructs a {@link RestfulDiffuserManagerClient} that connects to a {@link RestfulDiffuserServer} with
	 * an end-point at the specified base URI. The base URI is the starting point to which diffuser-specific path
	 * information is added.
	 * @param baseUri The {@link RestfulDiffuserServer} end-point to which this client connects
	 */
	public RestfulDiffuserManagerClient( final URI baseUri )
	{
		this.baseUri = baseUri;

		// atom parser/create
		this.abdera = AbderaFactory.getInstance();

		// create the Jersey RESTful client
		this.client = RestfulClientFactory.getInstance();
	}
	
	/**
	 * Constructs a {@link RestfulDiffuserManagerClient} that connects to a {@link RestfulDiffuserServer} with
	 * an end-point at the specified base URI. The base URI is the starting point to which diffuser-specific path
	 * information is added.
	 * @param baseUri The {@link RestfulDiffuserServer} end-point to which this client connects
	 */
	public RestfulDiffuserManagerClient( final String baseUri )
	{
		this( URI.create( baseUri ) );
	}

	/**
	 * Requests that the server create a RESTful diffuser for a method that doesn't return any value. 
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @return An Atom feed containing the result of the create request, and specifically, the URI of the newly
	 * created diffuser.
	 */
	public CreateDiffuserResponse createDiffuser( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		return createDiffuser( new ArrayList< URI >(), void.class, clazz, methodName, argumentTypes );
	}

	/**
	 * Requests that the server create a RESTful diffuser for a method that doesn't return any value. 
	 * @param classPathUri The list of URI which to search for remote classes
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @return An Atom feed containing the result of the create request, and specifically, the URI of the newly
	 * created diffuser.
	 */
	public CreateDiffuserResponse createDiffuser( final List< URI > classPathUri, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		return createDiffuser( classPathUri, void.class, clazz, methodName, argumentTypes );
	}

//	/**
//	 * Requests that the server create a RESTful diffuser for a method that returns a value. 
//	 * @param returnTypeClazz The {@link Class} of the return type of the diffusive method
//	 * @param clazz The {@link Class} containing the diffusive method 
//	 * @param methodName The name of the diffusive method
//	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
//	 * @return An Atom feed containing the result of the create request, and specifically, the URI of the newly
//	 * created diffuser.
//	 */
//	public CreateDiffuserResponse createDiffuser( final Class< ? > returnTypeClazz, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
//	{
//		return createDiffuser( new ArrayList< URI >(), returnTypeClazz, clazz, methodName, argumentTypes );
//	}
	
	/**
	 * Requests that the server create a RESTful diffuser for a method that returns a value. 
	 * @param classPathUri The list of URI which to search for remote classes
	 * @param returnTypeClazz The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @return An Atom feed containing the result of the create request, and specifically, the URI of the newly
	 * created diffuser.
	 */
	public CreateDiffuserResponse createDiffuser( final List< URI > classPathUri, final Class< ? > returnTypeClazz, final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		// convert the argument types to argument type names
		final String[] argumentTypeNames = convertArgumentTypes( argumentTypes );
		
		// convert the class path URI list into a list of string
		final List< String > classPaths = convertClassPaths( classPathUri );
		
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final CreateDiffuserRequest request = CreateDiffuserRequest.create( classPaths, clazz.getName(), methodName, returnTypeClazz.getName(), argumentTypeNames );
		
		// create the web resource for making the call, make the call to PUT the create-request to the server
		final WebResource resource = client.resource( baseUri.toString() );
		final ClientResponse createDiffuserResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).put( ClientResponse.class, request );
		
		// parse the response into an Atom feed object and return it
		Feed feed = null;
		try( InputStream response = createDiffuserResponse.getEntity( InputStream.class ) )
		{
			feed = abdera.getParser().< Feed >parse( response ).getRoot();
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to parse the create-diffuser response into an Atom feed" + Constants.NEW_LINE );
			message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
			message.append( "  Argument Type Names: " + Constants.NEW_LINE );
			for( String name : argumentTypeNames )
			{
				message.append( "    " + name + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return new CreateDiffuserResponse( feed );
	}
	
	private static List< String > convertClassPaths( final List< URI > classPathUri )
	{
		final List< String > classPaths = new ArrayList<>();
		for( final URI uri : classPathUri )
		{
			classPaths.add( uri.toString() );
		}
		return classPaths;
	}
	
	/**
	 * @return an Atom feed containing a list of entries that hold information about the diffuser resources.
	 */
	public ListDiffuserResponse getDiffuserList()
	{
		// create the web resource for making the call
		final WebResource resource = client.resource( baseUri.toString() );

		final ClientResponse clientResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );
		
		Feed feed = null;
		if( clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode() )
		{
			try( InputStream response = clientResponse.getEntity( InputStream.class ) )
			{
				feed = abdera.getParser().< Feed >parse( response ).getRoot();
			}
			catch( IOException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Failed to parse the get-diffuser-list response into an Atom feed" + Constants.NEW_LINE );
				LOGGER.error( message.toString(), e );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( clientResponse.toString() );
			LOGGER.warn( message.toString() );
		}
		
		return new ListDiffuserResponse( feed );
	}

	/**
	 * Deletes the diffuser with a signature that matches the specified information 
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @return An Atom feed containing the information about the deleted diffuser
	 */
	public DeleteDiffuserResponse deleteDiffuser( final Class< ? > clazz, final String methodName,  final Class< ? >...argumentTypes )
	{
		return deleteDiffuser( void.class, clazz, methodName, argumentTypes );
	}
	
	/**
	 * Deletes the diffuser with a signature and return type that matches the specified information
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @return An Atom feed containing the information about the deleted diffuser
	 */
	public DeleteDiffuserResponse deleteDiffuser( final Class< ? > returnType, final Class< ? > clazz, final String methodName,  final Class< ? >...argumentTypes )
	{
		return deleteDiffuser( DiffuserId.createId( returnType, clazz, methodName, argumentTypes ) );
	}
	
	/**
	 * Deletes the diffuser with a signature and return type that matches the specified information
	 * @param signature a {@link DiffuserId} signature (which is different from a Java signature because it includes
	 * the return type) of the diffuser to delete
	 * @return An Atom feed containing the information about the deleted diffuser
	 */
	public DeleteDiffuserResponse deleteDiffuser( final String signature )
	{
		// create the URI to the diffuser with the specified signature
		final URI diffuserUri = UriBuilder.fromUri( baseUri.toString() ).path( signature ).build();
		
		// create the web resource for making the call
		final WebResource resource = client.resource( diffuserUri.toString() );
		
		// make the call to delete the resource
		final ClientResponse clientResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).delete( ClientResponse.class );
		
		Feed feed = null;
		if( clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode() )
		{
			try( InputStream response = clientResponse.getEntity( InputStream.class ) )
			{
				feed = abdera.getParser().< Feed >parse( response ).getRoot();
			}
			catch( IOException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Failed to parse the delete-diffuser response into an Atom feed" + Constants.NEW_LINE );
				LOGGER.error( message.toString(), e );
				throw new IllegalStateException( message.toString(), e );
			}
		}
		else
		{
			final StringBuffer message = new StringBuffer();
			message.append( clientResponse.toString() );
			LOGGER.warn( message.toString() );
		}
		return new DeleteDiffuserResponse( feed );
	}

	/**
	 * Executes the specified method 
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param serializedObject A {@code byte[]} representation of the object of the {@link Class} that contains 
	 * the diffusive method being called
	 * @param serializer The {@link Serializer} used to serialize and deserialize the object
	 * @return An {@link ExecuteDiffuserResponse} object containing the information about the result and the 
	 * underlying Atom feed.
	 */
	public ExecuteDiffuserResponse executeMethod( final Class< ? > returnTypeClazz, 
							   					  final Class< ? > clazz, 
							   					  final String methodName,
							   					  final byte[] serializedObject,
							   					  final Serializer serializer )
	{
		// grab the serializer type
		final String serializerType = SerializerFactory.SerializerType.getSerializerName( serializer.getClass() );
		if( serializerType == null || serializerType.isEmpty() )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to execute method because specified serializer is invalid." );
			message.append( "  Class Name: " + clazz.getName() + Constants.NEW_LINE );
			message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
			message.append( "  Available Serializer Types: " + Constants.NEW_LINE );
			for( SerializerFactory.SerializerType type : SerializerFactory.SerializerType.values() )
			{
				message.append( "    " + type.getName() + " (" + type.getSerialzierClass().getName() + ")" + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString() );
			throw new IllegalArgumentException( message.toString() );
		}

		// construct the signature from the specified parameters
		final String signature = DiffuserId.createId( returnTypeClazz, clazz, methodName );
		
		// call the execute method
		return executeMethod( signature, serializedObject, clazz, serializerType );
	}

	/**
	 * 
	 * Executes the specified method 
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param serializedObject A {@code byte[]} representation of the object of the {@link Class} that contains 
	 * the diffusive method being called
	 * @param serializerType The name of the {@link Serializer} used to serialize and deserialize the object
	 * @return An {@link ExecuteDiffuserResponse} object containing the information about the result and the 
	 * underlying Atom feed.
	 */
	public ExecuteDiffuserResponse executeMethod( final Class< ? > returnTypeClazz, 
												  final Class< ? > clazz, 
												  final String methodName,
												  final byte[] serializedObject,
												  final String serializerType )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.createId( returnTypeClazz, clazz, methodName );
		
		// call the execute method
		return executeMethod( signature, serializedObject, clazz, serializerType );
	}
	
	/**
	 * Executes the specified method 
	 * @param signature a {@link DiffuserId} signature (which is different from a Java signature because it includes
	 * the return type) of the diffuser to use to execute the method
	 * @param serializedObject A {@code byte[]} representation of the object of the {@link Class} that contains 
	 * the diffusive method being called
	 * @param serializedObjectType The {@link Class} of the serialized object that contains the diffusive method
	 * @param serializerType The name of the {@link Serializer} used to serialize and deserialize the object
	 * @return An {@link ExecuteDiffuserResponse} object containing the information about the result and the 
	 * underlying Atom feed.
	 */
	public ExecuteDiffuserResponse executeMethod( final String signature, 
							   					  final byte[] serializedObject,
							   					  final Class< ? > serializedObjectType,
							   					  final String serializerType )
	{
		// grab the return type from the signature
		final String returnType = DiffuserId.parse( signature ).getReturnTypeClassName();
		
		// create the diffeser-execute request
		final ExecuteDiffuserRequest request = ExecuteDiffuserRequest.create( returnType,
																			  serializedObjectType.getName(), 
																			  serializedObject, 
																			  serializerType );

		return executeMethod( signature, request );
	}

	/**
	 * Executes the specified method 
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @param argumentValues The serialized value of each of the arguments passed to the diffusive method
	 * @param serializedObject A {@code byte[]} representation of the object of the {@link Class} that contains 
	 * the diffusive method being called
//	 * @param serializedObjectType The {@link Class} of the serialized object that contains the diffusive method
	 * @param serializerType The name of the {@link Serializer} used to serialize and deserialize the object
	 * @return An {@link ExecuteDiffuserResponse} object containing the information about the result and the 
	 * underlying Atom feed.
	 */
	public ExecuteDiffuserResponse executeMethod( final Class< ? > returnTypeClazz, 
							   					  final Class< ? > clazz, 
							   					  final String methodName,
							   					  final List< Class< ? > > argumentTypes, 
							   					  final List< byte[] > argumentValues, 
							   					  final byte[] serializedObject,
//							   					  final Class< ? > serializedObjectType,
							   					  final String serializerType )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.createId( returnTypeClazz, clazz, methodName, argumentTypes.toArray( new Class< ? >[ 0 ] ) );
		
		// call the execute method
//		return executeMethod( signature, argumentTypes, argumentValues, serializedObject, serializedObjectType, serializerType );
		return executeMethod( signature, argumentTypes, argumentValues, serializedObject, clazz, serializerType );
	}

	/**
	 * Executes the specified method 
	 * @param signature a {@link DiffuserId} signature (which is different from a Java signature because it includes
	 * the return type) of the diffuser to use to execute the method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @param argumentValues The serialized value of each of the arguments passed to the diffusive method
	 * @param serializedObject A {@code byte[]} representation of the object of the {@link Class} that contains 
	 * the diffusive method being called
	 * @param serializedObjectType The {@link Class} of the serialized object that contains the diffusive method
	 * @param serializerType The name of the {@link Serializer} used to serialize and deserialize the object
	 * @return An {@link ExecuteDiffuserResponse} object containing the information about the result and the 
	 * underlying Atom feed.
	 */
	public ExecuteDiffuserResponse executeMethod( final String signature, 
							   					  final List< Class< ? > > argumentTypes, 
							   					  final List< byte[] > argumentValues, 
							   					  final byte[] serializedObject,
							   					  final Class< ? > serializedObjectType,
							   					  final String serializerType )
	{
		// convert the argument types to argument type names
		final List< String > argumentTypeNames = convertArgumentTypes( argumentTypes );
		
		final String returnTypeClassName = DiffuserId.parse( signature ).getReturnTypeClassName();
		
		// create the diffeser-execute request
		final ExecuteDiffuserRequest request = ExecuteDiffuserRequest.create( returnTypeClassName,
																			  argumentTypeNames, 
																			  argumentValues, 
																			  serializedObjectType.getName(), 
																			  serializedObject, 
																			  serializerType );

		return executeMethod( signature, request );
	}
	
	/**
	 * Executes the specified method 
	 * @param signature a {@link DiffuserId} signature (which is different from a Java signature because it includes
	 * the return type) of the diffuser to use to execute the method
	 * @param request The {@link ExecuteDiffuserRequest} object containing the information needed to execute a diffusive method
	 * @return An Atom feed that contains a URI from which to obtain the result. 
	 */
	private ExecuteDiffuserResponse executeMethod( final String signature, final ExecuteDiffuserRequest request )
	{
		// create the URI to the diffuser with the specified signature
		final URI diffuserUri = UriBuilder.fromUri( baseUri.toString() ).path( signature ).build();
		
		// create the web resource for making the call, make the call to POST the create-request to the server
		final WebResource resource = client.resource( diffuserUri.toString() );
		final ClientResponse executeDiffuserResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).post( ClientResponse.class, request );
		
		// parse the response into an Atom feed object and return it
		Feed feed = null;
		try( InputStream response = executeDiffuserResponse.getEntity( InputStream.class ) )
		{
			feed = abdera.getParser().< Feed >parse( response ).getRoot();
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to parse the execute-diffuser response into an Atom feed" + Constants.NEW_LINE );
			message.append( "  Signature: " + signature + Constants.NEW_LINE );
			message.append( "  Request ID: " + request.getRequestId() + Constants.NEW_LINE );
			final DiffuserId diffuserId = DiffuserId.parse( signature );
			message.append( "  Class Name: " + diffuserId.getClassName() + Constants.NEW_LINE );
			message.append( "  Method Name: " + diffuserId.getMethodName() + Constants.NEW_LINE );
			message.append( "  Argument Type Names: " + Constants.NEW_LINE );
			for( String name : diffuserId.getArgumentTypeNames() )
			{
				message.append( "    " + name + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return new ExecuteDiffuserResponse( feed );
	}

	/**
	 * Requests the result of the {@code executeMethod(...)} request
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param requestId The request ID generated and returned after the method was executed
	 * @param serializer The {@link Serializer} used to serialize and deserialize the object
	 * @return The result object associated with the specified request ID
	 */
	public < T > T getResult( final Class< T > returnTypeClazz, 
							  final Class< ? > clazz, 
							  final String methodName,
							  final String requestId,
							  final Serializer serializer )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.createId( returnTypeClazz, clazz, methodName );

		return returnTypeClazz.cast( getResult( signature, requestId, serializer ) );
	}

	/**
	 * Requests the result of the {@code executeMethod(...)} request
	 * @param returnType The {@link Class} of the return type of the diffusive method
	 * @param clazz The {@link Class} containing the diffusive method 
	 * @param methodName The name of the diffusive method
	 * @param argumentTypes The {@link Class} for each of the formal method parameters of the diffusive method
	 * @param requestId The request ID generated and returned after the method was executed
	 * @param serializer The {@link Serializer} used to serialize and deserialize the object
	 * @return The result object associated with the specified request ID
	 */
	public < T > T getResult( final Class< T > returnTypeClazz, 
							  final Class< ? > clazz, 
							  final String methodName,
							  final List< Class< ? > > argumentTypes, 
							  final String requestId, 
							  final Serializer serializer )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.createId( returnTypeClazz, clazz, methodName, argumentTypes.toArray( new Class< ? >[ 0 ] ) );
		
		return returnTypeClazz.cast( getResult( signature, requestId, serializer ) );
	}

	/**
	 * Requests the result of the {@code executeMethod(...)} request
	 * @param signature a {@link DiffuserId} signature (which is different from a Java signature because it includes
	 * the return type) of the diffuser to use to execute the method
	 * @param requestId The request ID generated and returned after the method was executed
	 * @param serializer The {@link Serializer} used to serialize and deserialize the object
	 * @return The result object associated with the specified request ID
	 */
	public Object getResult( final String signature, final String requestId, final Serializer serializer )
	{
		final DiffuserId id = DiffuserId.parse( signature );
		
		// create the URI to the diffuser with the specified signature
		final URI diffuserUri = UriBuilder.fromUri( baseUri.toString() ).path( signature ).path( requestId ).build();
		
		// create the web resource for making the call, make the call to GET the result from the server
		final WebResource resource = client.resource( diffuserUri.toString() );
		final ClientResponse resultResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );

		if( resultResponse.getStatus() == Status.NO_CONTENT.getStatusCode() )
		{
			return null;
		}
		
		Feed feed = null;
		Object object = null;
		try( InputStream response = resultResponse.getEntity( InputStream.class ) )
		{
			// the response is an Atom feed
			feed = abdera.getParser().< Feed >parse( response ).getRoot();
			
			// grab the content from the entry and deserialize it
			final InputStream objectStream = feed.getEntries().get( 0 ).getContentStream();

			final Class< ? > returnType = id.getReturnTypeClazz();
			if( returnType.equals( void.class ) )
			{
				object = (Void)null;
			}
			else
			{
				object = serializer.deserialize( objectStream, returnType );
			}
		}
		catch( IOException e )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Failed to parse the execute-diffuser response into an Atom feed" + Constants.NEW_LINE );
			message.append( "  Signature: " + signature + Constants.NEW_LINE );
			message.append( "  Request ID: " + requestId + Constants.NEW_LINE );
			final DiffuserId diffuserId = DiffuserId.parse( signature );
			message.append( "  Class Name: " + diffuserId.getClassName() + Constants.NEW_LINE );
			message.append( "  Method Name: " + diffuserId.getMethodName() + Constants.NEW_LINE );
			message.append( "  Argument Type Names: " + Constants.NEW_LINE );
			for( String name : diffuserId.getArgumentTypeNames() )
			{
				message.append( "    " + name + Constants.NEW_LINE );
			}
			LOGGER.error( message.toString(), e );
			throw new IllegalArgumentException( message.toString(), e );
		}
		return object;
	}
	
	public boolean isComplete( final String resultId )
	{
		// create the URI to the diffuser with the specified signature
		final URI resultUri = UriBuilder.fromUri( baseUri.toString() ).path( resultId ).build();
		
		// create the web resource for making the call, make the call to GET the result from the server
		final WebResource resource = client.resource( resultUri.toString() );
		final ClientResponse resultResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );

		boolean isComplete = false;
		if( resultResponse.getStatus() == Status.NO_CONTENT.getStatusCode() )
		{
			isComplete = false;
		}
		else if( resultResponse.getStatus() == Status.OK.getStatusCode() )
		{
			isComplete = true;
		}
		else
		{
			// throw exception
		}
		return isComplete;
	}

	/*
	 * Converts a {@link List} of {@link Class} representing the argument types into a {@link List}
	 * of {@link String} representing the names of the argument types. For example, if an argument is
	 * of type {@link String}, then the argument type name will be {@link java.lang.String}.
	 * @param argumentTypes The {@link List} of the argument types
	 * @return a {@link List} of the argument type names
	 */
	private static List< String > convertArgumentTypes( final List< Class< ? > > argumentTypes )
	{
		// convert the argument types to argument type names
		final List< String > argumentTypeNames = new ArrayList<>();
		for( Class< ? > clazz : argumentTypes )
		{
			argumentTypeNames.add( clazz.getName() );
		}
		return argumentTypeNames;
	}

	/*
	 * Converts an {@link Class}[] of argument types into an {@link String}[] of argument type names
	 * @param argumentTypes The array of argument types
	 * @return an array of argument type names
	 */
	private static String[] convertArgumentTypes( final Class< ? >[] argumentTypes )
	{
		// convert the argument types to argument type names
		final String[] argumentTypeNames = new String[ argumentTypes.length ];
		for( int i = 0; i < argumentTypes.length; ++i )
		{
			argumentTypeNames[ i ] = argumentTypes[ i ].getName();
		}
		return argumentTypeNames;
	}

	public static void main( String[] args ) throws URISyntaxException, InterruptedException
	{
		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.DEBUG );

		// create the Java API client to interact with the Restful Diffuser Manager Server
		final RestfulDiffuserManagerClient managerClient = new RestfulDiffuserManagerClient( "http://localhost:8182/diffusers" );
		
		final Bean bean = new Bean();
		
		//
		// create a diffuser
		//
		CreateDiffuserResponse createResponse = managerClient.createDiffuser( new ArrayList< URI >(), String.class, bean.getClass(), "getA" );
		System.out.println( "Create getA: " + createResponse.toString() + Constants.NEW_LINE );
		
		// and another
		createResponse = managerClient.createDiffuser( new ArrayList< URI >(), bean.getClass(), "setA", new Class< ? >[] { String.class } );
		System.out.println( "Create setA: " + createResponse.toString() + Constants.NEW_LINE );

		//
		// list the diffusers
		//
		ListDiffuserResponse listResponse = managerClient.getDiffuserList();
		System.out.println( listResponse.toString() + Constants.NEW_LINE );
		
		//
		// delete a diffuser
		//
		DeleteDiffuserResponse deleteResponse = managerClient.deleteDiffuser( bean.getClass(), "setA", new Class< ? >[] { String.class } );
		System.out.println( "Delete setA: " + deleteResponse.toString() + Constants.NEW_LINE );

		//
		// list the diffusers
		//
		listResponse = managerClient.getDiffuserList();
		System.out.println( listResponse.toString() + Constants.NEW_LINE );
		
		//
		// execute some of the methods and grab their results
		//
		final Serializer serializer = new XmlPersistenceSerializer();
		
		// write the object to a byte array and the reconstitute the object
		ExecuteDiffuserResponse executeResponse = null;
		try( final ByteArrayOutputStream out = new ByteArrayOutputStream() )
		{
			serializer.serialize( bean, out );
			out.flush();
			executeResponse = managerClient.executeMethod( String.class, bean.getClass(), "getA", out.toByteArray(), serializer );
			System.out.println( executeResponse.toString() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		// grab the result of the call
		final DiffuserId diffuserId = DiffuserId.parse( executeResponse.getSignature() );
		final String methodName = diffuserId.getMethodName();
		final Class< ? > clazz = diffuserId.getClazz();
		String result = null;
		do
		{
			result = managerClient.getResult( String.class, clazz, methodName, executeResponse.getRequestId(), serializer );
			System.out.println( result );
			Thread.sleep( 500 );
		}
		while( result == null );
	}
}
