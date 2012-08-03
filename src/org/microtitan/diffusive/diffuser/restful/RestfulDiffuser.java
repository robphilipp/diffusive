package org.microtitan.diffusive.diffuser.restful;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.AbstractDiffuser;
import org.microtitan.diffusive.diffuser.LocalDiffuser;
import org.microtitan.diffusive.diffuser.restful.client.RestfulDiffuserManagerClient;
import org.microtitan.diffusive.diffuser.restful.response.ExecuteDiffuserResponse;
import org.microtitan.diffusive.diffuser.serializer.Serializer;
import org.microtitan.diffusive.diffuser.serializer.SerializerFactory;

public class RestfulDiffuser extends AbstractDiffuser {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuser.class );
	
	// used to serialize objects for making requests across the network
	private final Serializer serializer;
	private final List< URI > clientEndpoints;
	private final List< URI > classPaths;
	
	// TODO the diffuser should be handed a strategy instead of a list of end-points
	
	/**
	 * 
	 * @param serializer The object that converts the object into and out of the form that is transmitted across
	 * the wire.
	 * @param clientEndpoints The URIs at which other diffusers are located, which this diffuser can call.
	 */
	public RestfulDiffuser( final Serializer serializer, final List< URI > clientEndpoints, final List< URI > classPaths )
	{
		this.serializer = serializer;
		this.clientEndpoints = clientEndpoints;
		this.classPaths = classPaths;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(boolean, java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public < T > T runObject( final boolean isRemoteCall, final Class< T > returnType, final Object object, final String methodName, final Object... arguments )
	{
		// TODO develop execution-performance based approach to determining whether to run locally or remotely, as well as the current approach.
		T result = null;
		if( isRemoteCall || clientEndpoints == null || clientEndpoints.isEmpty() ) // and it meets other conditions for local execution
		{
			if( LOGGER.isInfoEnabled() )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Called " + RestfulDiffuser.class.getName() + "runObject(...) method." + Constants.NEW_LINE );
				message.append( "Using " + LocalDiffuser.class.getName() + " because: " );
				if( isRemoteCall )
				{
					message.append( "call to runObject(...) came from a remote call." + Constants.NEW_LINE );
				}
				else if( clientEndpoints == null || clientEndpoints.isEmpty() )
				{
					message.append( "RESTful diffuser was not assigned any client end-points." + Constants.NEW_LINE );
				}
			}
			
			// execute the method on the local diffuser
			result = new LocalDiffuser().runObject( false, returnType, object, methodName, arguments );
		}
		else
		{
			// create the client manager for the first endpoint in the list
			// TODO develop a better method for picking an endpoint
			final URI endpoint = clientEndpoints.get( 0 );
			final RestfulDiffuserManagerClient client = new RestfulDiffuserManagerClient( endpoint );

			// create the diffuser on the server
			final int numArguments = (arguments == null ? 0 : arguments.length);
			final Class< ? >[] argumentTypes = new Class< ? >[ numArguments ];
			for( int i = 0; i < numArguments; ++i )
			{
				argumentTypes[ i ] = arguments[ i ].getClass();
			}
			/*final CreateDiffuserResponse response = */
			client.createDiffuser( classPaths, returnType, object.getClass(), methodName, argumentTypes );
			
			// execute the method on the diffuser
			ExecuteDiffuserResponse executeResponse = null;
			try( final ByteArrayOutputStream out = new ByteArrayOutputStream() )
			{
				// serialize the object into the byte[] output stream and flush it
				serializer.serialize( object, out );
				out.flush();
				
				//
				// call the client to execute the method on the object
				//
				// we need to serialize the object containing the method we are calling, and then we need to serialize each
				// of the arguments passed into the method.
				if( numArguments == 0 )
				{
					executeResponse = client.executeMethod( returnType, object.getClass(), methodName, out.toByteArray(), serializer );
				}
				else
				{
					// serialize the argument values
					final List< byte[] > serializedArgs = new ArrayList<>();
					for( Object argument : arguments )
					{
						try( final ByteArrayOutputStream outArg = new ByteArrayOutputStream() )
						{
							serializer.serialize( argument, outArg );
							outArg.flush();
							
							// add the byte[] to the list of serialized arguments
							serializedArgs.add( outArg.toByteArray() );
						}
						catch( IOException e )
						{
							final StringBuffer message = new StringBuffer();
							message.append( "I/O error occured attempting to flush the byte[] output stream holding a serialized argument in" + Constants.NEW_LINE );
							message.append( "preparation for calling the execute(...) method on the client." + Constants.NEW_LINE );
							message.append( "  Client Endpoint: " + endpoint.toString() + Constants.NEW_LINE );
							message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
							message.append( "  Argument Value: " + argument + Constants.NEW_LINE );
							message.append( "  Argument Type: " + argument.getClass().getName() + Constants.NEW_LINE );
							message.append( "  Argument Types: " + Constants.NEW_LINE );
							for( int i = 0; i < numArguments; ++i )
							{
								message.append( "    " + argumentTypes[ i ].getName() + Constants.NEW_LINE );
							}
							message.append( "  Return Type: " + returnType.getName() + Constants.NEW_LINE );
							message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
							message.append( "  Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
							
							LOGGER.error( message.toString() );
							throw new IllegalArgumentException( message.toString() );
						}
					}
					
					final List< Class< ? > > argTypes = Arrays.asList( argumentTypes );
					final String serializerName = SerializerFactory.getSerializerName( serializer.getClass() );
					executeResponse = client.executeMethod( returnType, object.getClass(), methodName, argTypes, serializedArgs, out.toByteArray(), serializerName );
				}
			}
			catch( IOException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "I/O error occured attempting to flush the byte[] output stream holding the serialized object in" + Constants.NEW_LINE );
				message.append( "preparation for calling the execute(...) method on the client." + Constants.NEW_LINE );
				message.append( "  Client Endpoint: " + endpoint.toString() + Constants.NEW_LINE );
				message.append( "  Method Name: " + methodName + Constants.NEW_LINE );
				message.append( "  Argument Types: " + (numArguments == 0 ? "[none]" : "" ) + Constants.NEW_LINE );
				for( int i = 0; i < numArguments; ++i )
				{
					message.append( "    " + argumentTypes[ i ].getName() + Constants.NEW_LINE );
				}
				message.append( "  Return Type: " + returnType.getName() + Constants.NEW_LINE );
				message.append( "  Containing Class: " + object.getClass().getName() + Constants.NEW_LINE );
				message.append( "  Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
				
				LOGGER.error( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}

			// ask for the result, which blocks until the result returns
			final DiffuserId diffuserId = DiffuserId.parse( executeResponse.getSignature() );
			final Class< ? > clazz = diffuserId.getClazz();

			result = client.getResult( returnType, clazz, methodName, executeResponse.getRequestId(), serializer );
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Serializer: " + serializer.toString() + Constants.NEW_LINE );
		buffer.append( "Client Endpoints: " + Constants.NEW_LINE );
		for( URI uri : clientEndpoints )
		{
			buffer.append( "  " + uri.toString() + Constants.NEW_LINE );
		}
		return buffer.toString();
	}
}
