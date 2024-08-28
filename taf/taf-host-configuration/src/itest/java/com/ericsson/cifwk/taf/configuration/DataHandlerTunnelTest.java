package com.ericsson.cifwk.taf.configuration;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.List;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.de.tools.cli.CliCommandResult;
import com.ericsson.de.tools.cli.CliTool;
import com.ericsson.de.tools.cli.CliTools;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DataHandlerTunnelTest {
    public static final String HOST = null;
    public static final String NODE = null;

    @Before
    public void setUp() throws Exception {
        TafDataHandler.getConfiguration().clear();
    }

    /**
     * This Test intended for manual run because of:
     * Maintaining test environment is time consuming
     * Including this test may result in flaky execution
     * Unlikely that old tunelling functionality will be changed
     * Its not Data Handler responsibility to check tunelling as its com.ericsson.cifwk.taf.data.HostTunnelHelper specific
     * functionality
     */
    @Test
    @Ignore
    public void testThatTunnelActuallyWorks() throws Exception {
        Preconditions.checkNotNull(HOST, "You need to define host and node");
        Preconditions.checkNotNull(NODE, "You need to define host and node");

        TafDataHandler.setAttribute("host.thehost.ip", HOST);
        TafDataHandler.setAttribute("host.thehost.port.ssh", "22");
        TafDataHandler.setAttribute("host.thehost.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.thehost.user.root.type", "admin");
        TafDataHandler.setAttribute("host.thehost.node.thenode.tunnel", "1");
        TafDataHandler.setAttribute("host.thehost.node.thenode.name", NODE);
        TafDataHandler.setAttribute("host.thehost.node.thenode.ip", NODE);
        TafDataHandler.setAttribute("host.thehost.node.thenode.port.ssh", "22");

        Host theNode = TafDataHandler.getHostByName("thenode");

        assertThat(theNode.getIp()).isEqualTo("127.0.0.1");

        CliTool tool = CliTools.simpleExecutor(theNode.getIp())
                .withPort(theNode.getPort(Ports.SSH))
                .withUsername("root")
                .withPassword("shroot")
                .build();

        CliCommandResult getHostName = tool.execute("hostname");
        assertThat(getHostName.getOutput()).isEqualTo(NODE);
    }


    /**
     * Connection to 0.0.0.0 expected to fail, attributes should be set regardless of that
     */
    @Test
    @Ignore
    public void tunnelSingleInstance() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.port.ssh", "22");
        TafDataHandler.setAttribute("host.thehost.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.thehost.user.root.type", "admin");
        TafDataHandler.setAttribute("host.thehost.node.thenode.tunnel", "1");
        TafDataHandler.setAttribute("host.thehost.node.thenode.name", "tafexe1");
        TafDataHandler.setAttribute("host.thehost.node.thenode.ip", "tafexe1");
        TafDataHandler.setAttribute("host.thehost.node.thenode.port.ssh", "22");

        List<Host> allHosts = TafDataHandler.getAllHosts();
        Host theNode1 = findHost(allHosts, "thenode");

        Host theNode2 = TafDataHandler.findHost().withHostname("thenode").get();

        Host theNode3 = TafDataHandler.findHost().withHostname("thenode").get();

        Host thehost = findHost(allHosts, "thehost");
        Host theNode4 = getOnlyElement(thehost.getNodes());


        assertThat(theNode1.getIp()).isEqualTo("127.0.0.1");
        assertThat(theNode2.getIp()).isEqualTo("127.0.0.1");
        assertThat(theNode3.getIp()).isEqualTo("127.0.0.1");
        assertThat(theNode4.getIp()).isEqualTo("127.0.0.1");

        Integer tunnelPort = theNode1.getPort(Ports.SSH);
        assertThat(tunnelPort).isNotEqualTo(22);
        assertThat(theNode2.getPort(Ports.SSH)).isEqualTo(tunnelPort);
        assertThat(theNode3.getPort(Ports.SSH)).isEqualTo(tunnelPort);
        assertThat(theNode4.getPort(Ports.SSH)).isEqualTo(tunnelPort);

        assertThat(theNode1.isTunneled()).isTrue();
        assertThat(theNode2.isTunneled()).isTrue();
        assertThat(theNode3.isTunneled()).isTrue();
        assertThat(theNode4.isTunneled()).isTrue();

        assertThat(theNode1.getOriginalIp()).isEqualTo("tafexe1");
        assertThat(theNode2.getOriginalIp()).isEqualTo("tafexe1");
        assertThat(theNode3.getOriginalIp()).isEqualTo("tafexe1");
        assertThat(theNode4.getOriginalIp()).isEqualTo("tafexe1");

        assertThat(theNode1.getOriginalPort().get(Ports.SSH)).isEqualTo("22");
        assertThat(theNode2.getOriginalPort().get(Ports.SSH)).isEqualTo("22");
        assertThat(theNode3.getOriginalPort().get(Ports.SSH)).isEqualTo("22");
        assertThat(theNode4.getOriginalPort().get(Ports.SSH)).isEqualTo("22");
    }

    private Host findHost(List<Host> allHosts, final String name) {
        return Iterables.find(allHosts, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return name.equals(input.getHostname());
            }
        });
    }
}
