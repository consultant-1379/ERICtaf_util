package com.ericsson.cifwk.taf.ui.spi;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.spi.TafPlugin;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class UiTafPlugin implements TafPlugin {

    @Override
    public void init() {
        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        testEventBus.register(new UiTestEventSubscriber());
    }

    @Override
    public void shutdown() {
        // empty
    }

}
