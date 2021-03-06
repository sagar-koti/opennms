/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an TCP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="mailto:weave@oculan.com">Weaver</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class TcpPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(TcpPlugin.class);

    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "TCP";

    /**
     * Default number of retries for TCP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * <P>
     * Test to see if the passed host-port pair is the endpoint for a TCP
     * server. If there is a TCP server at that destination then a value of true
     * is returned from the method. Otherwise a false value is returned to the
     * caller. In order to return true the remote host must generate a banner
     * line which contains the text from the bannerMatch argument.
     * </P>
     * 
     * @param host
     *            The remote host to connect to.
     * @param port
     *            The remote port on the host.
     * @param bannerResult
     *            Banner line generated by the remote host must contain this
     *            text.
     * 
     * @return True if a connection is established with the host and the banner
     *         line contains the bannerMatch text.
     */
    private boolean isServer(InetAddress host, int port, int retries, int timeout, RE regex, StringBuffer bannerResult) {

        boolean isAServer = false;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            Socket socket = null;
            try {
                // create a connected socket
                //
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout);
                socket.setSoTimeout(timeout);
                LOG.debug("TcpPlugin: connected to host: {} on port: {}", port, host);

                // If banner matching string is null or wildcard ("*") then we
                // only need to test connectivity and we've got that!
                //
                if (regex == null) {
                    isAServer = true;
                } else {
                    // get a line reader
                    //
                    BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Read the server's banner line ouptput and validate it
                    // against
                    // the bannerMatch parameter to determine if this interface
                    // supports the
                    // service.
                    //
                    String response = lineRdr.readLine();
                    if (regex.match(response)) {

                        LOG.debug("isServer: matching response= {}", response);
                        isAServer = true;
                        if (bannerResult != null)
                            bannerResult.append(response);
                    } else {
                        // Got a response but it didn't match...no need to
                        // attempt retries
                        isAServer = false;

                        LOG.debug("isServer: NON-matching response= {}", response);
                        break;
                    }
                }
            } catch (ConnectException e) {
                // Connection refused!! Continue to retry.
                //
                LOG.debug("TcpPlugin: Connection refused to {}: {}", port, InetAddressUtils.str(host));
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                //
                e.fillInStackTrace();
                LOG.info("TcpPlugin: Could not connect to host {}, no route to host", InetAddressUtils.str(host), e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // This is an expected exception
                //
                LOG.debug("TcpPlugin: did not connect to host within timeout: {} attempt: {}", attempts, timeout);
                isAServer = false;
            } catch (IOException e) {
                LOG.info("TcpPlugin: An expected I/O exception occured connecting to host {} on port {}", InetAddressUtils.str(host), port, e);
                isAServer = false;
            } catch (Throwable t) {
                isAServer = false;
                LOG.warn("TcpPlugin: An undeclared throwable exception was caught connecting to host {} on port {}", InetAddressUtils.str(host), port, t);
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                }
            }
        }

        //
        // return the success/failure of this
        // attempt to contact an ftp server.
        //
        return isAServer;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        throw new UnsupportedOperationException("Undirected TCP checking not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;
        int port = -1;

        String banner = null;
        String match = null;

        if (qualifiers != null) {
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            port = ParameterMap.getKeyedInteger(qualifiers, "port", -1);
            banner = ParameterMap.getKeyedString(qualifiers, "banner", null);
            match = ParameterMap.getKeyedString(qualifiers, "match", null);
        }

        // verify the port
        //
        if (port == -1)
            throw new IllegalArgumentException("The port must be specified when doing TCP discovery");

        try {
            StringBuffer bannerResult = null;
            RE regex = null;
            if (match == null && (banner == null || banner.equals("*"))) {
                regex = null;
            } else if (match != null) {
                regex = new RE(match);
                bannerResult = new StringBuffer();
            } else if (banner != null) {
                regex = new RE(banner);
                bannerResult = new StringBuffer();
            }
            
            boolean result = isServer(address, port, retries, timeout, regex, bannerResult);
            if (result && qualifiers != null) {
                if (bannerResult != null && bannerResult.length() > 0)
                    qualifiers.put("banner", bannerResult.toString());
            }

            return result;
        } catch (RESyntaxException e) {
            throw new java.lang.reflect.UndeclaredThrowableException(e);
        }
    }
}
