package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.List;

import org.microtitan.diffusive.Constants;


/**
 * Abstract class that deals with the {@link List} of end-point {@link URI}. Implementing classes
 * need to implement the {@link #getEndpoint()} method from the {@link DiffuserStrategy} interface.
 * 
 * @author Robert Philipp
 */
public abstract class AbstractDiffuserStrategy implements DiffuserStrategy {

	private final List< URI > endpoints;
	
	protected AbstractDiffuserStrategy( final List< URI > endpoints )
	{
		this.endpoints = endpoints;
	}
	
	public final int getNumEndpoints()
	{
		return endpoints.size();
	}
	
	public final URI getEndpoint( final int index )
	{
		return endpoints.get( index );
	}
	
	public final boolean addEndpoint( final URI endpoint )
	{
		return endpoints.add( endpoint );
	}
	
	public final boolean removeEndpoint( final URI endpoint )
	{
		return endpoints.remove( endpoint );
	}
	
	public final URI removeEndpoint( final int index )
	{
		return endpoints.remove( index );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return ( endpoints == null || endpoints.isEmpty() );
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Client Endpoints: " + Constants.NEW_LINE );
		for( URI uri : endpoints )
		{
			buffer.append( "  " + uri.toString() + Constants.NEW_LINE );
		}
		return buffer.toString();
	}
}
