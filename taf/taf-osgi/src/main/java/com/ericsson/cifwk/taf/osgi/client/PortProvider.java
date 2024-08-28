package com.ericsson.cifwk.taf.osgi.client;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.utils.cluster.FreePortFinder;
import com.ericsson.cifwk.taf.utils.cluster.GatewayUtility;

public class PortProvider {

    /**
     * Get free CLI Port for OSGi container
     * 
     * @param host
     * @return
     */
    public static int getCliPort(final Host host) {
        return getNextFreePort(host);
    }

    /**
     * Get free Data port for OSGi container
     * 
     * @param host
     * @return
     */
    public static int getDataPort(final Host host) {
        return getNextFreePort(host);
    }

    private static Host gateway;

    private static Host getGateway() {
        if (gateway == null) {
            gateway = DataHandler.getHostByType(HostType.GATEWAY);
        }
        return gateway;
    }

    public static final int DEFAULT_START_PORT = 10_000;
    private static int lastFreePort = DEFAULT_START_PORT;
    private static final String NAT_MS_IP = "192.168.0.5";

    private static boolean requiresNat(final Host host) {
        return getGateway() != null && host.getIp().equalsIgnoreCase(getGateway().getIp())
                && !GatewayUtility.isInPrivateNetwork();
    }

    private synchronized static int getNextFreePort(final Host host) {
        if (requiresNat(host)) {
            String gwRootPass = getGateway().getUsers(UserType.ADMIN).get(0)
                    .getPassword();
            lastFreePort = FreePortFinder.findFreePort(lastFreePort,
                    getGateway().getIp(), gwRootPass, NAT_MS_IP);
        } else {
            lastFreePort = FreePortFinder.findFreePort(host.getIp(),
                    lastFreePort);
        }
        return lastFreePort++;
    }

}
