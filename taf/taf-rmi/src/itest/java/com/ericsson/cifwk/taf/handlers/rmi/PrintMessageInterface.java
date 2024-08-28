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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote Interface for the "PrintMessage, world!" service.
 */
public interface PrintMessageInterface extends Remote {
    /**
     * Remotely invokable method.
     * 
     * @return the message of the remote object.
     * @exception RemoteException
     *                if the remote invocation fails.
     */
    public String say() throws RemoteException;
}