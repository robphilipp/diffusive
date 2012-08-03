package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.List;


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
}
