package org.microtitan.diffusive.diffuser.restful.response;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;
import org.microtitan.diffusive.Constants;

/**
 * Class that wraps and parses the Atom feed return as the response to the request to
 * obtain a list of diffusers.
 * 
 * @author Robert Philipp
 */
public class ListDiffuserResponse extends AbstractDiffuserResponse {

	private static final Logger LOGGER = Logger.getLogger( ListDiffuserResponse.class );
	
	private Set< DiffuserInfo > diffusers;
	
	/**
	 * 
	 * @param feed
	 */
	public ListDiffuserResponse( final Feed feed )
	{
		super( feed );
	}

	/* (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.AbstractDiffuserResponse#parse(org.apache.abdera.model.Feed)
	 */
	@Override
	protected void parse( final Feed feed )
	{
		// create the set holding the diffuser information
		if( diffusers == null )
		{
			diffusers = new LinkedHashSet<>();
		}
		
		// run through the entries pulling out the information for the diffusers and
		// adding the parsed information to the set of diffuser info.
		for( Entry entry : feed.getEntries() )
		{
			try
			{
				final String signature = entry.getId().toString();
				final Link selfLink = entry.getLink( Link.REL_SELF );
				if( selfLink != null )
				{
					final URI diffuserUri = selfLink.getHref().toURI();
					final Calendar pubDate = Calendar.getInstance();
					pubDate.setTime( entry.getPublished() );
					
					diffusers.add( new DiffuserInfo( diffuserUri, signature, pubDate ) );
				}
				else
				{
					final StringBuffer message = new StringBuffer();
					message.append( "Diffuser " + signature + " is missing a link to its resource representation." );
					LOGGER.warn( message.toString() );
				}
			}
			catch( URISyntaxException e )
			{
				final StringBuffer message = new StringBuffer();
				message.append( "Failed to parse URI for the following diffuser: " + Constants.NEW_LINE );
				message.append( "  Diffuser ID: " + entry.getId().toString() );
				LOGGER.warn( message.toString() );
				throw new IllegalArgumentException( message.toString() );
			}
		}
	}
	
	/**
	 * @return a {@link List} of {@link DiffuserInfo} objects containing information about
	 * the diffusers held within the diffuser manager resource.
	 */
	public List< DiffuserInfo > getDiffuserInfoList()
	{
		return new ArrayList<>( diffusers );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.restful.response.AbstractDiffuserResponse#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		
		// get the parent's represenation
		buffer.append( super.toString() + Constants.NEW_LINE );
		
		// add the diffuser info
		for( DiffuserInfo info : diffusers )
		{
			buffer.append( "Diffuser:" + Constants.NEW_LINE );
			buffer.append( info.toString() + Constants.NEW_LINE );
		}
		return buffer.toString();
	}
	
	/**
	 * Class representing information about the diffusers that is returned in the feed from the
	 * diffuser web resource.
	 * 
	 * @author Robert Philipp
	 */
	public static class DiffuserInfo {
		
		// for the hashCode method
		private volatile int hashCode;

		private final URI diffuserUri;
		private final String signature;
		private Calendar publishedDate;
		
		public DiffuserInfo( final URI diffuserUri, final String signature, final Calendar publishedDate )
		{
			this.diffuserUri = diffuserUri;
			this.signature = signature;
			this.publishedDate = publishedDate;
			
			hashCode = 0;
		}

		/**
         * @return the diffuserUri
         */
        public URI getDiffuserUri()
        {
        	return diffuserUri;
        }

		/**
         * @return the signature
         */
        public String getSignature()
        {
        	return signature;
        }

		/**
         * @return the publishedDate
         */
        public Calendar getPublishedDate()
        {
        	return publishedDate;
        }
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( final Object object )
		{
			if( !(object instanceof DiffuserInfo) )
			{
				return false;
			}
			
			final DiffuserInfo info = (DiffuserInfo)object;
			if( info.diffuserUri.equals( diffuserUri ) &&
				info.signature.equals( signature ) &&
				info.publishedDate.equals( publishedDate ) )
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
				result = 31 * result + ( diffuserUri == null ? 0 : diffuserUri.hashCode() );
				result = 31 * result + ( signature == null ? 0 : signature.hashCode() );
				result = 31 * result + ( publishedDate == null ? 0 : publishedDate.hashCode() );
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
			final StringBuffer buffer = new StringBuffer();
			buffer.append( "  URI: " + diffuserUri.toString() + Constants.NEW_LINE );
			buffer.append( "  ID: " + signature + Constants.NEW_LINE );
			buffer.append( "  Published Date: " + new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss.SSS" ).format( publishedDate.getTime() ) );
			return buffer.toString();
		}
	}
}
