package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.AbstractDiffuser;
import org.microtitan.diffusive.diffuser.LocalDiffuser;
import org.microtitan.diffusive.diffuser.serializer.Serializer;

public class RestfulDiffuser extends AbstractDiffuser {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuser.class );
	
	// used to serialize objects for making requests across the network
	private Serializer serializer;
	private List< URI > clientEndpoints;
	
	/**
	 * 
	 * @param serializer The object that converts the object into and out of the form that is transmitted across
	 * the wire.
	 * @param clientEndpoints The URIs at which other diffusers are located, which this diffuser can call.
	 */
	public RestfulDiffuser( final Serializer serializer, final List< URI > clientEndpoints )
	{
		this.serializer = serializer;
		this.clientEndpoints = clientEndpoints;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.Diffuser#runObject(boolean, java.lang.Object, java.lang.String, java.lang.Object[])
	 */
	@Override
	public synchronized Object runObject( final boolean isRemoteCall, final Object object, final String methodName, final Object... arguments )
	{
		// TODO develop execution-performance based approach to determining whether to run locally or remotely, as well as the current approach.
		Object result = null;
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
			result = new LocalDiffuser().runObject( false, object, methodName, arguments );
		}
		else
		{
			// here we look up an endpoint, and send to code to the endpoint, use the restful diffuser client code
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
