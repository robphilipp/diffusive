package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.freezedry.persistence.utils.Require;
import org.microtitan.diffusive.diffuser.serializer.Serializer;

@Path( "/diffuser" )
public class RestfulDiffuserManagerResource {

//	private Serializer serializer;
//	private List< URI > clientEndpoints;
//	
//	public RestfulDiffuserManagerResource( final Serializer serializer, final List< URI > clientEndpoints )
//	{
//		if( clientEndpoints == null )
//		{
//			this.clientEndpoints = new ArrayList<>();
//		}
//		else
//		{
//			this.clientEndpoints = clientEndpoints;
//		}
//		
//		Require.notNull( serializer );
//		this.serializer = serializer;
//	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@POST @Path( "/create" )
	@Consumes( MediaType.APPLICATION_XML )
	@Produces( MediaType.APPLICATION_XML )
	public DiffuserCreateResponse createDiffuser( final DiffuserCreateRequest request )
	{
		// create the new diffuser and pass back the link to this new resource.
		final RestfulDiffuser diffuser = new RestfulDiffuser( request.getSerializer(), request.getClientEndpoints() );
		
		System.out.println( request.toString() );
		return new DiffuserCreateResponse();
	}
}
