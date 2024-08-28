package com.ericsson.cifwk.taf.handlers.impl

import org.jboss.as.cli.impl.CommandContextImpl
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertFalse

class JbossLocalClientTest {

    @BeforeClass
    static void prepareContexts() {
        JbossLocalClient.activeContexts = ["node": ["port": ["user": new CommandContextImpl(), "otherUser": new CommandContextImpl()]]]
    }

    @Test
    void verifyConnectionCanBeFound() {
        assertNotNull(JbossLocalClient.findContext("node", "port", "user"))
    }

    @Test
    void verifyContextsAreStoredAndFoundCorrectly() {
        assertFalse(JbossLocalClient.findContext("node", "port", "user").equals(JbossLocalClient.findContext("node", "port", "otherUser")))
    }


}
