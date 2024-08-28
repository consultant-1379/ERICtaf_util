package com.ericsson.cifwk.taf.utils.cluster;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

public class GatewayUtility {

    public static final String PRIVATE_NET = "192.168.0";

    /**
     * Method to check if current JVM has access to network interface connected
     * to network specified as parameter
     * 
     * @param networkIpPrefix
     * @return
     */
    public static boolean isInsideNetwork(String networkIpPrefix) {
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface
                    .getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(iface
                        .getInetAddresses())) {
                    if (address.getHostAddress().startsWith(networkIpPrefix)) {
                        return true;
                    }
                }
            }

        } catch (SocketException ignore) {
        }
        return false;
    }

    /**
     * Method to check if current JVM has access to network interface connected
     * to private network
     * 
     * @return
     */
    public static boolean isInPrivateNetwork() {
        return isInsideNetwork(PRIVATE_NET);
    }

}
