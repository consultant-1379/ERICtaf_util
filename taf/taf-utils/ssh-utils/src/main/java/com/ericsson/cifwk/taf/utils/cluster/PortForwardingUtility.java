package com.ericsson.cifwk.taf.utils.cluster;

import com.ericsson.cifwk.taf.utils.InternalStringUtils;
import com.ericsson.cifwk.taf.utils.ssh.J2SshCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by erafkos on 26/10/2012.
 */
public class PortForwardingUtility {
    private static Logger LOGGER = LoggerFactory.getLogger(PortForwardingUtility.class);

    public static final String IPTABLES = "iptables";
    public static final String TABLE = "-t nat";
    public static final String INSERT = "-I";
    public static final String LIST = "-L";
    public static final String DNAT = "-j DNAT --to-destination";
    public static final String TCP = "-p tcp";
    public static final String DST_PORT = "--dport";
    public static final String DPT = "dpt:";
    public static final String LIST_AS_NUMBERS = "-n";
    public static final String CHAIN = "PREROUTING";

    public static final int DEFAULT_LOOKUP_PORT = 10_000;
    public int firstFreePortLookup = DEFAULT_LOOKUP_PORT;

    public static final String jbossHttpWithoutOffset = "8080";

    private J2SshCommandExecutor ssh;
    private String iptablesList;

    private boolean modifiedIpTables = false;

    /**
     * Method for configuring port forwarding on gateway
     *
     * @param targetIp            Ip of machine behind gateway
     * @param targetPort          port on the machine behind gateway
     * @return configured port number
     */
    public String configureForwarding(String targetIp, String targetPort, String gatewayIp, String gatewayRootPassword, int jbossOffset) {
        return configureForwarding(targetIp, targetPort, String.valueOf(DEFAULT_LOOKUP_PORT), gatewayIp, gatewayRootPassword, jbossOffset);
    }

    /**
     * Method for configuring port forwarding on gateway
     *
     * @param targetIp            Ip of machine behind gateway
     * @param targetPort          port on the machine behind gateway
     * @return configured port number
     */
    public String configureForwarding(String targetIp, String targetPort, String destinationPort, String gatewayIp, String gatewayRootPassword) {
        return configureForwarding(targetIp, targetPort, destinationPort, gatewayIp, gatewayRootPassword, -1);
    }

    /**
     * Method for configuring port forwarding on gateway
     *
     * @param targetIp            Ip of machine behind gateway
     * @param targetPort          port on the machine behind gateway
     * @return configured port number
     */
    public String configureForwarding(String targetIp, String targetPort, String gatewayIp, String gatewayRootPassword) {
        return configureForwarding(targetIp, targetPort, String.valueOf(DEFAULT_LOOKUP_PORT), gatewayIp, gatewayRootPassword, -1);
    }

    /**
     * Method for configuring port forwarding on gateway
     *
     * @param targetIp            Ip of machine behind gateway
     * @param targetPort          port on the machine behind gateway
     * @return configured port number
     */
    public String configureForwarding(String targetIp, String targetPort, String destinationPort, String gatewayIp, String gatewayRootPassword, int jbossOffset) {
        establishSshConnection(gatewayIp, gatewayRootPassword);
        String port = isAlreadyThere(targetIp, targetPort);
        modifiedIpTables = port != null;
        LOGGER.debug("Found port", port);
        if (InternalStringUtils.isNotBlank(port)) {
            firstFreePortLookup = Integer.valueOf(destinationPort);
            port = getCalculatedPort(targetIp, targetPort, jbossOffset);
            LOGGER.debug("Configuring port with", port);
            doConfigure(port, targetIp, targetPort);
            iptablesList = null;
            modifiedIpTables = true;
        }
        ssh.disconnect();
        return port;
    }

    public void deleteForwarding(String targetIp, String targetPort, String destinationPort, String gatewayIp, String gatewayRootPassword) {
        establishSshConnection(gatewayIp, gatewayRootPassword);
        ssh.sendCommand(getDeleteRuleCommand(targetIp, targetPort, destinationPort));
        ssh.disconnect();
    }

    public boolean hasModifiedIpTables() {
        return modifiedIpTables;
    }

    protected String getDeleteRuleCommand(String targetIp, String targetPort, String destinationPort) {
        return InternalStringUtils.join(Arrays.asList(IPTABLES, TABLE, "-D", CHAIN, TCP, DST_PORT, destinationPort, DNAT, targetIp + ":" + targetPort), ' ');
    }

    /**
     * Get jboss HTTP port forwarding for using offset
     */
    private String getJbossHttp(String jbossIp, int jbossOffset) {
        int wantedPort = Integer.valueOf(jbossHttpWithoutOffset) + jbossOffset;
        return isAlreadyThere(jbossIp, String.valueOf(wantedPort));
    }

    /**
     * Method to get currently configured iptables
     */
    private String getListCommand() {
        if (!InternalStringUtils.isEmpty(iptablesList)) {
            String listCommand = InternalStringUtils.join(Arrays.asList(IPTABLES, TABLE, LIST, CHAIN, LIST_AS_NUMBERS), ' ');
            ssh.sendCommand(listCommand);
            iptablesList = ssh.getStdOut();
        }
        return iptablesList;
    }

    /**
     * Method to check if port on gateway is not used for forwarding
     */
    private boolean isPortFree(String port) {
        return getListCommand().contains(DPT + port);
    }

    /**
     * Method to establish SSH connection to gateway
     *
     */
    private void establishSshConnection(String gatewayIp, String gatewayRootPassword) {
        ssh = new J2SshCommandExecutor();
        ssh.setHostName(gatewayIp);
        ssh.setUser("root");
        ssh.setPass(gatewayRootPassword);
    }

    /**
     * Execute iptables command for configuration
     *
     */
    private void doConfigure(String port, String targetIp, String targetPort) {
        String command = InternalStringUtils.join(Arrays.asList(IPTABLES, TABLE, INSERT, CHAIN, TCP, DST_PORT, port, DNAT, targetIp + ":" + targetPort), ' ');
        ssh.sendCommand(command);
    }

    /**
     * Get first free port from iptables
     */
    private String getFirstFreePort() {
        return getFirstFreePort(firstFreePortLookup);
    }

    /**
     * Get first free port from iptables
     */
    private String getFirstFreePort(int startingPort) {
        int currentPort = startingPort;
        while (!isPortFree(String.valueOf(currentPort))) {
            currentPort++;
        }
        return String.valueOf(currentPort);
    }

    /**
     * Method to get port that should be used for particular ip and port
     */
    private String getCalculatedPort(String targetIp, String targetPort, int jbossOffset) {
        String currentPort = getFirstFreePort();
        if (jbossOffset >= 0) {
            LOGGER.debug("Jboss Offset used. Looking for jboss http port");
            String jbossHttp = getJbossHttp(targetIp, jbossOffset);
            if (InternalStringUtils.isNotBlank(jbossHttp)) {
                LOGGER.debug("Setting jboss http passtrough");
                jbossHttp = currentPort;
                doConfigure(currentPort, targetIp, jbossHttpWithoutOffset);
            }
            LOGGER.debug("Using jboss Http {} for setting with jboss offset", jbossHttp);
            return String.valueOf(Integer.valueOf(jbossHttp) + jbossOffset);
        }
        return currentPort;
    }

    /**
     * Method to check if forwarding configuration is already there
     */
    private String isAlreadyThere(String targetIp, String targetPort) {
        List<String> commands = new ArrayList<>(Arrays.asList(getListCommand().split("\n")));
        for (String command : commands) {
            if (command.contains(targetIp + ":" + targetPort)) {
                LOGGER.debug("Port there: {}", command);
                return InternalStringUtils.isEmpty(command) ? command : command.split(DPT)[1].split(" ")[0];
            }
        }
        return null;
    }
}
