package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.spi.TafPlugin;

/**
 * Plugin that registers test listeners for Data Sources.
 */
public class DataSourceTafPlugin implements TafPlugin {
    @Override
    public void init() {
        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        testEventBus.register(new DataSourceShutdownListener());
    }

    @Override
    public void shutdown() {

    }
}
