package org.microtitan.diffusive.diffuser.restful.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

@Path( RestfulClassPathResource.CLASSPATH_PATH )
public class RestfulClassPathResource {

	private static final Logger LOGGER = Logger.getLogger( RestfulDiffuserManagerResource.class );

	public static final String CLASSPATH_PATH = "classpath/"; 

	// parameters for retrieving a Class for a specific diffuser
	public static final String FULLY_QUALIFIED_CLASS_NAME = "classname";
	
	@GET @Path( "{" + FULLY_QUALIFIED_CLASS_NAME + ": [a-zA-Z0-9\\.\\-]*}" )
	public Response getClass( @Context final UriInfo uriInfo,
							  @PathParam( FULLY_QUALIFIED_CLASS_NAME ) final String className )
	{
		Class< ? > clazz = null;
		try
        {
			clazz = Class.forName( className );
        }
        catch( ClassNotFoundException e )
        {
        	// in this case, the class isn't found, which is a problem, because it should
        	// be on the class path...we could add in the file system class loader...?
	        e.printStackTrace();
        }
		final byte[] classBytes = convertClassToByteArray( clazz );
		
		return null;
	}
	
	private byte[] convertClassToByteArray( final Class< ? > clazz )
	{
		byte[] bytes = null;
		try( final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 final ObjectOutputStream out = new ObjectOutputStream( bos ) )
		{
			out.writeObject( clazz );
			bytes = bos.toByteArray();
		}
        catch( IOException e )
        {
	        e.printStackTrace();
        }
		return bytes;
	}
}
