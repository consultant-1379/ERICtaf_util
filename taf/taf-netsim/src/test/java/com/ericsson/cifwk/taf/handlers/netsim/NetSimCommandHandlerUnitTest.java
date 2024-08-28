package com.ericsson.cifwk.taf.handlers.netsim;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.google.common.collect.Lists;

public class NetSimCommandHandlerUnitTest {
	@Before
	public void setUp() {
		NetSimCommandHandler.closeAllContexts();
	}

	@Test
	public void createSimulations() {
		NetSimCommandHandler unit = NetSimCommandHandler.getInstance(mock(Host.class));
		List<String> simNames = Lists.newArrayList("sim1", "sim1.zip", "sim2", "sim2.zip", "sim3", "sim4");
		List<Simulation> simulations = unit.createSimulations(null, simNames, Lists.newArrayList("sim2", "sim3"));
		Assert.assertEquals(2, simulations.size());
	}

	@Test
	public void closeAllSessions() {
		Assert.assertEquals(0, NetSimCommandHandler.getOpenContextsAmount());
		NetSimContext context1 = NetSimCommandHandler.getContext(mock(Host.class));
		NetSimContext context2 = NetSimCommandHandler.getContext(mock(Host.class));
		Assert.assertEquals(2, NetSimCommandHandler.getOpenContextsAmount());
		
		context1.close();
		Assert.assertEquals(1, NetSimCommandHandler.getOpenContextsAmount());

		NetSimCommandHandler.closeAllContexts();
		Assert.assertEquals(0, NetSimCommandHandler.getOpenContextsAmount());
		Assert.assertTrue(context2.isClosed());
	}
}
