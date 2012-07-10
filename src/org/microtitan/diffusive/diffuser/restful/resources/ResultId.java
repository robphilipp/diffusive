package org.microtitan.diffusive.diffuser.restful.resources;

import java.util.regex.Pattern;

import org.microtitan.diffusive.Constants;

public class ResultId {

	public static final String PATH_SEPARATOR = "/";
	private final String signature;
	private final String requestId;
	
	public ResultId( final String signature, final String requestId )
	{
		this.signature = signature;
		this.requestId = requestId;
	}

	/**
	 * @return the signature
	 */
	public String getSignature()
	{
		return signature;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId()
	{
		return requestId;
	}
	
	public String getResultId()
	{
		return create( signature, requestId );
	}

	public static final String create( final String signature, final String requestId )
	{
		return signature + PATH_SEPARATOR + requestId;
	}
	
	public static final ResultId parse( final String resultId )
	{
		final String[] elements = resultId.split( Pattern.quote( PATH_SEPARATOR ) );
		if( elements.length != 2 )
		{
			final StringBuffer message = new StringBuffer();
			message.append( "Error parsing result ID. Specified result ID is invalid." + Constants.NEW_LINE );
			message.append( "  Specified result ID: " + resultId );
			throw new IllegalArgumentException( message.toString() );
		}
		return new ResultId( elements[ 0 ], elements[ 1 ] );
	}
}
