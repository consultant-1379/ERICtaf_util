package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ericsson.cifwk.taf.data.Host;
import com.google.common.base.Preconditions;

/**
 * NetSim context registry
 */
class NetSimContextRegistry {
	private final Map<Host, NetSimContext> registry = new ConcurrentHashMap<>();

	public NetSimContext getContext(Host host) {
		Preconditions.checkArgument(host != null, "Host cannot be null");
		NetSimContext context = registry.get(host);
		if (context == null || context.isClosed()) {
			context = createContextForHost(host);
			registry.put(host, context);
		}
		return context;
	}

	NetSimContext createContextForHost(Host host) {
		return new SshNetSimContext(host);
	}

	public void close() {
		Collection<NetSimContext> registeredContexts = registry.values();
		for (NetSimContext netsimContext : registeredContexts) {
			if (!netsimContext.isClosed()) {
				netsimContext.close();
			}
		}
		registry.clear();
	}

	public int getOpenContextsAmount() {
		Collection<NetSimContext> allContexts = registry.values();
		int result = 0;
		for (NetSimContext context : allContexts) {
			if (!context.isClosed()) {
				result++;
			}
		}
		return result;
	}

}
