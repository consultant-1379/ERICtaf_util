package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/09/2016
 */
@API(Internal)
public class TestEventBusProvider {

    private static final TestEventBus EVENT_BUS_INSTANCE = new TestEventBusImpl();

    private TestEventBusProvider() {}

    public static TestEventBus provide() {
        return EVENT_BUS_INSTANCE;
    }

}
