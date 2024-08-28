package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class NetSimResultMapperTest {

    @Mock
    NetSimContext netSimContext;
    @Mock
    Simulation simulation;
    @Mock
    NetworkElement networkElement;

    private NetSimResultMapper netSimResultMapper;

    @Before
    public void setUp() throws Exception {
        netSimResultMapper = new NetSimResultMapper(netSimContext, simulation);
    }

    @Test
    public void shouldParseCommandOutput() throws Exception {
        String listOfNesAsText = "NE Name                  Type             Server         In Address       Default dest.\n"
                + "LTE12ERBS00001           LTE ERBS B154-V2 netsim         192.168.100.226  \n"
                + "LTE12ERBS00002           LTE ERBS B154-V2 netsim         192.168.100.227  \n"
                + "Delayed response to: LTE02ERBS00001\n"
                + "set_interface_version version: 1.2\n"
                + "LTE12ERBS00003           LTE ERBS B154-V2 netsim         192.168.100.228  \n" + "OK";

        List<NetworkElement> networkElements = netSimResultMapper.parse(new CommandOutput(listOfNesAsText));

        assertEquals(3, networkElements.size());

        verifyNetworkElement("LTE12ERBS00001",
                "192.168.100.226",
                "B154-V2",
                "ERBS",
                "LTE",
                "LTE ERBS B154-V2",
                networkElements.get(0));

        verifyNetworkElement("LTE12ERBS00002",
                "192.168.100.227",
                "B154-V2",
                "ERBS",
                "LTE",
                "LTE ERBS B154-V2",
                networkElements.get(1));


        verifyNetworkElement("LTE12ERBS00003",
                "192.168.100.228",
                "B154-V2",
                "ERBS",
                "LTE",
                "LTE ERBS B154-V2",
                networkElements.get(2));
    }

    @Test
    public void shouldPopulateNetsimNEDetails() {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("Default dest.", "");
        map.put("In Address", "192.168.100.226");
        map.put("NE Name", "LTE12ERBS00001");
        map.put("Server", "netsim");
        map.put("Type", "LTE ERBS B154-V2");

        NetworkElementImpl ne = new NetworkElementImpl(null, null);
        netSimResultMapper.populateNetsimNEDetails(ne, map);

        verifyNetworkElement("LTE12ERBS00001",
                "192.168.100.226",
                "B154-V2",
                "ERBS",
                "LTE",
                "LTE ERBS B154-V2",
                ne);
    }


    @Test
    public void shouldExtractNEsFromSGSNNEOutputTextList() {
        String listOfNesAsText = "NE Name                  Type                Server         In Address       Default dest.\n"
                + "SGSNMME15BV201           WPP SGSN 15B-WPP-V2 ieatnetsimv044-01 10.152.0.162 1161 public v2 .128.0.0.193.1.10.152.0.162 CORE-FT-SGSNMME-15B-V2x2_SGSNMME15BV201 authpass privpass hmac_md5 cbc_des  192.168.0.12:162\n"
                + "SGSNMME15BV202           WPP SGSN 15B-WPP-V2 ieatnetsimv044-01 10.152.0.163 1161 public v2 .128.0.0.193.1.10.152.0.163 CORE-FT-SGSNMME-15B-V2x2_SGSNMME15BV202 authpass privpass hmac_md5 cbc_des  192.168.0.12:162\n"
                + "OK";

        List<NetworkElement> nes = netSimResultMapper.parse(new CommandOutput(listOfNesAsText));
        Assert.assertEquals(2, nes.size());

        verifyNetworkElement("SGSNMME15BV201",
                "10.152.0.162",
                "15B-WPP-V2",
                "SGSN",
                "WPP",
                "WPP SGSN 15B-WPP-V2", nes.get(0));
        verifyNetworkElement("SGSNMME15BV202",
                "10.152.0.163",
                "15B-WPP-V2", "SGSN",
                "WPP",
                "WPP SGSN 15B-WPP-V2", nes.get(1));
    }

    @Test
    public void populateNetsimNEDetailsForSGSNTypeNodes() {
        HashMap<String, String> map = Maps.newHashMap();
        map.put("Default dest.",
                "public v2 .128.0.0.193.1.10.152.0.162 CORE-FT-SGSNMME-15B-V2x2_SGSNMME15BV201 authpass privpass hmac_md5 cbc_des  192.168.0.12:162");
        map.put("In Address", "10.152.0.162 1161");
        map.put("NE Name", "SGSNMME15BV201");
        map.put("Server", "ieatnetsimv044-01");
        map.put("Type", "WPP SGSN 15B-WPP-V2");

        NetworkElementImpl ne = new NetworkElementImpl(null, null);
        netSimResultMapper.populateNetsimNEDetails(ne, map);

        verifyNetworkElement("SGSNMME15BV201",
                "10.152.0.162",
                "15B-WPP-V2",
                "SGSN",
                "WPP",
                "WPP SGSN 15B-WPP-V2",
                ne);
    }


    private void verifyNetworkElement(String expectedName,
                                      String expectedIp,
                                      String expectedMim,
                                      String expectedNodeType,
                                      String expectedTechType,
                                      String expectedType,
                                      NetworkElement ne) {
        assertEquals(expectedName, ne.getName());
        assertEquals(expectedIp, ne.getIp());
        assertEquals(expectedMim, ne.getMim());
        assertEquals(expectedNodeType, ne.getNodeType());
        assertEquals(expectedTechType, ne.getTechType());
        assertEquals(expectedType, ne.getType());
    }



    @Test
    public void verifyStartedNeIsInSimulation(){
        List<String> neDetails = setupMockNetworkElement("CORE-3K-ST-MGw-C1193-15Bx20");
        assertThat(NetSimResultMapper.isStartedNEInSim(networkElement, neDetails)).isTrue();
    }

    @Test
    public void verifyStartedNeIsNotInSimulation(){
        List<String> neDetails = setupMockNetworkElement("Other Simulation");
        assertThat(NetSimResultMapper.isStartedNEInSim(networkElement, neDetails)).isFalse();
    }

    private List<String> setupMockNetworkElement(final String networkElementSimulation) {
        when(networkElement.getSimulationName()).thenReturn(networkElementSimulation);
        List<String> neDetails = new ArrayList<>();
        neDetails.add("192.168.105.211");
        neDetails.add("/netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20");
        return neDetails;
    }


    @Test
    public void verifyNetworkElementIsStarted(){
        CommandOutput commandOutput = setupMockCommandOutput("K3C119303");
        Optional<List<String>> neDetails = NetSimResultMapper.getNeDetailsIfStarted(commandOutput, networkElement);
        assertThat(neDetails).isPresent();
        assertThat(neDetails.get()).hasSize(2);
    }

    private CommandOutput setupMockCommandOutput(final String networkElementName) {
        when(networkElement.getName()).thenReturn(networkElementName);
        return new CommandOutput(
                "    K3C119305                192.168.105.211  /netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20"
                        + "    K3C119304                192.168.105.210  /netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20\n"
                        + "    K3C119303                192.168.105.209  /netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20\n"
                        + "    K3C119302                192.168.105.208  /netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20\n"
                        + "    K3C119301                192.168.105.207  /netsim/netsimdir/CORE-3K-ST-MGw-C1193-15Bx20\n");
    }

    @Test
    public void verifyNetworkElementIsNotStarted(){
        CommandOutput commandOutput = setupMockCommandOutput("NotStarted");
        assertThat(NetSimResultMapper.getNeDetailsIfStarted(commandOutput, networkElement)).isAbsent();
    }

    @Test
    public void verifyStartedNeIsPopulatedIntoList(){
        CommandOutput commandOutput = setupMockCommandOutput("K3C119303");
        List<NetworkElement> nes = new ArrayList<>();
        when(networkElement.getSimulationName()).thenReturn("CORE-3K-ST-MGw-C1193-15Bx20");
        nes.add(networkElement);
        assertThat(new NetSimResultMapper(null, null).populateListOfStartedNEs(commandOutput, nes).size()).isEqualTo(1);
    }

    @Test
    public void verifyNotStartedNeIsNotPopulatedIntoList(){
        CommandOutput commandOutput = setupMockCommandOutput("K3C119319");
        List<NetworkElement> nes = new ArrayList<>();
        when(networkElement.getSimulationName()).thenReturn("CORE-3K-ST-MGw-C1193-15Bx20");
        nes.add(networkElement);
        assertThat(new NetSimResultMapper(null, null).populateListOfStartedNEs(commandOutput, nes).size()).isEqualTo(0);
    }

    @Test
    public void verifyInValidNeDataIsNotPopulatedIntoList(){
        CommandOutput commandOutput = new CommandOutput(
                "=======================================");
        List<NetworkElement> nes = new ArrayList<>();
        when(networkElement.getName()).thenReturn("K3C119319");
        when(networkElement.getSimulationName()).thenReturn("CORE-3K-ST-MGw-C1193-15Bx20");
        nes.add(networkElement);
        assertThat(new NetSimResultMapper(null, null).populateListOfStartedNEs(commandOutput, nes).size()).isEqualTo(0);
    }


}
