package com.ericsson.cifwk.taf.handlers.netsim.domain;

import java.util.List;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;

/**
 * NetSim simulation representation
 */
public interface Simulation extends NetSimCommandExecutor {

    /**
     * Finds by name a NE that belongs to this simulation
     *
     * @param neName
     *         network element name
     *
     * @return this simulation’s NE found by name, or <code>null</code> if it wasn’t found
     */
    NetworkElement getNetworkElement(String neName);

    /**
     * Returns the name of this simulation
     *
     * @return name of this simulation
     */
    String getName();

    /**
     * Returns all network elements that belong to this simulation
     *
     * @return all network elements that belong to this simulation
     */
    List<NetworkElement> getAllNEs();

    /**
     * Returns all network elements that belong to this simulation
     *
     * @return all network elements that belong to this simulation
     */
    List<NetworkElement> getAllNEs(NetSimCommandExecutor executor);

    /**
     * Returns all started network elements that belong to this simulation
     *
     * @return all started network elements that belong to this simulation
     */
    List<NetworkElement> getStartedNEs();

    /**
     * Returns the context (that represents an appropriate {@link Host}) that this simulation belongs to
     *
     * @return the context (that represents an appropriate {@link Host}) that this simulation belongs to
     */
    NetSimContext getContext();

    /**
     * Stops all {@link NetworkElement}'s in the simulation which are started
     * @return boolean representing result of command
     */
    boolean stopAllStartedNEs();

    /**
     * Starts all {@link NetworkElement}'s in the simulation
     * @return boolean representing result of command
     */
    boolean startAllNEs();

}
