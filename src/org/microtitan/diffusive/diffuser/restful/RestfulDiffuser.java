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
	
	@Override
	public Object runObject( final Object object, final String methodName, final Object... arguments )
	{
//		final StringBuffer buffer = new StringBuffer();
//		buffer.append( "******RESTful Diffuser*******" + Constants.NEW_LINE );
//		buffer.append( "Serializer: " + serializer.getClass().getName() + Constants.NEW_LINE );
//		buffer.append( "Endpoints: " + clientEndpoints + Constants.NEW_LINE );
//		// TODO Auto-generated method stub
//		// add the jersey client stuff in here...create a calculator, send the object to the calculator
//		return buffer.toString();
		return new LocalDiffuser().runObject( object, methodName, arguments );
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
