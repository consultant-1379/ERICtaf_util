package com.ericsson.cifwk.taf.handlers.netsim.domain;

import java.util.Comparator;


final class NetworkElementComparator implements Comparator<NetworkElement> {
	@Override
	public int compare(NetworkElement o1, NetworkElement o2) {
		int hostNamesCompared = o1.getHostName().compareTo(o2.getHostName());
		if (hostNamesCompared != 0) {
			return hostNamesCompared;
		} else {
			int simulationsCompared = o1.getSimulationName().compareTo(o2.getSimulationName());
			if (simulationsCompared != 0) {
				return simulationsCompared;
			}
			return o1.getName().compareTo(o2.getName());
		}
	}
}