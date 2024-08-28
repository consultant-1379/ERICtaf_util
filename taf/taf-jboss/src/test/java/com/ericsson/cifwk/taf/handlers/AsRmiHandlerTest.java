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
package com.ericsson.cifwk.taf.handlers;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import javax.naming.NamingException;
import com.ericsson.cifwk.taf.data.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsRmiHandlerTest {
    private static Logger log = LoggerFactory.getLogger(AsRmiHandlerTest.class);

    @Test
    public void getServiceViaJndiLookup() {
        Host jbossNode = new Host();
        log.info("Using node $jbossNode");
        try {
            AsRmiHandler rmi = new AsRmiHandler(jbossNode);
            rmi.close();
        } catch (NullPointerException e) {

        }
    }

    @Test
    public void getContextUserNameTest() throws NamingException {
        Host jbossNode = new Host();
        jbossNode.addUser("cloud-user", "dummy", UserType.OPER);
        jbossNode.addUser("guest", "guestp", UserType.OPER);
        jbossNode.setPort(new HashMap<Ports, String>());
        jbossNode.getPort().put(Ports.RMI, "4447");
        log.info("Using node $jbossNode");
        AsRmiHandler rmi = new AsRmiHandler(jbossNode);
        assertEquals(
                rmi.getContext().getEnvironment()
                        .get("java.naming.security.principal"), "guest");
        rmi.close();
    }
}