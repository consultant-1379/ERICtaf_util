package com.ericsson.cifwk.taf.handlers.netsim.spi;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.spi.TafPlugin;

/**
 * TAF plugin that registers NetSim test listeners.
 */
public class NetSimTafPlugin implements TafPlugin {

	@Override
	public void init() {
		TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
		testEventBus.register(new NetSimTestListener());
	}

	@Override
	public void shutdown() {
	}

}
