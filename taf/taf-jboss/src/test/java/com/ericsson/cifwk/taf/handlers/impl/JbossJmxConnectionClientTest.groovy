package com.ericsson.cifwk.taf.handlers.impl

import org.junit.Ignore
import org.junit.Test

public class JbossJmxConnectionClientTest {

    // TODO Unit tests should not connect by network. Timeout here stalls test execution.
    @Ignore
	@Test
	public void testGetJmxConnection() {
		try {
			JbossJmxConnectionClient.getJmxConnection("a", "12", "a", "a")
		} catch (java.nio.channels.UnresolvedAddressException e){
			assert true
		}
	}

//	@Test
	public void testCloseConnection() {
		JbossJmxConnectionClient.closeConnection("a", "a", "a", "a")
	}

}
