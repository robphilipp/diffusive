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
import org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy;
import org.microtitan.diffusive.diffuser.strategy.load.DiffuserLoadCalc;

/**
 * A diffuser that uses REST to diffuser methods to remote RESTful diffusers, or runs the task locally.
 * To diffuse to a remote diffuser, this diffuser instantiates a RESTful client 
 * ({@link RestfulDiffuserManagerClient}) that is then used to send the task to the remote server.
 *  
 * @author Robert Philipp
 */
public class RestfulDiffuser extends AbstractDiffuser {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuser.class );
	
	// used to serialize objects for making requests across the network
	private final Serializer serializer;
	private final DiffuserStrategy strategy;
	private final List< URI > classPaths;
	private final double loadThreshold;
	
	/**
	 * Constructs the RESTful diffuser that runs methods either locally or sends them on to a remote
	 * RESTful diffuser.
	 * @param serializer The object that converts the object into and out of the form that is transmitted across
	 * the wire.
	 * @param strategy The diffuser strategy that determines which end-point will get called next
	 * @param classPaths The class paths on a remote server needed for loading classes that aren't 
	 * locally available 
	 * @param loadThreshold The load threshold above which the {@link RestfulDiffuser} will forward execution
	 * of the task to remote diffuser. The load threshold must be in the interval {@code (0.0, infinity]}
	 * @see DiffuserLoadCalc
	 */
	public RestfulDiffuser( final Serializer serializer, 
							final DiffuserStrategy strategy, 
							final List< URI > classPaths,
							final double loadThreshold )
	{
		this.serializer = serializer;
		this.strategy = strategy;
		this.classPaths = classPaths;
		
		if( loadThreshold <= 0.0 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "The load threshold must be greater than 0.0" + Constants.NEW_LINE );
			message.append( "  Specified Load Threshold: " + loadThreshold );
			throw new IllegalArgumentException( message.toString() );
		}
		this.loadThreshold = loadThreshold;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(boolean, java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public < T > T runObject( final double load, final Class< T > returnType, final Object object, final String methodName, final Object... arguments )
	{
		// if the load is less than the threshold, then we can compute this task locally, or if there are no
		// end-points to which to diffuse the task further. Otherwise, the task is diffused to an end-point
		// based on the strategy that selects the end-point
		T result = null;
		if( load < loadThreshold || strategy.isEmpty() )
		{
			if( LOGGER.isInfoEnabled() )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Called " + RestfulDiffuser.class.getName() + "runObject(...) method." + Constants.NEW_LINE );
				message.append( "Using " + LocalDiffuser.class.getName() + " because: " );
				if( load < loadThreshold )
				{
					message.append( "because the load (" + load + ") was less than the load threshold (" + loadThreshold + ")." + Constants.NEW_LINE );
				}
				else if( strategy.isEmpty() )
				{
					message.append( "RESTful diffuser was not assigned any client end-points." + Constants.NEW_LINE );
				}
			}
			
			// execute the method on the local diffuser
			result = new LocalDiffuser().runObject( load, returnType, object, methodName, arguments );
		}
		else
		{
			// create the client manager for the next end point from the strategy
			final URI endpoint = strategy.getEndpoint();
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
			// try argument is supposed to close the ByteArrayOutputStream, but it seems it doesn't.
			// yet, at the same time, closing a ByteArrayOutputStream has no effect anyway, so we can
			// safely suppress the warning.
			try( @SuppressWarnings( "resource" ) final ByteArrayOutputStream out = new ByteArrayOutputStream() )
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
		buffer.append( "Strategy: " + strategy.getClass().getName() + Constants.NEW_LINE );
		buffer.append( strategy.toString() );
		return buffer.toString();
	}
}
