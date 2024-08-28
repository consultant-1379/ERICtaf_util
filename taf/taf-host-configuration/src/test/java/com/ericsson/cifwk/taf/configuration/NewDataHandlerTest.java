package com.ericsson.cifwk.taf.configuration;

import java.util.List;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Api Example
 */
public class NewDataHandlerTest {
    Predicate<HostFilter> customPredicate = new Predicate<HostFilter>() {
        @Override
        public boolean apply(HostFilter input) {
            return true;
        }
    };

    @Before
    public void setUp() throws Exception {
        TafDataHandler.getConfiguration().clear();
        TafDataHandler.setAttribute("host.hostname.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.hostname.group", "group");
        TafDataHandler.setAttribute("host.hostname.node.thenode.type", "http");
        TafDataHandler.setAttribute("host.hostname.node.thenode.group", "thegroup");
        TafDataHandler.setAttribute("host.hostname.node.thenode.ip", "0.0.0.1");
    }

    @After
    public void tearDown() throws Exception {
        TafDataHandler.getConfiguration().clear();
    }

    @Test
    public void apiProposal() throws Exception {
        // Old API (backwards compatibility)
        List<Host> oldApi = TafDataHandler.getAllHostsByType(HostType.NETSIM);

        // New API
        List<Host> newApi = TafDataHandler.findHost().withType(HostType.NETSIM).getAll();

        // New API for Network Hosts
        List<EnmNetworkHost> hosts = TafDataHandler.findEnmHost().withType(HostType.NETSIM).getAll();

        List<Host> allHosts = TafDataHandler.getAllHosts();

        // Fluent builder
        List<Host> hostsByCriteria = TafDataHandler
                .findHost()
                .withGroup("group")
                .withHostname("hostname")
                .withType("type")
                .withPredicate(customPredicate) // for team specific cases its possible to set custom predicate
                .getAll();

        Host host = TafDataHandler
                .findHost()
                .withGroup("zgroup")
                //...
                .nullable() // return null if no host found. If .nullable not set, will throw exception if no host found
                .get(); // will throw exception if more than one host found

        Host firstHost = TafDataHandler
                .findHost()
                .withGroup("group")
                //...
                .getFirst(); // return fist if more than one host found


       List<EnmNetworkHost> networkHostsByCriteria = TafDataHandler // get network hosts
              .findEnmHost()
              .withGroup("group")
              //...
              .getAll();

        // Attributes delegate to TafConfiguration, for convenience and simplification of migration from old Data Handler
        TafDataHandler.setAttribute("name", "Value");

        String attribute = TafDataHandler.getAttribute("name");

        TafConfiguration configuration = TafDataHandler.getConfiguration();

        // Deprecated API support for DataHandler drop in replacement

        TafDataHandler.getHosts();

        TafDataHandler.getAllHostsByType(HostType.NETSIM);

        //...
    }
}