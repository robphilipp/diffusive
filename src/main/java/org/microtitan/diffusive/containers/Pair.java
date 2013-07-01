/*
 * Copyright 2012 Robert Philipp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.microtitan.diffusive.containers;

import org.freezedry.persistence.copyable.Copyable;

/**
 * Simple class to represent the concept of a pair. 
 * 
 * @author Robert Philipp
 */
public class Pair< F, S > implements Copyable< Pair< F, S > > {

	private volatile int hashCode;

	/*
	 * The first of the pair
	 */
	private F first;
	
	/*
	 * The second of the pair
	 */
	private S second;
	
	/**
	 * Constructor for creating the pair.
	 * @param first The first of the pair
	 * @param second The second of the pair
	 */
	public Pair( final F first, final S second )
	{
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Default constructor
	 */
	public Pair() {}
	
	/**
	 * Copy constructor
	 * @param pair The pair to copy
	 */
	public Pair( final Pair< F, S > pair )
	{
		this.first = pair.first;
		this.second = pair.second;
	}
	
	/**
	 * Allows the setting of the pair in one call 
	 * @param first The first of the pair
	 * @param second The second of the pair
	 */
	public void set( final F first, final S second )
	{
		setFirst( first );
		setSecond( second );
	}
	
	/**
	 * Returns the first of the pair
	 * @return the first of the pair
	 */
	public F getFirst()
	{
		return first;
	}
	
	/**
	 * Sets the value of the first
	 * @param first The value with which to set the first
	 */
	public void setFirst( final F first )
	{
		this.first = first;
		hashCode = 0;
	}
	
	/**
	 * Returns the second of the pair
	 * @return the second of the pair
	 */
	public S getSecond()
	{
		return second;
		
	}
	
	/**
	 * Sets the value fo the second of the pair
	 * @param second The value with which to set the second
	 */
	public void setSecond( final S second )
	{
		this.second = second;
		hashCode = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( final Object obj )
	{
		// same object
		if( obj == this )
		{
			return true;
		}
		
		// is it the same type, this also catches if obj is null
		if( !( obj instanceof Pair ) )
		{
			return false;
		}
		
		// cast
		final Pair< ?, ?> pair = (Pair< ?, ?>)obj;
		
		return ( first.equals( pair.first ) && second.equals( pair.second ) );
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
			result = 31 * result + first.hashCode();
			result = 31 * result + second.hashCode();
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
		final StringBuffer asString = new StringBuffer( "[" );
		asString.append( (first != null ? first.toString() : "[null]" ) ).append( ", " );
		asString.append( (second != null ? second.toString() : "[null]" ) ).append( "]" );
		return asString.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.freezedry.persistence.copyable.Copyable#getCopy()
	 */
	@Override
	public Pair< F, S > getCopy()
	{
		return new Pair< F, S >( this );
	}
}
