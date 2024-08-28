package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class SimulationGroupUnitTest {

	@Test
	public void testBasicCollectionOperations() {
		Simulation sim1 = mock(Simulation.class);
		Simulation sim2 = mock(Simulation.class);
		Simulation sim3 = mock(Simulation.class);
		Simulation sim4 = mock(Simulation.class);
		
		SimulationGroup simGroup1 = new SimulationGroup(sim1);
		SimulationGroup simGroup2 = new SimulationGroup(sim3, sim4);
		Assert.assertFalse(simGroup1.isEmpty());
		Assert.assertFalse(simGroup2.isEmpty());
		
		simGroup1.add(sim2);
		Assert.assertTrue(simGroup1.contains(sim1) && simGroup1.contains(sim2));
		Assert.assertEquals(2, simGroup1.size());

		simGroup1.addAll(simGroup2);
		Assert.assertTrue(simGroup1.containsAll(simGroup2));
		Assert.assertTrue(simGroup1.contains(sim3) && simGroup1.contains(sim4));
		Assert.assertEquals(4, simGroup1.size());
		
		simGroup1.removeAll(simGroup2);
		Assert.assertTrue(simGroup1.contains(sim1) && simGroup1.contains(sim2));
		Assert.assertEquals(2, simGroup1.size());
		
		simGroup1.remove(sim1);
		Assert.assertTrue(simGroup1.contains(sim2) && !simGroup1.contains(sim1));
		Assert.assertEquals(1, simGroup1.size());
		
		simGroup1.retainAll(simGroup2);
		Assert.assertTrue(simGroup1.isEmpty());
	}

	@Test
	public void testConversionAndIteration() {
		Simulation sim1 = mock(Simulation.class);
		Simulation sim2 = mock(Simulation.class);
		
		SimulationGroup simGroup1 = new SimulationGroup(sim1, sim2);
		Iterator<Simulation> iterator = simGroup1.iterator();
		Assert.assertEquals(sim1, iterator.next());
		Assert.assertEquals(sim2, iterator.next());
		Assert.assertFalse(iterator.hasNext());
		
		Object[] array = simGroup1.toArray();
		Assert.assertEquals(sim1, array[0]);
		Assert.assertEquals(sim2, array[1]);

		array = simGroup1.toArray(new Simulation[0]);
		Assert.assertEquals(sim1, array[0]);
		Assert.assertEquals(sim2, array[1]);
		
		simGroup1.clear();
		Assert.assertTrue(simGroup1.isEmpty());
	}
}
