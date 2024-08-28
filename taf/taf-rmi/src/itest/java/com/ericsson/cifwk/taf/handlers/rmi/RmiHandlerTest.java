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

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class RmiHandlerTest {

    static String service = "Sample_RMIService_1";
    static int port = 11_099;
    static int badport = 11_111;
    static String message = "Apple";
    static ServiceCreator s;
    String badservice = "bad_RMIService_1";

    @BeforeClass
    public static void setUp() throws IOException {
        s = new ServiceCreator(port, service, message);
        s.startAllServices();
    }

    @Test
    public void shouldGetRmiService_ok() throws RemoteException {
        RmiHandler rmi = new RmiHandler(getDefaultHost());
        Remote r = rmi.getRemoteService(service, port);
        assertEquals((verifyConnectionIsOperable((PrintMessageInterface) r)), message);
    }

    @Test(expected = RuntimeException.class)
    public void getRmiServiceErrors_1() {
        RmiHandler rmi = new RmiHandler(getDefaultHost());
        Remote r = rmi.getRemoteService(badservice, port);
        assertThat(r, nullValue());
    }

    @Test(expected = RuntimeException.class)
    public void getRmiServiceErrors_2() {
        RmiHandler rmi = new RmiHandler(getBadHost());
        Remote r = rmi.getRemoteService(service, port);
        assertThat(r, nullValue());
    }

    @Test(expected = RuntimeException.class)
    public void getRmiServiceErrors_3() {
        RmiHandler rmi = new RmiHandler(getDefaultHost());
        Remote r = rmi.getRemoteService(badservice, badport);
        assertThat(r, nullValue());
    }

    private static Host getDefaultHost() {
        Host host = new Host();
        host.setIp("localhost");
        host.setPort(new HashMap<Ports, String>());
        host.getPort().put(Ports.RMI, String.valueOf(port));
        return host;
    }

    private static Host getBadHost() {
        Host host = new Host();
        host.setIp("localhost111");
        host.setPort(new HashMap<Ports, String>());
        host.getPort().put(Ports.RMI, String.valueOf(port));
        return host;
    }

    public String verifyConnectionIsOperable(PrintMessageInterface connection)
            throws RemoteException {
        return connection.say();
    }

    @AfterClass
    public static void stop() {
        s.stopAllServices();
    }

}
