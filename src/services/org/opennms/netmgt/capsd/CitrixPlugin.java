//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jul 18: Fixed exception to enable retries.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 14: Used non-blocking I/O for speed improvements.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SocketChannelUtil;

/**
 * <P>This class is designed to be used by the capabilities
 * daemon to test for the existance of an Citrix server on 
 * remote interfaces. The class implements the Plugin
 * interface that allows it to be used along with other
 * plugins by the daemon.</P>
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason</A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS</A>
 *
 *
 */
public final class CitrixPlugin
	extends AbstractPlugin
{
	/**
	 * <P>The capability name of the plugin.</P>
	 */
	private static final String 	PROTOCOL_NAME = "Citrix";
	
	/**
	 * <P>The default port on which the host is checked to
	 * see if it supports Citrix.</P>
	 */
	private static final int	DEFAULT_PORT	= 1494;

	/**
	 * Default number of retries for Citrix requests.
	 */
	private final static int	DEFAULT_RETRY	= 0;
	
	/**
	 * Default timeout (in milliseconds) for Citrix requests.
	 */
	private final static int	DEFAULT_TIMEOUT	= 5000; // in milliseconds
	
	/**
	 * <P>Test to see if the passed host-port pair is the 
	 * endpoint for an Citrix server. If there is an Citrix server
	 * at that destination then a value of true is returned
	 * from the method. Otherwise a false value is returned 
	 * to the caller.</P>
	 *
	 * @param host	The remote host to connect to.
	 * @param port 	The remote port on the host.
	 *
	 * @return True if server supports Citrix on the specified 
	 *	port, false otherwise
	 */
	private boolean isServer(InetAddress host, int port, int retries, int timeout)
	{
		// get a log to send errors
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		//don't let the user set the timeout to 0, an infinite loop will occur if the server is down
		if (timeout==0)
		{
			timeout=10;
		}
		
		boolean isAServer = false;
		for (int attempts=0; attempts <= retries && !isAServer; attempts++)
		{
			SocketChannel sChannel = null;
			try
			{
				// create a connected socket
				//
                                sChannel = SocketChannelUtil.getConnectedSocketChannel(host, port, timeout);
                                if (sChannel == null)
                                {
                                        log.debug("CitrixPlugin: did not connect to host within timeout: " + timeout +" attempt: " + attempts);
                                        continue;
                                }
                                log.debug("CitrixPlugin: connected to host: " + host + " on port: " + port);

				// Allocate a line reader
				//
				BufferedReader reader = new BufferedReader(new InputStreamReader(sChannel.socket().getInputStream()));
				StringBuffer buffer = new StringBuffer();
				while(!isAServer)
				{
					buffer.append((char)reader.read());
					if (buffer.toString().indexOf("ICA")>-1)
					{
						isAServer=true;
					}
				}
			}
			catch(ConnectException cE)
			{
				// Connection refused!!  Continue to  retry.
				//
				log.debug("CitrixPlugin: connection refused to " + host.getHostAddress() + ":" + port);
				isAServer = false;
			}
			catch(NoRouteToHostException e)
			{
				// No route to host!!  No need to perform retries.
				e.fillInStackTrace();
				log.info("CitrixPlugin: Unable to test host " + host.getHostAddress() + ", no route available", e);
				isAServer = false;
				throw new UndeclaredThrowableException(e);
			}
			catch(InterruptedIOException e)
			{
				// no logging necessary, this is "expected" behavior
				//
				isAServer = false;
			}
			catch(IOException e)
			{
				log.info("CitrixPlugin: Error communicating with host " + host.getHostAddress(), e);
				isAServer = false;
			}
			catch(Throwable t)
			{
				log.warn("CitrixPlugin: Undeclared throwable exception caught contacting host " + host.getHostAddress(), t);
				isAServer = false;
			}
			finally
			{
				try
				{
                                        if(sChannel != null)
					{
                                                sChannel.close();
					}
				}
				catch(IOException e) { }
			}
		}

		//
		// return the success/failure of this
		// attempt to contact an ftp server.
		//
		return isAServer;
	}

	/**
	 * Returns the name of the protocol that this plugin
	 * checks on the target system for support.
	 *
	 * @return The protocol name for this plugin.
	 */
	public String getProtocolName()
	{
		return PROTOCOL_NAME;
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller.
	 *
	 * @param address	The address to check for support.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address)
	{
		return isServer(address, DEFAULT_PORT, DEFAULT_RETRY, DEFAULT_TIMEOUT);
	}

	/**
	 * Returns true if the protocol defined by this
	 * plugin is supported. If the protocol is not 
	 * supported then a false value is returned to the 
	 * caller. The qualifier map passed to the method is
	 * used by the plugin to return additional information
	 * by key-name. These key-value pairs can be added to 
	 * service events if needed.
	 *
	 * @param address	The address to check for support.
	 * @param qualifiers	The map where qualification are set
	 *			by the plugin.
	 *
	 * @return True if the protocol is supported by the address.
	 */
	public boolean isProtocolSupported(InetAddress address, Map qualifiers)
	{
		int retries = DEFAULT_RETRY;
		int timeout = DEFAULT_TIMEOUT;
		int port    = DEFAULT_PORT;

		if(qualifiers != null)
		{
			retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
			timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
			port    = ParameterMap.getKeyedInteger(qualifiers, "port", DEFAULT_PORT);
		}

		boolean result = isServer(address, port, retries, timeout);
		if(result && qualifiers != null && !qualifiers.containsKey("port"))
		{
			qualifiers.put("port", new Integer(port));
		}

		return result;
	}
}

