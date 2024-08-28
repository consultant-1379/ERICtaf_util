package com.ericsson.cifwk.taf.performance.threshold.rules;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MaxLinearCapTest {

    @Test
    public void testExceedsFromOrigin() throws Exception {
        MaxLinearCap cap = new MaxLinearCap(0, 100, 2);

        assertFalse(cap.check(0, 90));
        assertFalse(cap.check(0, 100));
        assertTrue(cap.check(0, 110));

        assertFalse(cap.check(2, 103));
        assertFalse(cap.check(2, 104));
        assertTrue(cap.check(2, 105));
    }

    @Test
    public void testExceeds() throws Exception {
        MaxLinearCap cap = new MaxLinearCap(50, 100, 2);

        assertFalse(cap.check(51, 100));
        assertFalse(cap.check(50, 100));
        assertTrue(cap.check(49, 100));
    }

}
