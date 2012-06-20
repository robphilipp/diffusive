package org.microtitan.diffusive.diffuser.restful.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.AbderaFactory;
import org.microtitan.diffusive.diffuser.restful.DiffuserCreateRequest;
import org.microtitan.diffusive.diffuser.restful.DiffuserId;
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
		// create the web resource for making the call
		final WebResource resource = client.resource( baseUri.toString() );

		// convert the argument types to argument type names
		final String[] argumentTypeNames = new String[ argumentTypes.length ];
		for( int i = 0; i < argumentTypes.length; ++i )
		{
			argumentTypeNames[ i ] = argumentTypes[ i ].getName();
		}
		
		// construct the request to create the diffuser for the specific signature (class, method, arguments)
		final DiffuserCreateRequest request = DiffuserCreateRequest.create( clazz.getName(), methodName, argumentTypeNames );
		
		// make the call to PUT the create-request to the server
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

	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
//		Logger.getRootLogger().setLevel( Level.DEBUG );

		// atom parser/create
		final Abdera abdera = AbderaFactory.getInstance();

		// create the Jersey RESTful client
		final Client client = Client.create();

		final RestfulDiffuserManagerClient managerClient = new RestfulDiffuserManagerClient( "http://localhost:8182/diffusers" );
		
		//
		// create a diffuser
		//
		Feed feed = managerClient.createDiffuser( Bean.class, "getA" );
		System.out.println( "Create getA: " + feed.toString() );
		
		// and another
		feed = managerClient.createDiffuser( Bean.class, "setA", new Class< ? >[] { String.class } );
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
		feed = managerClient.deleteDiffuser( Bean.class, "setA", new Class< ? >[] { String.class } );
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
	}
}
