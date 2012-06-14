package org.microtitan.diffusive.diffuser.restful;

import java.util.HashMap;
import java.util.Map;


public class DiffuserContainer {

	private static DiffuserContainer instance = null;
	
	private Map< String, RestfulDiffuser > diffusers;
	
	private DiffuserContainer()
	{
		diffusers = new HashMap<>();
	}
	
	public static DiffuserContainer getInstance()
	{
		synchronized( DiffuserContainer.class )
		{
			if( instance == null )
			{
				// lazily create INSTANCE
				instance = new DiffuserContainer();
			}

			return instance;
		}
	}
	
	public synchronized RestfulDiffuser get( final String id )
	{
		return diffusers.get( id );
	}
	
	public synchronized RestfulDiffuser put( final String id, final RestfulDiffuser diffuser )
	{
		return diffusers.put( id, diffuser );
	}
	
	public synchronized RestfulDiffuser remove( final String id )
	{
		return diffusers.remove( id );
	}
}
