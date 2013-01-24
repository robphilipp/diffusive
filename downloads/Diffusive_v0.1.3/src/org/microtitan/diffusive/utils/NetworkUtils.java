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
package org.microtitan.diffusive.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Basic utilty methods for obtaining information about network
 * 
 * @author Robert Philipp
 */
public class NetworkUtils {
	
	private static final Logger LOGGER = Logger.getLogger( NetworkUtils.class );

	/**
	 * Returns the IPv4 address for the local host if there exists a network device that is up,
	 * is not the loop-back device or address, and is not virtual. If the above conditions are not
	 * met, or if the network device is not available, return "localhost" as the IP address.
	 * @return a string version of the IPv4 address of the local host, or "localhost".
	 */
	public static final String getLocalIpAddress()
	{
		// the below code works on a mac as well...above code didn't...
		String ip = "localhost";
		try
		{
			// grab all the network interfaces and find the one that isn't a loop-back, is up, and isn't virtual
			final Enumeration< NetworkInterface > interfaces = NetworkInterface.getNetworkInterfaces();
			while( interfaces.hasMoreElements() )
			{
				final NetworkInterface currentInterface = interfaces.nextElement();				
				if( currentInterface.isUp() && !currentInterface.isLoopback() && !currentInterface.isVirtual() )
				{
					// for this interface, grab all the internet addresses and find the one that is not the
					// loop-back address and is an IP4 address (as opposed to an IP6 address)
					final Enumeration< InetAddress > addresses = currentInterface.getInetAddresses();
					while( addresses.hasMoreElements() )
					{
						final InetAddress currentAddress = addresses.nextElement();
						if( !currentAddress.isLoopbackAddress() && currentAddress instanceof Inet4Address )
						{
							// this is the address we want, so grab the string representation (i.e. xxx.xxx.xxx.xxx)
							// and break out of the loop
							ip = currentAddress.getHostAddress();
							break;
						}
					}
					
					// and then break out of the outer loop as well.
					break;
				}
			}
		}
		catch( SocketException e )
		{
        	final String message = "Unable to get IP address of local host. Using \"localhost\" instead.";
	        LOGGER.warn( message );
		}
		return ip;
	}
	
	/**
	 * Constructs a string representation of the URI from the local host's IP address, the specified scheme and port
	 * @param scheme The scheme (i.e. http, https, etc)
	 * @param port The port at which the server listens
	 * @return a string representation of the URI
	 */
	public static final String createLocalHostServerUri( final String scheme, final int port )
	{
		return scheme + "://" + getLocalIpAddress() + String.format( ":%d", port );
	}
	

}
