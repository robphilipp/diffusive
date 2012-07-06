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
	public Object runObject( final boolean isRemoteCall, final Object object, final String methodName, final Object... arguments )
	{
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "******RESTful Diffuser*******" + Constants.NEW_LINE );
//		buffer.append( "Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
//		buffer.append( "Endpoints: " + clientEndpoints + Constants.NEW_LINE );
//		// TODO Auto-generated method stub
//		// add the jersey client stuff in here...create a calculator, send the object to the calculator
//		return buffer.toString();
		// TODO need to figure out how to know if this is supposed to go out to an endpoint, or if it
		// is now at the endpoint and needs to run locally.
//		return new LocalDiffuser().runObject( false, object, methodName, arguments );

		// TODO need to determine whether to run locally, or continue to diffuse to other endpoints.
		// must develop the logic to do this.
		Object result = null;
		if( isRemoteCall ) // and it meets other conditions for local execution
		{
			result = new LocalDiffuser().runObject( false, object, methodName, arguments );
		}
		else
		{
			// here we look up an endpoint, and send to code to the endpoint, use the restful diffuser client code
		}
		return result;
	}
	
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
