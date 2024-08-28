package com.ericsson.cifwk.taf.utils.cluster;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GatewayUtilityTest {

    @Test
    public void shouldDetectLoopbackInterface() {
        assertTrue(GatewayUtility.isInsideNetwork("127.0.0"));
    }

}
