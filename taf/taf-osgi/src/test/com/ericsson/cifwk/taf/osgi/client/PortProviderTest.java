package com.ericsson.cifwk.taf.osgi.client;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.ericsson.cifwk.taf.osgi.agent.AgentServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class PortProviderTest {

    public static final int MILLIS = 1000;
    public static final int PORT_REQUEST = 10;
    public static final int NOT_MORE_2_SECONDS_PER_PORT_REQUEST = 2 *PORT_REQUEST*MILLIS ;

    private static Host host;
    private static EmbeddedJetty jetty;
    private static int portToUse = PortProvider.DEFAULT_START_PORT;

    @AfterClass
    public static void tearDown() throws Exception {
        jetty.stop();
    }

    @BeforeClass
    public static void prepareHost() {
        host = new Host();
        host.setIp("127.0.0.1");
        host.setHostname("localhost");

        while (jetty == null) {
            try {
                jetty = EmbeddedJetty.build()
                        .withServlet(new AgentServlet(null), "/agent/*")
                        .withPort(portToUse)
                        .start();
            } catch (Exception e) {
                e.printStackTrace();
                jetty = null;
                portToUse++;
            }
        }

    }

    private static final CopyOnWriteArrayList<Integer> ports = new CopyOnWriteArrayList<Integer>();

    @Test(timeout = (NOT_MORE_2_SECONDS_PER_PORT_REQUEST))
    public void shallProvide10UniquePortsWithoutUsingJettyOne()
            throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            new Thread(new PortRequest()).start();
        }
        while (ports.size() < 10) {
            Thread.sleep(10);
        }
        assertEquals(ports.size(), new TreeSet<Integer>(ports).size());
        assertTrue(ports.get(0) > portToUse);
    }

    private class PortRequest implements Runnable {

        @Override
        public void run() {
            ports.add(PortProvider.getCliPort(host));
            ports.add(PortProvider.getDataPort(host));
        }
    }

}
