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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Remote Class for the "PrintMessage, world!" service.
 */
public class PrintMessage extends UnicastRemoteObject implements
        PrintMessageInterface {

    private static final long serialVersionUID = 102012123101L;
    private String message;

    /**
     * Construct a remote object
     *
     * @param msg the message of the remote object.
     * @exception RemoteException if the object handle cannot be constructed.
     */
    public PrintMessage(String msg) throws RemoteException {
        message = msg;
    }

    /**
     * Implementation of the remotely invokable method.
     *
     * @return the message of the remote object.
     * @exception RemoteException if the remote invocation fails.
     */
    public String say() throws RemoteException {
        return message;
    }
}