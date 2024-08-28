package com.ericsson.cifwk.taf.handlers.rmi;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Provide the RMI connection according to specified service.
 */
public final class RmiHandler {

    private static final Logger logger = LoggerFactory .getLogger(RmiHandler.class);

    private Host rcHost;
    private int port;

    /**
     * Constructor requiring OSS-RC node as argument. If OSS-RC node has not RMI port, error is thrown
     *
     * @param rcHost host to connect to
     * @throws java.lang.IllegalArgumentException
     */
    public RmiHandler(final Host rcHost) {
        this.rcHost = rcHost;
        String portString = rcHost.getPort().get(Ports.RMI);
        if (Strings.isNullOrEmpty(portString)) {
            logger.error("No RMI port defined for OSS-RC " + rcHost.getHostname() + ". Cannot make connection.");
            throw new IllegalArgumentException("RMI port undefined for host : " + String.valueOf(rcHost));
        }
        this.port = Integer.parseInt(portString);
    }

    /**
     * Get the remote service from rmi service.
     *
     * @param serviceName service name
     * @param port port
     * @return rmi service object.
     * @throws java.lang.RuntimeException - If wrong host,service or port used.
     */
    public Remote getRemoteService(String serviceName, int port) {
        Remote remote;
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(rcHost.getIp(), port);
            remote = registry.lookup(serviceName);
            return remote;
        } catch (RemoteException | NotBoundException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Get the remote service from rmi service.
     *
     * @param serviceName service name
     * @return rmi service object.
     */
    public Remote getRemoteService(String serviceName) {
        return getRemoteService(serviceName, port);
    }

    public Host getRcHost() {
        return rcHost;
    }

    public void setRcHost(Host rcHost) {
        this.rcHost = rcHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
