package com.ericsson.cifwk.taf.handlers.netsim;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FDNParsingUtilitiesTest {

    @Test
    public void shouldParseNetworkElementIdFromSimpleFDN() {
        assertThat(FDNParsingUtilities.getNetworkElementId("NetworkElement=LTE01ERBS02")).isEqualTo("LTE01ERBS02");
    }

    @Test
    public void shouldParseNetworkElementIdFromMeContext() {
        assertThat(FDNParsingUtilities.getNetworkElementId("MeContext=LTE01ERBS02")).isEqualTo("LTE01ERBS02");
    }

    @Test
    public void shouldParseNetworkElementIdFromManagedElement() {
        assertThat(FDNParsingUtilities.getNetworkElementId("ManagedElement=LTE01ERBS02")).isEqualTo("LTE01ERBS02");
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotParseNetworkElementIdFromFDNWithNoNetworkElementId() {
        assertThat(FDNParsingUtilities.getNetworkElementId("Inventory=1")).isEqualTo("");
    }

    @Test
    public void shouldParseNetworkElementIdFromComplexFDNWithMeContextAndManagedElement() {
        assertThat(FDNParsingUtilities.getNetworkElementId("MeContext=LTE01ERBS02,ManagedElement=1,ENodeBFunction=1"))
                .isEqualTo("LTE01ERBS02");
    }

    @Test
    public void shouldParseMOValueFromFDN() {
        assertThat(FDNParsingUtilities
                .getMoValue("EUtranCellFDD", "MeContext=LTE32ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE32ERBS00001-1"))
                .matches("LTE32ERBS00001-1");
    }

    @Test
    public void shouldParseMOValueFromFDNCaseInsensitive() {
        assertThat(FDNParsingUtilities
                .getMoValue("eutrancellfdd", "MeContext=LTE32ERBS00001,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=LTE32ERBS00001-1"))
                .matches("LTE32ERBS00001-1");
    }

    @Test
    public void shouldParseLDNFromFDNWithMeContext(){
        assertThat(FDNParsingUtilities.getLdn("MeContext=LTE01ERBS1,ManagedElement=1,ENodeBFunction=1")).matches("ManagedElement=1,ENodeBFunction=1");
    }

    @Test
    public void shouldParseLDNFromFDNWithSubNetwork(){
        assertThat(FDNParsingUtilities.getLdn("SubNetwork=SGSN01,MeContext=SGSN01,ManagedElement=1,SgsnMme=1")).matches("ManagedElement=1,SgsnMme=1");
    }
}
