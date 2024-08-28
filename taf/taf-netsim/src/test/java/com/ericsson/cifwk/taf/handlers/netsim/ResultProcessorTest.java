package com.ericsson.cifwk.taf.handlers.netsim;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 *
 */
public class ResultProcessorTest {

    @Test
    public void shouldSplitByLines() throws Exception {
        List<String> result = ResultProcessor.parseLines(">>  .show simulations \n" + "LTEB154-V2x10-FT-TDD-LTE12\n"
                + "LTEB16x10-FT-TDD-LTE14\n" + "RNCM1200x1-FT-RBSS126x15-RXIJ1141x2-RNC12\n" + "default");

        assertThat(result.size(), equalTo(4));
        assertThat(result, hasItem("LTEB154-V2x10-FT-TDD-LTE12"));
        assertThat(result, hasItem("LTEB16x10-FT-TDD-LTE14"));
        assertThat(result, hasItem("RNCM1200x1-FT-RBSS126x15-RXIJ1141x2-RNC12"));
        assertThat(result, hasItem("default"));
    }

    @Test
    public void shouldSplitColumns() throws Exception {
        String output = ">> .show simnes \n"
                + "NE Name                  Type             Server         In Address       Default dest.\n"
                + "LTE12ERBS00001           LTE ERBS B154-V2 netsim         192.168.100.226  \n"
                + "LTE12ERBS00002           LTE ERBS B154-V2 netsim         192.168.100.227  \n"
                + "LTE12ERBS00003           LTE ERBS B154-V2 netsim         192.168.100.228  \n" + "OK\n";

        List<Map<String, String>> result = ResultProcessor.parseColumns(output);

        assertThat(result.size(), equalTo(3));
        assertThat(result.get(0).get("NE Name"), equalTo("LTE12ERBS00001"));
        assertThat(result.get(1).get("Type"), equalTo("LTE ERBS B154-V2"));
        assertThat(result.get(2).get("In Address"), equalTo("192.168.100.228"));
    }

    @Test
    public void shouldSplitColumnsWithLengthyText() throws Exception {
        String output = ">> .show simnes \n"
                + "NE Name                  Type             Server         In Address       Default dest.\n"
                + "LTE12ERBS00001           LTE ERBS B154-V2 ieatnetsimv011-01 192.168.100.226  \n"
                + "LTE12ERBS00002           LTE ERBS B154-V2 ieatnetsimv011-01 192.168.100.227  \n"
                + "LTE12ERBS00003           LTE ERBS B154-V2 ieatnetsimv011-01 192.168.100.228  \n" + "OK\n";

        List<Map<String, String>> result = ResultProcessor.parseColumns(output);

        assertThat(result.size(), equalTo(3));
        assertThat(result.get(0).get("NE Name"), equalTo("LTE12ERBS00001"));
        assertThat(result.get(0).get("Server"), equalTo("ieatnetsimv011-01"));
        assertThat(result.get(1).get("Type"), equalTo("LTE ERBS B154-V2"));
        assertThat(result.get(2).get("In Address"), equalTo("192.168.100.228"));
    }

    @Test
    public void shouldSplitColumns_Empty() throws Exception {
        String output = ">> .show simnes \n"
                + "NE Name                  Type             Server         In Address       Default dest.\n" + "OK\n";
        List<Map<String, String>> result = ResultProcessor.parseColumns(output);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void shouldSplitSections() throws Exception {
        String output = ">>  .show started \n"
                + "\n"
                + "'server_00036_WCDMA_RBS_S126-lim_-security@netsim'\n"
                + "=================================================================\n"
                + "    NE                       Address          Simulation/Commands \n"
                + "    RNC12RBS05               192.168.100.109  /netsim/netsimdir/RNCM1200x1-FT-RBSS126x15-RXIJ1141x2-RNC12\n"
                + "\n" + "'server_00033_LTE_ERBS_B154-V2_-security@netsim'\n"
                + "=================================================================\n"
                + "    NE                       Address          Simulation/Commands \n"
                + "    LTE12ERBS00005           192.168.100.230  /netsim/netsimdir/LTEB154-V2x10-FT-TDD-LTE12\n"
                + "    LTE12ERBS00002           192.168.100.227  /netsim/netsimdir/LTEB154-V2x10-FT-TDD-LTE12\n"
                + "END\n";

        Map<String, List<Map<String, String>>> result = ResultProcessor.parseSections(output);

        assertThat(result.size(), equalTo(2));
        String KEY_ONE = "'server_00036_WCDMA_RBS_S126-lim_-security@netsim'";
        String KEY_TWO = "'server_00033_LTE_ERBS_B154-V2_-security@netsim'";
        assertThat(result.keySet(), hasItem(KEY_ONE));
        assertThat(result.keySet(), hasItem(KEY_TWO));
        assertThat(result.get(KEY_ONE).size(), equalTo(1));
        assertThat(result.get(KEY_TWO).size(), equalTo(2));
    }

    @Test
    public void shouldSplitRows() throws Exception {
        String output = ">>  .show started \n\r" + "\n\r"
                + "'server_00017_LTE_ERBS_D125-V2@netsim' for LTE ERBS D125-V2\n\r"
                + "=================================================================\n\r"
                + "    NE                       Address          Simulation/Commands \n\r"
                + "    LTE02ERBS00160           192.168.100.175  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02\n\r"
                + "                                  gencmdshell\n\r"
                + "    LTE02ERBS00131           192.168.100.146  /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02\n\r"
                + "                                  netconf\n\r"
                + "    LTE02ERBS00052           192.168.100.67   /netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02\n\r"
                + "END\n\r";

        Map<String, List<Map<String, String>>> result = ResultProcessor.parseSections(output);

        assertThat(result.size(), equalTo(1));
        String KEY_NAME = "'server_00017_LTE_ERBS_D125-V2@netsim' for LTE ERBS D125-V2";
        assertThat(result.keySet(), hasItem(KEY_NAME));
        assertThat(result.get(KEY_NAME).size(), equalTo(3));
        assertThat(result.get(KEY_NAME).get(0).get("NE"), equalTo("LTE02ERBS00160"));
        assertThat(result.get(KEY_NAME).get(1).get("Address"), equalTo("192.168.100.146"));
        assertThat(result.get(KEY_NAME).get(2).get("Simulation/Commands"),
                equalTo("/netsim/netsimdir/LTED125-V2x160-ST-FDD-LTE02"));
    }

    @Test
    public void shouldSplitByRows() throws Exception {
        String output = ">> .show fs\n"
                + "fs        /netsim/netsim_dbdir/simdir/netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01/LTE01ERBS00001/fs\n"
                + "tmpfs     off a b c\n" + "yetnewfield";

        Map<String, List<String>> rows = ResultProcessor.parseRows(output);
        assertThat(rows.size(), equalTo(3));
        String ROW1_NAME = "fs";
        String ROW2_NAME = "tmpfs";
        String ROW3_NAME = "yetnewfield";
        assertThat(rows.keySet(), hasItem(ROW1_NAME));
        assertThat(rows.get(ROW1_NAME).get(0),
                equalTo("/netsim/netsim_dbdir/simdir/netsim/netsimdir/LTED1180-V2x10-FT-FDD-LTE01/LTE01ERBS00001/fs"));
        assertThat(rows.keySet(), hasItem(ROW2_NAME));
        assertThat(rows.get(ROW2_NAME).get(0), equalTo("off a b c"));
        assertThat(rows.keySet(), hasItem(ROW3_NAME));
        assertThat(rows.get(ROW3_NAME), is(empty()));
    }

    @Test
    public void shouldSplitToList() {
        String line = "=================================================================";
        List<String> splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(1));

        line = "NE                       Address          Simulation/Commands---";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "LTE10ERBS00010           2001:1b70:82a1:103::64:1 /netsim/netsimdir/LTEH140-limx10-FT-FDD-LTE10";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "LTE10ERBS00005           192.168.100.15   /netsim/netsimdir/LTEH140-limx10-FT-FDD-LTE10";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "SGSN-15B-CP01-V301       192.168.105.6 25161 public v3+v2+v1 .128.0.0.193.1.192.168.105.6 CORE-ST-4.5K-SGSN-15B-CP01-V3x25_SGSN-15B-CP01-V301 authpass privpass hmac_md5 cbc_des  /netsim/netsimdir/CORE-ST-4.5K-SGSN-15B-CP01-V3x25";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "SGSN-15B-CP01-V325       2001:1b70:82a1:103::64:183 25161 public v3+v2+v1 .128.0.0.193.2.32.1.27.112.130.161.1.3.0.0.0.0.0.100.1.131 CORE-ST-4.5K-SGSN-15B-CP01-V3x25_SGSN-15B-CP01-V325 authpass privpass hmac_md5 cbc_des  /netsim/netsimdir/CORE-ST-4.5K-SGSN-15B-CP01-V3x25---";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "K3C114101                192.168.105.88   /netsim/netsimdir/CORE-3K-ST-MGw-C1141-14Bx20";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(3));

        line = "'server_00188_LTE_ERBS_H140-lim@netsim' for LTE ERBS H140-lim";
        splitToList = ResultProcessor.splitToList(line);
        assertThat(line + " was not split correctly", splitToList.size(), is(1));
    }

}
