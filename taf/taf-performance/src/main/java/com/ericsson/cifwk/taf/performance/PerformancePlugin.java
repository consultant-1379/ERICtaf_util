package com.ericsson.cifwk.taf.performance;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.performance.metric.TestMetricsListener;
import com.ericsson.cifwk.taf.spi.TafPlugin;

public class PerformancePlugin implements TafPlugin {

    @Override
    public void init() {
        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        testEventBus.register(new TestMetricsListener());
    }

    @Override
    public void shutdown() {
        PerformancePluginServices.shutdown();
    }

}
