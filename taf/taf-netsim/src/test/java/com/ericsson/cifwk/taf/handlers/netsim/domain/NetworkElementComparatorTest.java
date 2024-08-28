package com.ericsson.cifwk.taf.handlers.netsim.domain;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class NetworkElementComparatorTest {

	@Test
	public void test() {
		List<NetworkElement> neList = Lists.newArrayList();
		
		neList.add(createNE("host2", "sim21", "NE001"));
		neList.add(createNE("host1", "sim12", "NE002"));
		neList.add(createNE("host2", "sim23", "NE004"));
		neList.add(createNE("host2", "sim22", "NE002"));
		neList.add(createNE("host1", "sim11", "NE001"));
		neList.add(createNE("host2", "sim23", "NE003"));
		
		Collections.sort(neList, new NetworkElementComparator());
		
		verifyListNe(neList, 0, "host1", "sim11", "NE001");
		verifyListNe(neList, 1, "host1", "sim12", "NE002");
		verifyListNe(neList, 2, "host2", "sim21", "NE001");
		verifyListNe(neList, 4, "host2", "sim23", "NE003");
		verifyListNe(neList, 5, "host2", "sim23", "NE004");
	}

	private NetworkElement createNE(String hostName, String simName, String neName) {
		NetworkElement neMock = mock(NetworkElement.class);
		when(neMock.getHostName()).thenReturn(hostName);
		when(neMock.getSimulationName()).thenReturn(simName);
		when(neMock.getName()).thenReturn(neName);

		return neMock;
	}
	
	private void verifyListNe(List<NetworkElement> neList, int index, String hostName, String simName, String neName) {
		NetworkElement ne = neList.get(index);
		Assert.assertEquals(hostName, ne.getHostName());
		Assert.assertEquals(simName, ne.getSimulationName());
		Assert.assertEquals(neName, ne.getName());
	}
}
