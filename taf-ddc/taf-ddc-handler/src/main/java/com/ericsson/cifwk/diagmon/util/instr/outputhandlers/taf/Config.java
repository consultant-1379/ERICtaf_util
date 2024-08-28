package com.ericsson.cifwk.diagmon.util.instr.outputhandlers.taf;

import com.ericsson.cifwk.diagmon.util.common.TreeNode;
import com.ericsson.cifwk.diagmon.util.instr.config.ConfigTreeNode;
import com.ericsson.cifwk.taf.ddc.Messages;
import com.google.common.base.Strings;

import java.net.InetAddress;

public class Config {

    ConfigTreeNode config;

    public Config(ConfigTreeNode config) {
        this.config = config;
    }

    public String getMonitoredHost() {
        String host = null;
        try {
            if (Strings.isNullOrEmpty(host)) {
                for (TreeNode node : config.getParent().getChildren()) {
                    if ("ipService".equalsIgnoreCase(node.baseName())) {
                        host = ((ConfigTreeNode) node).getAttribute("host");
                        host = InetAddress.getByName(host).getHostName();
                        if (host != null) host = host.split("\\.")[0];
                    }
                }
            }
            if (Strings.isNullOrEmpty(host) || host.trim().isEmpty()) {
                host = "localhost";
                host = InetAddress.getLocalHost().getHostName();
                host = host.split("\\.")[0];
            }
        } catch (Exception e) {
            throw new RuntimeException(Messages.format("host.error", host), e);
        }
        return host;
    }

}

