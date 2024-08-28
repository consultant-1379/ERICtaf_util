/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.tools.http;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ericsson.de.tools.http.impl.IPv6Resolver;

public interface DnsResolver extends com.ericsson.de.tools.http.DnsResolver {
    /**
     * System default DNS resolver. May use cached IP
     *
     * @see InetAddress#getAllByName(java.lang.String)
     */
    DnsResolver DEFAULT = new DnsResolver() {
        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            return InetAddress.getAllByName(host);
        }
    };

    /**
     * Get IPv6 from DNS.
     * This resolver should be used to ensure that host has IPv6 configured
     *
     * @throws IllegalArgumentException if unable to resolve IPv6 address for host
     */
    DnsResolver IPV6_ONLY = new DnsResolver() {
        IPv6Resolver iPv6Resolver = new IPv6Resolver();

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            return iPv6Resolver.resolve(host);
        }
    };
}
