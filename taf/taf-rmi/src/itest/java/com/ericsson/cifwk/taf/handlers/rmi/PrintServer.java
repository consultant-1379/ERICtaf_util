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
package com.ericsson.cifwk.taf.handlers.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server to print 'PrintMessage World'.
 */
public class PrintServer {

    private int port;
    private String serviceName;
    private String message;

    /**
     * Constructor for PrintServer class.
     *
     * @param port
     * @param serviceName
     * @param message
     */
    public PrintServer(int port, String serviceName, String message) {
        this.port = port;
        this.serviceName = serviceName;
        this.message = message;
        System.out.println("Create service : " + serviceName + " in port "
                + port + " of server ");
    }

    /**
     * Start the server
     */
    public void startServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind(serviceName, new PrintMessage(message));
            System.out.println(serviceName + " Server is started.");
        } catch (Exception e) {
            System.out.println(serviceName + " Server failed: ");
        }
    }

    /**
     * Stop the server.
     */
    public void stopServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.unbind(serviceName);
            System.out.println(serviceName + " Server is stopped.");
        } catch (Exception e) {
            System.out.println(serviceName + " Server failed: ");
        }
    }
}