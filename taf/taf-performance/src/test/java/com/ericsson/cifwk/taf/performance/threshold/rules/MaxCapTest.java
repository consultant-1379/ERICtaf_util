package com.ericsson.cifwk.taf.performance.threshold.rules;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaxCapTest {

    @Test
    public void testExceededBy() throws Exception {
        MaxCap cap = new MaxCap(100);

        assertFalse(cap.check(0, 90));
        assertFalse(cap.check(0, 100));
        assertTrue(cap.check(0, 110));
    }

}
