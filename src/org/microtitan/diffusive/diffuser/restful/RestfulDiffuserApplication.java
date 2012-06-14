package org.microtitan.diffusive.diffuser.restful;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


public class RestfulDiffuserApplication extends Application {

	private Set< Object > singletonResources;
	private Set< Class< ? > > perRequestResources;

	public RestfulDiffuserApplication( final Set< Object > singletonResources, final Set< Class< ? > > perRequestResClasses )
	{
		this.singletonResources = singletonResources;
		this.perRequestResources = perRequestResClasses;
	}
	
	public RestfulDiffuserApplication() {}
	
	private Set< Object > getSingletonResources()
	{
		if( singletonResources == null )
		{
			this.singletonResources = new HashSet<>();
		}
		return singletonResources;
	}
	
	private Set< Class< ? > > getPerRequestResources()
	{
		if( perRequestResources == null )
		{
			this.perRequestResources = new HashSet<>();
		}
		return perRequestResources;
	}
	
	public boolean addSingletonResource( final Object singletonResource )
	{
		return getSingletonResources().add( singletonResource );
	}
	
	public boolean addPerRequestResource( final Class< ? > perRequestResource )
	{
		return getPerRequestResources().add( perRequestResource );
	}
	
	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getClasses()
	 */
	@Override
	public Set< Class< ? > > getClasses()
	{
		return getPerRequestResources();
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.core.Application#getSingletons()
	 */
	@Override
	public Set< Object > getSingletons()
	{
		return getSingletonResources();
	}
}
