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
package org.microtitan.diffusive.diffuser.restful.resources;

import java.util.regex.Pattern;

import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.DiffuserSignature;

/**
 * Represents the components that make up the execute result ID used to retrieve results. The result ID
 * is composed of the signature ({@link DiffuserSignature}) and the request ID generated and returned by the execute
 * method. This class can be used to create and parse result IDs and get their component parts.
 * 
 * @author Robert Philipp
 */
public class ResultId {

	public static final String PATH_SEPARATOR = "/";
	
	// for the hashCode method
	private volatile int hashCode;
	
	private final String signature;
	private final String requestId;
	
	/**
	 * Constructs the {@link ResultId} object holding the component parts of the ID
	 * @param signature The signature ({@link DiffuserSignature})
	 * @param requestId The request ID generated and returned by the execute method
	 */
	public ResultId( final String signature, final String requestId )
	{
		this.signature = signature;
		this.requestId = requestId;
		
		hashCode = 0;
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
	
	/**
	 * @return A string representation of the result ID
	 */
	public String getResultId()
	{
		return create( signature, requestId );
	}

	/**
	 * Creates a string representation of the result ID using the specified signature and request ID
	 * @param signature The signature ({@link DiffuserSignature})
	 * @param requestId The request ID generated and returned by the execute method
	 * @return a string representation of the result ID
	 */
	public static final String create( final String signature, final String requestId )
	{
		return signature + PATH_SEPARATOR + requestId;
	}
	
	/**
	 * Parses the specified string representation of the resultId into a {@link ResultId} object
	 * @param resultId The string representation of the resultId
	 * @return a {@link ResultId} object based on the specified string representation 
	 * @throws IllegalArgumentException
	 */
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( final Object object )
	{
		if( !(object instanceof ResultId) )
		{
			return false;
		}
		
		final ResultId resultId = (ResultId)object;
		if( signature.equals( resultId.signature ) && requestId.equals( resultId.requestId ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sun.java.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = hashCode;
		if( result == 0 )
		{
			result = 17;
			result = 31 * result + ( signature == null ? 0 : signature.hashCode() );
			result = 31 * result + ( requestId == null ? 0 : requestId.hashCode() );
			hashCode = result;
		}
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getResultId();
	}
}
