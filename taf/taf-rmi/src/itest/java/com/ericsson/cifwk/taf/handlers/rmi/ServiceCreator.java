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

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is creating and starting services. This class is accepting command
 * line arguments (pairs) for provide service name and message. Each arguments
 * pair represent service name and message.
 * <p>
 * <b>Example :</b> When we executing following,
 * <p>
 * <i>java ServiceCreator </i>Service_name Message_display_when_call_the_service
 * <p>
 * Create a service called "Service_name"
 * <p>
 * User can provide n pairs of service and message in arguments and it create n
 * services. When no any arguments provided, then default service is creating
 * with default message.
 *
 */
public class ServiceCreator {

    private Registry registry;
    private List<PrintServer> services = new ArrayList<PrintServer>();

    public ServiceCreator(int port, String service, String mesg) {
        createRegistry(port);
        createServices(port, service, mesg);
    }

    /**
     * Create registry on given port
     *
     * @param port
     */
    private void createRegistry(int port) {
        try {
            registry = LocateRegistry.createRegistry(port);
            System.out.println("Creating registry on " + port);
        } catch (RemoteException e) {
            System.out.println("Cannot create registry on " + port);
        }
    }

    /**
     * Create services and start them all.
     *
     * @param args
     */
    private void createServices(int port, String service, String mesg) {
        createServicesFromArg(port, service, mesg);
    }

    /**
     * Create services from Args.
     *
     * @param args
     */
    private void createServicesFromArg(int port, String service, String mesg) {
        services.add(new PrintServer(port, service, mesg));
        System.out.println("Create service " + service + " on port " + port
                + " of server");
    }

    /**
     * Start all services.
     */
    public void startAllServices() {
        if (services.size() != 0) {
            for (PrintServer serves : services) {
                serves.startServer();
            }
        }
    }

    /**
     * Stop all services.
     */
    public void stopAllServices() {
        if (services.size() != 0) {
            for (PrintServer serves : services) {
                serves.stopServer();
            }
        }
    }

    /**
     * Print all services bind to registry
     */
    public void printServices() {
        String[] serviceList = null;
        try {
            serviceList = registry.list();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        for (int x = 0; x < serviceList.length; x++) {
            System.out.println(serviceList[x]);
        }
    }

    /**
     * @return the registry
     */
    public Registry getRegistry() {
        return registry;
    }

}