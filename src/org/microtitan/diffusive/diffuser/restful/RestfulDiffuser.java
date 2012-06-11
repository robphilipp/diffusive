package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.microtitan.diffusive.diffuser.AbstractDiffuser;
import org.microtitan.diffusive.diffuser.serializer.Serializer;

public class RestfulDiffuser extends AbstractDiffuser {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuser.class );
	
	private Serializer serializer;
	private List< URI > endpoints;
	
	public RestfulDiffuser( final Serializer serializer, final List< URI > endpoints )
	{
		this.serializer = serializer;
		this.endpoints = endpoints;
	}
	
	@POST @Path( "/diffuser/create" )
	@Consumes( MediaType.APPLICATION_ATOM_XML )
	@Produces( MediaType.APPLICATION_ATOM_XML )
	public String acceptMessageXml( @FormParam( "lastname" ) String lastname, @FormParam( "firstname" ) String firstname )
	{
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message>" + lastname.toUpperCase() + ", " + firstname + "</message>";
	}
	
	@Override
	public Object runObject( final Object object, final String methodName, final Object... arguments )
	{
		// TODO Auto-generated method stub
		// add the jersey client stuff in here...create a calculator, send the object to the calculator
		return null;
	}
}
