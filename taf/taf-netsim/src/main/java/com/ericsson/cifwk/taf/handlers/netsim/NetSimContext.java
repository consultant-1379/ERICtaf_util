package com.ericsson.cifwk.taf.handlers.netsim;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkMap;


/**
 * A simple command execution facility that allows you to execute a batch of NetSim commands on a particular host.
 */
public interface NetSimContext extends NetSimCommandExecutor {

    /**
     * Returns the name of the host that the context is working with
     *
     * @return the name of the host that the context is working with. It is equal to the value returned by {@link Host#toString()}
     */
    String getHostName();

    /**
     * Returns the host that the context is working with
     *
     * @return the host that the context is working with.
     */
    Host getHost();

    /**
     * Checks if NetSim instance is running on the current host
     *
     * @return    <code>true</code> if NetSim is running on the current host
     */
    boolean isNetSimRunning();

    /**
     * Returns a NetSim session instance that will execute the commands on the current host
     *
     * @return session instance that will execute the commands on the current host
     */
    NetSimSession openSession();

    /**
     * Closes the context and its sessions
     */
    void close();

    /**
     * Checks if context is closed
     *
     * @return    <code>true</code> if the context is closed
     */
    boolean isClosed();

    /**
     * Retrieve NetworkMap of Netsim Box. Contains information about Simulations and NE available in Netsim
     *
     * @return
     */
    NetworkMap getNetworkMap();

    /**
     * Reload Network Map. Only required if content of Netsim changed during test.
     */
    void refreshNetworkMapData();
}
