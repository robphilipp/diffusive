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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;
import org.microtitan.diffusive.diffuser.restful.atom.Atom;
import org.microtitan.diffusive.utils.ClassLoaderUtils;

/**
 * The JAX-RS resource that provides {@code byte[]} representations of a specified class. The 
 * resource will first attempt to load the {@link Class} file as a resource found on the application's
 * class path. If the no {@link Class} with that name is found, then it will attempt to load the
 * {@link Class} file from the JAR files listed in the {@code classPaths} handed to the constructor.
 * The URL should point to a JAR file, and are likely to be a local path. See the 
 * {@link RestfulDiffuserManagerResource#createJarClassPath(List)} method to create a {@link List}
 * of {@link URL} from a list of absolute or relative local paths. Examples of paths passed to
 * the {@link RestfulDiffuserManagerResource#createJarClassPath(List)} method are:
 * <ul>
 * 	<li>{@code ..\\examples\\example_0.2.0.jar} (a relative path on Windows)</li>
 * 	<li>{@code ../another/example.jar} (a relative path on Windows, Unix/Linux, MacOSX)</li>
 * 	<li>{@code C:/this/is/a/path/to/a/jar.jar} (an absolute path on Windows)</li>
 * 	<li>{@code C:\\this\\is\\another\\jar\\path\\example_0.2.0.jar} (an absolute path on Windows)</li>
 * 	<li>{@code /unix/type/jar/path/example.jar} (an absolute path on Unix/Linux, MacOSX)</li>
 * 	<li>{@code /Users/person/Documents/workspace/examples/example_0.2.0.jar} (an absolute path on Unix/Linux, MacOSX)</li>
 * </ul>
 * On Windows these will be translated to the form<br>
 * {@code file:///C:/path/to/jar/file/example.jar}<br>
 * and on Unix/Linux and MacOSX these will be translated to the form<br>
 * {@code file:///path/to/jar/file/example.jar}<br>
 * 
 * @author Robert Philipp
 */
@Path( RestfulClassPathResource.CLASSPATH_PATH )
public class RestfulClassPathResource {

	private static final Logger LOGGER = Logger.getLogger( RestfulClassPathResource.class );

	public static final String CLASSPATH_PATH = "/classpath"; 

	// parameters for retrieving a Class for a specific diffuser
	public static final String FULLY_QUALIFIED_CLASS_NAME = "classname";
	
	// holds the class loader used to specified the additional jars that hold the classes
	// for the actual diffused methods.
	private final URLClassLoader urlClassLoader;

	/**
	 * 
	 * @param classPaths holds a list of {@link URL} to JAR files that must be loaded in order to execute
	 * (see {@link #createJarClassPath(List)} for a convenient method to create this list).
	 * diffuser methods, and that will get sent to other remote diffusers
	 */
	public RestfulClassPathResource( final List< URL > classPaths )
	{
		// create the class loader for the additional jars that hold the classes for the diffusive methods
		final URL[] urls = classPaths.toArray( new URL[0] );
		urlClassLoader = new URLClassLoader( urls, this.getClass().getClassLoader() );
	}
	
	/**
	 * 
	 * @param uriInfo
	 * @param className
	 * @return
	 */
	@GET @Path( "{" + FULLY_QUALIFIED_CLASS_NAME + ": [a-zA-Z0-9\\.\\-]*}" )
	public Response getClass( @Context final UriInfo uriInfo,
							  @PathParam( FULLY_QUALIFIED_CLASS_NAME ) final String className )
	{
		// grab the date for time stamp
		final Date date = new Date();

		// create the atom feed 
		final URI requestUri = uriInfo.getRequestUri();
		final String resultKey = UUID.randomUUID().toString();
		final Feed feed = Atom.createFeed( requestUri, resultKey, date, uriInfo.getBaseUri() );

		// find the class and convert it to a byte[] for transport across the network
		byte[] classBytes = ClassLoaderUtils.convertClassToByteArray( className );
		StringBuffer message = new StringBuffer();
		if( classBytes == null || classBytes.length == 0 )
		{
			message.append( "Could not find class on system class path. Attempting URL class loader." + Constants.NEW_LINE );
			message.append( "  Class Name: " + className + Constants.NEW_LINE );
			message.append( "  Class Loader: " + this.getClass().getClassLoader().getClass().getName() + Constants.NEW_LINE );
			message.append( "  Class Path: " + System.getProperty( "java.class.path" ) );
			LOGGER.info( message.toString() );
			
			// attempt to use the url class loader to create the bytes
			classBytes = ClassLoaderUtils.convertClassToByteArray( className, urlClassLoader );
			if( classBytes == null || classBytes.length == 0 )
			{
				message = new StringBuffer();
				message.append( "Could not load class using URL class loader" + Constants.NEW_LINE );
				message.append( "  Class Loader: " + urlClassLoader.getClass().getName() + Constants.NEW_LINE );
				message.append( "  Class Path Searched: " );
				for( URL url : urlClassLoader.getURLs() )
				{
					message.append( Constants.NEW_LINE + "    " + url.toString() );
				}
				LOGGER.error( message.toString() );
			}
		}

		Response response = null;
		if( classBytes == null || classBytes.length == 0 )
		{
			// create an error entry
			final Entry entry = Atom.createEntry();
			entry.setId( resultKey );
			entry.setContent( "Failded to load class result." + Constants.NEW_LINE + message.toString(), MediaType.TEXT_PLAIN );
			feed.addEntry( entry );
			
			response = Response.status( Status.NOT_FOUND )
							   .location( requestUri )
							   .entity( feed.toString() )
							   .build();
		}
		else
		{
			// create an entry for the feed and set the byte[] representing the class as the content
			final Entry entry = Atom.createEntry();
			final ByteArrayInputStream input = new ByteArrayInputStream( classBytes );
			entry.setId( resultKey );
			entry.setContent( input, MediaType.APPLICATION_OCTET_STREAM );
			feed.addEntry( entry );
			
			// create the response
			response = Response.ok()
							   .location( requestUri )
							   .entity( feed.toString() )
							   .type( MediaType.APPLICATION_ATOM_XML )
							   .build();
		}
		return response;
	}
}
