package com.ericsson.cifwk.taf.handlers.netsim;

import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;

/**
 *
 */
public class NetSimSecurityCommandsSyntaxTest {

    @Test
    public void shouldGenerate_setssliopCreateormodifyCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopCreateormodify("security"),
            ".setssliop createormodify security");
    }

    @Test
    public void shouldGenerate_setssliopDescriptionCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopDescription("description"),
            ".setssliop description description");
    }

    @Test
    public void shouldGenerate_setssliopClientcertfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientcertfile("/netsim/netsim_security/ossmaster_pems/certs.pem"),
            ".setssliop clientcertfile /netsim/netsim_security/ossmaster_pems/certs.pem");
    }

    @Test
    public void shouldGenerate_setssliopClientcacertfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientcacertfile("/netsim/netsim_security/ossmaster_pems/certs.pem"),
            ".setssliop clientcacertfile /netsim/netsim_security/ossmaster_pems/certs.pem");
    }

    @Test
    public void shouldGenerate_setssliopClientkeyfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientkeyfile("/netsim/netsim_security/ossmaster_pems/cacert.pem"),
            ".setssliop clientkeyfile /netsim/netsim_security/ossmaster_pems/cacert.pem");
    }

    @Test
    public void shouldGenerate_setsetssliopClientpasswordCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientpassword("netsim"),
            ".setssliop clientpassword netsim");
    }

    @Test
    public void shouldGenerate_setssliopClientverifyCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientverify("2"),
            ".setssliop clientverify 2");
    }

    @Test
    public void shouldGenerate_setssliopClientdepthCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopClientdepth("1"),
            ".setssliop clientdepth 1");
    }

    @Test
    public void shouldGenerate_setssliopServercertfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopServercertfile("/netsim/netsim_security/ossmaster_pems/certs.pem"),
            ".setssliop servercertfile /netsim/netsim_security/ossmaster_pems/certs.pem");
    }

    @Test
    public void shouldGenerate_setssliopServercacertfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopServercacertfile("/netsim/netsim_security/ossmaster_pems/certs.pem"),
            ".setssliop servercacertfile /netsim/netsim_security/ossmaster_pems/certs.pem");
    }

    @Test
    public void shouldGenerate_setssliopServerkeyfileCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopServerkeyfile("/netsim/netsim_security/ossmaster_pems/cacert.pem"),
            ".setssliop serverkeyfile /netsim/netsim_security/ossmaster_pems/cacert.pem");
    }

    @Test
    public void shouldGenerate_setsetssliopServerpasswordCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopServerpassword("netsim"),
            ".setssliop serverpassword netsim");
    }

    @Test
    public void shouldGenerate_setssliopSerververifyCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopSerververify("2"),
            ".setssliop serververify 2");
    }

    @Test
    public void shouldGenerate_setssliopServerdepthCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopServerdepth("1"),
            ".setssliop serverdepth 1");
    }

    @Test
    public void shouldGenerate_setssliopProtocol_versionCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopProtocol_version("sslv2|sslv3|tlsv1"),
            ".setssliop protocol_version sslv2|sslv3|tlsv1");
    }

    @Test
    public void shouldGenerate_setssliopSaveForceCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.setssliopSaveForce(),
            ".setssliop save force ");
    }

    @Test
    public void shouldGenerate_showssliopCommand() {
        CommandSyntaxTest.checkCommand(
            NetSimCommands.showSsliop("securityDefinitions1422284754315"),
            ".show ssliop securityDefinitions1422284754315");
    }

}
