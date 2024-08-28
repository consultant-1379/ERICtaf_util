package com.ericsson.cifwk.taf.performance.threshold.rules;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.cifwk.taf.performance.threshold.rules.StandardDeviationCap.stdDev;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StandardDeviationCapTest {

    private ImmutableList<Double> sample;

    @Before
    public void setUp() throws Exception {
        sample = ImmutableList.of(2d, 4d, 4d, 4d, 5d, 5d, 7d, 9d);
    }

    @Test
    public void testStdDev() throws Exception {
        double sigma = stdDev(sample);

        assertEquals(2, sigma, 0);
    }

    @Test
    public void testCheck() throws Exception {
        final StandardDeviationCap cap = new StandardDeviationCap(1.9, 8);

        for (int i = 0; i < 7; i++) {
            assertFalse(cap.check(i, sample.get(i)));
        }
        assertTrue(cap.check(7, sample.get(7)));
        assertFalse(cap.check(8, 8d));
        assertTrue(cap.check(8, 10d));
    }

}
