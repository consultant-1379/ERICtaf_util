package com.ericsson.cifwk.taf.handlers.netsim.implementation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;

@RunWith(MockitoJUnitRunner.class)
public class NetsimShowAllSimNesOutputProcessorTest {

    @Mock AppenderSkeleton appender;
    @Captor ArgumentCaptor<LoggingEvent> logCaptor;

    private final String RAW_OUTPUT = ""
            + "RNCV42740x1-FT-RBSU190x10-RXIK190x2-RNC05\n"
            + "=====================\n"
            + "NE          Address         Server\n"
            + "RNC05       192.168.100.73  server_00334_WCDMA_RNC_V42740-V1lim_-security@netsim\n"
            + "RNC05RBS01  192.168.100.74  server_00335_WCDMA_RBS_U190_-security@netsim\n"
            + "RNC05RBS02  192.168.100.75  not started\n"
            + "RNC05RBS03  192.168.100.76  server_00335_WCDMA_RBS_U190_-security@netsim\n"
            + "\n"
            + "CORE-FT-SGSN-COMECIM-13Bx5-vApp\n"
            + "=====================\n"
            + "NE         Address                                                                                                                                      Server                  \n"
            + "SGSN13B01  192.168.101.232 1161 public v2 .128.0.0.193.1.192.168.101.232 CORE-FT-SGSN-COMECIM-13Bx5-vApp_SGSN13B01 authpass privpass hmac_md5 cbc_des   server_00108_WPP_SGSN_13B-WPP-2@netsim\n"
            + "SGSN13B02  192.168.101.233 1161 public v2 .128.0.0.193.1.192.168.101.233 CORE-FT-SGSN-COMECIM-13Bx5-vApp_SGSN13B02 authpass privpass hmac_md5 cbc_des   server_00108_WPP_SGSN_13B-WPP-2@netsim\n"
            + "SGSN13B03  192.168.101.234 1161 public v2 .128.0.0.193.1.192.168.101.234 CORE-FT-SGSN-COMECIM-13Bx5-vApp_SGSN13B03 authpass privpass hmac_md5 cbc_des   server_00108_WPP_SGSN_13B-WPP-2@netsim\n"
            + "SGSN13B04  192.168.101.235 1161 public v2 .128.0.0.193.1.192.168.101.235 CORE-FT-SGSN-COMECIM-13Bx5-vApp_SGSN13B04 authpass privpass hmac_md5 cbc_des   server_00108_WPP_SGSN_13B-WPP-2@netsim\n"
            + "\n"
            + "LTEC1127x10-FT-FDD-LTE11\n"
            + "=====================\n"
            + "NE              Address          Server\n"
            + "LTE11ERBS00001  192.168.100.179  server_00109_LTE_ERBS_C1127_-security@netsim\n"
            + "LTE11ERBS00002  192.168.100.180  server_00109_LTE_ERBS_C1127_-security@netsim\n"
            + "LTE11ERBS00003  192.168.100.181  server_00109_LTE_ERBS_C1127_-security@netsim\n"
            + "LTE11ERBS00004  192.168.100.182  server_00109_LTE_ERBS_C1127_-security@netsim\n";

    private NeGroup allNEs;

    @Before
    public void setup() {
        final Map<NetSimContext, NetSimResult> showAllSimNesCommandResult = new HashMap<>();
        final NetSimResult result = new NetSimResult(RAW_OUTPUT);
        showAllSimNesCommandResult.put(mock(NetSimContext.class), result);

        final NetsimShowAllSimNesOutputProcessor sut = new NetsimShowAllSimNesOutputProcessor(showAllSimNesCommandResult);
        allNEs = sut.getAllStartedNEs();
    }

    @Test
    public void test_OnlyStarted() {
        assertEquals(11, allNEs.size());
    }

    @Test
    public void test_Rnc() {
        final NetworkElement rnc = allNEs.get("RNC05");
        assertEquals("RNCV42740x1-FT-RBSU190x10-RXIK190x2-RNC05", rnc.getSimulationName());
        assertEquals("WCDMA RNC V42740-V1lim", rnc.getType());
        assertEquals("WCDMA", rnc.getTechType());
        assertEquals("RNC", rnc.getNodeType());
        assertEquals("V42740-V1lim", rnc.getMim());
        assertEquals("192.168.100.73", rnc.getIp());
    }

    @Test
    public void test_Sgsn() {
        final NetworkElement sgsn = allNEs.get("SGSN13B02");
        assertEquals("CORE-FT-SGSN-COMECIM-13Bx5-vApp", sgsn.getSimulationName());
        assertEquals("WPP SGSN 13B-WPP-2", sgsn.getType());
        assertEquals("WPP", sgsn.getTechType());
        assertEquals("SGSN", sgsn.getNodeType());
        assertEquals("13B-WPP-2", sgsn.getMim());
        assertEquals("192.168.101.233", sgsn.getIp());
    }

    @Test
    public void test_Erbs() {
        final NetworkElement erbs = allNEs.get("LTE11ERBS00003");
        assertEquals("LTEC1127x10-FT-FDD-LTE11", erbs.getSimulationName());
        assertEquals("LTE ERBS C1127", erbs.getType());
        assertEquals("LTE", erbs.getTechType());
        assertEquals("ERBS", erbs.getNodeType());
        assertEquals("C1127", erbs.getMim());
        assertEquals("192.168.100.181", erbs.getIp());
    }

    @Test
    public void test_Broken_NETSim() {
        Logger.getRootLogger().addAppender(appender);

        String BROKEN_RAW_OUTPUT = "Error: {{badmatch,\n"
                + "{error,{invalid_address,\"Bad number of address parameters\"}}},\n"
                + "[{snmp_telnet_prot,effective_address,5,\n"
                + "[{file,\"snmp_telnet_prot.erl\"},{line,232}]},\n"
                + "{protocollib,lookup_effective_address_string_by_nedata,3,\n"
                + "[{file,\"protocollib.erl\"},{line,657}]},\n"
                + "{'show-allsimnes','get_simne_data/2-lc$^0/1-0',2,\n"
                + "[{file,\"show-allsimnes.erl\"},{line,154}]},\n"
                + "{'show-allsimnes','get_simulations_and_ne_data/1-fun-0',1,\n"
                + "[{file,\"show-allsimnes.erl\"},{line,133}]},\n"
                + "{lists,map,2,[{file,\"lists.erl\"},{line,1224}]},\n"
                + "{'show-allsimnes',plugcmdstart,4,\n"
                + "[{file,\"show-allsimnes.erl\"},{line,55}]},\n"
                + "{coordcommand,execute_possible_internal,4,\n"
                + "[{file,\"coordcommand.erl\"},{line,392}]},\n"
                + "{coordcommand,do_command1,4,[{file,\"coordcommand.erl\"},{line,128}]}]}\n";

        Map<NetSimContext, NetSimResult> showAllSimNesCommandResult1 = new HashMap<>();
        NetSimResult result1 = new NetSimResult(BROKEN_RAW_OUTPUT);
        showAllSimNesCommandResult1.put(mock(NetSimContext.class), result1);

        NetsimShowAllSimNesOutputProcessor sut1 = new NetsimShowAllSimNesOutputProcessor(showAllSimNesCommandResult1);
        NeGroup allNEs1 = sut1.getAllStartedNEs();
        NetworkElement erbs1 = allNEs1.get("LTE11ERBS00003");
        assertNull(erbs1);
        verify(appender, atLeast(1)).doAppend(logCaptor.capture());

        assertThat("Error message should have been logged", logCaptor.getAllValues().get(0).getRenderedMessage(),
                containsString("Error occured, could not retrieve NE"));
        Logger.getRootLogger().removeAppender(appender);
    }
}
