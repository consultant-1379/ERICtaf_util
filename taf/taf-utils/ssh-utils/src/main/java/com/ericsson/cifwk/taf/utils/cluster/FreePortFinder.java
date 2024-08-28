package com.ericsson.cifwk.taf.utils.cluster;

import com.ericsson.cifwk.meta.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class FreePortFinder {

    public static final int MAX_PORT = 65_535;

    private static final Logger log = LoggerFactory
            .getLogger(FreePortFinder.class);

    /**
     * Method to find port without process already using it
     */
    public static final int findFreePort(final String destinationIp,
            final int startingPort) {
        for (int port = startingPort; port < MAX_PORT; port++) {
            if (isPortFree(destinationIp, port)) {
                return port;
            }
        }
        return -1;
    }

    /**
     * Method to find port without process already using it with gateway inside.
     * Appriopriate NAT rules will be added
     */
    public static final int findFreePort(final int startingPort,
            final String gatewayIp, final String gatewayRootPass,
            final String destinationIp) {
        final PortForwardingUtility pf = new PortForwardingUtility();
        for (int port = startingPort; port < MAX_PORT; port++) {
            final String configuredPort = pf.configureForwarding(destinationIp,
                    String.valueOf(port), String.valueOf(port), gatewayIp,
                    gatewayRootPass);
            if (Integer.valueOf(configuredPort) != port) {
                pf.deleteForwarding(destinationIp, String.valueOf(port),
                        configuredPort, gatewayIp, gatewayRootPass);
            }
            if (isPortFree(gatewayIp, Integer.valueOf(configuredPort))) {
                return Integer.valueOf(configuredPort);
            } else {
                log.info("Port {} with forwarding {} is not free", port,
                        configuredPort);
                if (pf.hasModifiedIpTables()) {
                    pf.deleteForwarding(destinationIp, String.valueOf(port),
                            configuredPort, gatewayIp, gatewayRootPass);
                }

            }
        }
        return -1;

    }

    /**
     * Method to check if particular port is not being used by a process
     */
    public static boolean isPortFree(final String ip, final int portToCheck) {
        try {
            new Socket(ip, portToCheck).close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
