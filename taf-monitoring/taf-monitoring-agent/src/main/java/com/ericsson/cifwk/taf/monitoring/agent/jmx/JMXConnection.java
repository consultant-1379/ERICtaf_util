/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.management.remote.JMXConnector;

/**
 * A container class for a JMXConnector and a list of associated MBean proxies.
 * <p>
 * The purpose of this class is to add the possibility of retrieve the MBean value and after close the JMXConnector (client connection).
 *
 * @author eraumun
 *
 */
public class JMXConnection<T> implements Closeable {

    private final JMXConnector jmxConnector;
    private List<T> mbeanProxies;

    public JMXConnection(final JMXConnector jmxConnector){
        this.jmxConnector = jmxConnector;
    }
    /**
     * Constructor
     *
     * @param jmxConnector
     * @param mbeanProxies
     */
    public JMXConnection(final JMXConnector jmxConnector, final List<T> mbeanProxies) {
        this.jmxConnector = jmxConnector;
        this.mbeanProxies = mbeanProxies;
    }

    /**
     * Closes the client connection to its server.
     * <p>
     * Delegates to this objects {@link javax.management.remote.JMXConnector#close jmxConnector.close method}.
     *
     * @see javax.management.remote.JMXConnector#close
     */
    @Override
    public void close() throws IOException {
        this.jmxConnector.close();
    }

    /**
     * Returns the list of MBean proxies which was set in the constructor.
     *
     * @return the list of MBean proxies
     */
    public List<T> getMbeanProxies() {
        return this.mbeanProxies;
    }

    /**
     * Returns the first MBean proxy from the list which was set in the constructor.
     *
     * @return the first MBean proxy
     */
    public T getMbeanProxy() {
        return this.mbeanProxies.get(0);
    }

}
