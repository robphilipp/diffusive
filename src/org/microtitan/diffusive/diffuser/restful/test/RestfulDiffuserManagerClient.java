package org.microtitan.diffusive.diffuser.restful.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.CreateDiffuserRequest;
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
import org.microtitan.diffusive.diffuser.restful.ExecuteDiffuserRequest;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;
import org.microtitan.diffusive.diffuser.serializer.XmlPersistenceSerializer;
import org.microtitan.diffusive.tests.Bean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestfulDiffuserManagerClient {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerClient.class );
	
	private URI baseUri;
	private final Abdera abdera;
	private final Client client;
	
	public RestfulDiffuserManagerClient( final URI baseUri )
	{
		this.baseUri = baseUri;

		// atom parser/create
		this.abdera = AbderaFactory.getInstance();

		// create the Jersey RESTful client
		this.client = Client.create();


	}
	
	public RestfulDiffuserManagerClient( final String baseUri )
	{
		this( URI.create( baseUri ) );
	}
	
	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param argumentTypes
	 * @return
	 */
	public Feed createDiffuser( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		// convert the argument types to argument type names
		final String[] argumentTypeNames = convertArgumentTypes( argumentTypes ); 
		
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final CreateDiffuserRequest request = CreateDiffuserRequest.create( clazz.getName(), methodName, argumentTypeNames );
		
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
		return feed;
	}
	
	/**
	 * 
	 * @return
	 */
	public Feed getDiffuserList()
	{
		// create the web resource for making the call
		final WebResource resource = client.resource( baseUri.toString() );

		final ClientResponse clientResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );
		
		Feed feed = null;
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
		return feed;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param argumentTypes
	 * @return
	 */
	public Feed deleteDiffuser( final Class< ? > clazz, final String methodName, final Class< ? >...argumentTypes )
	{
		return deleteDiffuser( DiffuserId.create( clazz, methodName, argumentTypes ) );
	}
	
	/**
	 * 
	 * @param signature
	 * @return
	 */
	public Feed deleteDiffuser( final String signature )
	{
		// create the URI to the diffuser with the specified signature
		final URI diffuserUri = UriBuilder.fromUri( baseUri.toString() ).path( signature ).build();
		
		// create the web resource for making the call
		final WebResource resource = client.resource( diffuserUri.toString() );
		
		// make the call to delete the resource
		final ClientResponse clientResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).delete( ClientResponse.class );
		
		Feed feed = null;
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
		return feed;
	}

	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param serializedObject
	 * @param serializer
	 * @return
	 */
	public Feed executeMethod( final Class< ? > clazz, 
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
		final String signature = DiffuserId.create( clazz, methodName );
		
		// call the execute method
		return executeMethod( signature, serializedObject, clazz, serializerType );
	}
	
	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param serializedObject
	 * @param serializedObjectType
	 * @param serializerType
	 * @return
	 */
	public Feed executeMethod( final Class< ? > clazz, 
							   final String methodName, 
							   final byte[] serializedObject,
							   final String serializerType )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.create( clazz, methodName );
		
		// call the execute method
		return executeMethod( signature, serializedObject, clazz, serializerType );
	}
	
	/**
	 * 
	 * @param signature
	 * @param serializedObject
	 * @param serializedObjectType
	 * @param serializerType
	 * @return
	 */
	public Feed executeMethod( final String signature, 
							   final byte[] serializedObject,
							   final Class< ? > serializedObjectType,
							   final String serializerType )
	{
		// create the diffeser-execute request
		final ExecuteDiffuserRequest request = ExecuteDiffuserRequest.create( serializedObjectType.getName(), 
																			  serializedObject, 
																			  serializerType );

		return executeMethod( signature, request );
	}
	
	/**
	 * 
	 * @param clazz
	 * @param methodName
	 * @param argumentTypes
	 * @param argumentValues
	 * @param serializedObject
	 * @param serializedObjectType
	 * @param serializerType
	 * @return
	 */
	public Feed executeMethod( final Class< ? > clazz, 
							   final String methodName, 
							   final List< Class< ? > > argumentTypes, 
							   final List< byte[] > argumentValues, 
							   final byte[] serializedObject,
							   final Class< ? > serializedObjectType,
							   final String serializerType )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.create( clazz, methodName, argumentTypes.toArray( new Class< ? >[ 0 ] ) );
		
		// call the execute method
		return executeMethod( signature, argumentTypes, argumentValues, serializedObject, serializedObjectType, serializerType );
	}
	
	/**
	 * 
	 * @param signature
	 * @param argumentTypes
	 * @param argumentValues
	 * @param serializedObject
	 * @param serializedObjectType
	 * @param serializerType
	 * @return
	 */
	public Feed executeMethod( final String signature, 
							   final List< Class< ? > > argumentTypes, 
							   final List< byte[] > argumentValues, 
							   final byte[] serializedObject,
							   final Class< ? > serializedObjectType,
							   final String serializerType )
	{
		// convert the argument types to argument type names
		final List< String > argumentTypeNames = convertArgumentTypes( argumentTypes );
		
		// create the diffeser-execute request
		final ExecuteDiffuserRequest request = ExecuteDiffuserRequest.create( argumentTypeNames, 
																			  argumentValues, 
																			  serializedObjectType.getName(), 
																			  serializedObject, 
																			  serializerType );

		return executeMethod( signature, request );
	}
	
	/**
	 * 
	 * @param signature
	 * @param request
	 * @return
	 */
	private Feed executeMethod( final String signature, final ExecuteDiffuserRequest request )
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
		return feed;
	}
	
	public Object getResult( final Class< ? > clazz, 
							 final String methodName, 
							 final String requestId,
							 final Serializer serializer )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.create( clazz, methodName );

		return getResult( signature, requestId, serializer );
	}
	
	public Object getResult( final Class< ? > clazz, 
							 final String methodName, 
							 final List< Class< ? > > argumentTypes, 
							 final String requestId, 
							 final Serializer serializer )
	{
		// construct the signature from the specified parameters
		final String signature = DiffuserId.create( clazz, methodName, argumentTypes.toArray( new Class< ? >[ 0 ] ) );
		
		return getResult( signature, requestId, serializer );
	}
	
	public Object getResult( final String signature, final String requestId, final Serializer serializer )
	{
		final DiffuserId id = DiffuserId.parse( signature );
		
		// create the URI to the diffuser with the specified signature
		final URI diffuserUri = UriBuilder.fromUri( baseUri.toString() ).path( signature ).path( requestId ).build();
		
		// create the web resource for making the call, make the call to GET the result from the server
		final WebResource resource = client.resource( diffuserUri.toString() );
		final ClientResponse resultResponse = resource.accept( MediaType.APPLICATION_ATOM_XML ).get( ClientResponse.class );

		Feed feed = null;
		Object object = null;
		try( InputStream response = resultResponse.getEntity( InputStream.class ) )
		{
			// the response is an Atom feed
			feed = abdera.getParser().< Feed >parse( response ).getRoot();
			
			// grab the content from the entry and deserialize it
			feed.getEntries().get( 0 ).getContentStream();
			
			// TODO id contains the wrong class information....here we need the return type, not the class on
			// which the method is called.
			object = serializer.deserialize( feed.getEntries().get( 0 ).getContentStream(), id.getClazz() );
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

	/**
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

	public static void main( String[] args ) throws URISyntaxException
	{
		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.DEBUG );

		// create the Java API client to interact with the Restful Diffuser Manager Server
		final RestfulDiffuserManagerClient managerClient = new RestfulDiffuserManagerClient( "http://localhost:8182/diffusers" );
		
		final Bean bean = new Bean();
		
		//
		// create a diffuser
		//
		Feed feed = managerClient.createDiffuser( bean.getClass(), "getA" );
		System.out.println( "Create getA: " + feed.toString() );
		
		// and another
		feed = managerClient.createDiffuser( bean.getClass(), "setA", new Class< ? >[] { String.class } );
		System.out.println( "Create setA: " + feed.toString() );

		//
		// list the diffusers
		//
		feed = managerClient.getDiffuserList();
		System.out.println( "Get Diffuser List: " + feed.toString() );
		for( Entry entry : feed.getEntries() )
		{
			System.out.println( "  " + entry.getId() );
		}
		
		//
		// delete a diffuser
		//
		feed = managerClient.deleteDiffuser( bean.getClass(), "setA", new Class< ? >[] { String.class } );
		System.out.println( "Delete setA: " + feed.toString() );

		//
		// list the diffusers
		//
		feed = managerClient.getDiffuserList();
		System.out.println( "Get Diffuser List: " + feed.toString() );
		for( Entry entry : feed.getEntries() )
		{
			System.out.println( "  " + entry.getId() );
		}
		
		//
		// execute some of the methods
		//
		final Serializer serializer = new XmlPersistenceSerializer();
		
		// write the object to a byte array and the reconstitute the object
		try( final ByteArrayOutputStream out = new ByteArrayOutputStream() )
		{
			serializer.serialize( bean, out );
			out.flush();
			feed = managerClient.executeMethod( bean.getClass(), "getA", out.toByteArray(), serializer );
			System.out.println( "Execute getA: " + feed.toString() );
//			final String object = new String( bytes );
//			System.out.println( "StringWriter: " + object );
//			System.out.println( "StringWriter: " + object.getBytes() );
//			
//			final TestClassA desA = serializer.deserialize( new ByteArrayInputStream( bytes ), TestClassA.class );
//			System.out.println( desA.toString() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		feed.getId().toURI().getPath();
		System.out.println( feed.getLink( Link.REL_SELF ).getHref().toURI().toString() );
		
		final String requestId = feed.getEntries().get( 0 ).getContent();
		System.out.println( "Request ID: " + requestId );
		
		final List< String > idParts = Arrays.asList( requestId.split( "/" ) );
		/*final Object object = */managerClient.getResult( bean.getClass(), "getA", idParts.get( 1 ), serializer );
	}
}
