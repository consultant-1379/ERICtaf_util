package com.ericsson.cifwk.taf.monitoring.agent;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BeemoTest {

    @Test
    public void shouldReloadInterval(){
        assertNotNull(Beemo.getReloadInterval());
    }

    @Test
    @Ignore("Flaky test")
    public void getShouldShutdown() throws InterruptedException {
        System.setProperty("taf.config.reload","10");
        assertFalse(Beemo.shouldShutdown());
        System.setProperty(Beemo.TAF_SHUTDOWN, "true");
        Thread.sleep(10L);
        assertTrue(Beemo.shouldShutdown());
        Beemo.close();
    }
}
