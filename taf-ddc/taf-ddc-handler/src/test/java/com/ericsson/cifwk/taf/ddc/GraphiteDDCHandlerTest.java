package com.ericsson.cifwk.taf.ddc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphiteDDCHandlerTest {

    GraphiteDDCHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new GraphiteDDCHandler();
    }

    @Test
    public void shouldReturnEmptyString_whenNotDefinedMetricPrefix() throws Exception {
        System.clearProperty(GraphiteDDCHandler.GRAPHITE_METRIC_PREFIX_PROPERTY);
        String metricPrefix = GraphiteDDCHandler.getMetricPrefix();
        assertEquals("", metricPrefix);
    }

    @Test
    public void shouldReturnEmptyString_whenEmptyStringDefinedMetricPrefix() throws Exception {
        System.setProperty(GraphiteDDCHandler.GRAPHITE_METRIC_PREFIX_PROPERTY, "    ");
        String metricPrefix = GraphiteDDCHandler.getMetricPrefix();
        assertEquals("", metricPrefix);
    }

    @Test
    public void shouldAddDOT_whenDefinedMetricPrefix() throws Exception {
        System.setProperty(GraphiteDDCHandler.GRAPHITE_METRIC_PREFIX_PROPERTY, "PREFIX");
        String metricPrefix = GraphiteDDCHandler.getMetricPrefix();
        assertEquals(".PREFIX", metricPrefix);
    }

    @Test
    public void shouldNotAddDOT_whenDOTPresentInMetricPrefix() throws Exception {
        System.setProperty(GraphiteDDCHandler.GRAPHITE_METRIC_PREFIX_PROPERTY, ".PREFIX");
        String metricPrefix = GraphiteDDCHandler.getMetricPrefix();
        assertEquals(".PREFIX", metricPrefix);
    }

}


