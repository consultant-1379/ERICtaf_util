package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static com.google.common.truth.Truth.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.exceptions.InvalidNetworkMapException;

@RunWith(MockitoJUnitRunner.class)
public class NetworkMapTest {

    @Mock NetSimContext ctx;

    @Test(expected = InvalidNetworkMapException.class)
    public void invalidNetworkMap_Node_IP4() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/invalid_networkMap_ip.json");
        new NetworkMap(ctx, is);
    }

    @Test(expected = InvalidNetworkMapException.class)
    public void invalidNetworkMap_Node_IP6() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/invalid_networkMap_ipv6.json");
        new NetworkMap(ctx, is);
    }

    @Test(expected = InvalidNetworkMapException.class)
    public void invalidNetworkMap_MissingRoot() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/invalid_networkMap_no_root.json");
        new NetworkMap(ctx, is);
    }

    @Test
    public void getSimulations() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/networkMap.json");
        NetworkMap networkMap = new NetworkMap(ctx, is);

        List<Simulation> simulations = networkMap.getSimulations();
        assertThat(simulations).hasSize(57);
    }

    /**
     * Lookup for
     * {
     * "name": "RNC01RBS46",
     * "ip": "192.168.104.142",
     * "type": "WCDMA RBS U4340-lim",
     * "techType": "WCDMA",
     * "nodeType": "RBS",
     * "mim": "U4340-lim",
     * "Simulation": "RNCV71659x1-FT-RBSU4340x46-RNC01"
     * }
     *
     * @throws Exception
     */
    @Test
    public void findNetworkElement() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/networkMap.json");
        NetworkMap networkMap = new NetworkMap(ctx, is);

        NetworkElement rnc01RBS46 = networkMap.findNetworkElement("RNC01RBS46");
        assertThat(rnc01RBS46).isNotNull();
        assertThat(rnc01RBS46.getSimulationName()).isEqualTo("RNCV71659x1-FT-RBSU4340x46-RNC01");
        assertThat(rnc01RBS46.getMim()).isEqualTo("U4340-lim");
        assertThat(rnc01RBS46.getNodeType()).isEqualTo("RBS");
        assertThat(rnc01RBS46.getTechType()).isEqualTo("WCDMA");
        assertThat(rnc01RBS46.getType()).isEqualTo("WCDMA RBS U4340-lim");
        assertThat(rnc01RBS46.getIp()).isEqualTo("192.168.104.142");
        assertThat(rnc01RBS46.getName()).isEqualTo("RNC01RBS46");
    }

    @Test
    public void getNetworkElements() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/networkmap/networkMap.json");
        NetworkMap networkMap = new NetworkMap(ctx, is);

        List<NetworkElement> networkElements = networkMap.getNetworkElements();
        assertThat(networkElements).hasSize(1662);
    }
}
