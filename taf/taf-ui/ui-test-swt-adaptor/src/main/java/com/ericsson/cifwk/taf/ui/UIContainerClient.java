package com.ericsson.cifwk.taf.ui;


import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;
import com.ericsson.cifwk.taf.osgi.client.OsgiContainerClient;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Deploy taf-swt-agent
 */
@API(Stable)
public class UIContainerClient extends OsgiContainerClient {

    private static final String GSON_BUNDLE = "gson";
    private static final String GUAVA_BUNDLE = "guava";
    private static final String AGENT_JAR = "taf-swt-agent";
    private static ThreadLocal<UIContainerClient> instance = new ThreadLocal<>();
    private static List<String> bundles = new ArrayList<>();

    private UIContainerClient(Host host, String startupScriptPath, String configPath) {
        super(host.getHostname(), host.getHttpPort(), startupScriptPath, configPath);
    }

    private UIContainerClient(String host, int port, String startupScriptPath, String configPath) {
        super(host, port, startupScriptPath, configPath);
        instance.set(this);
    }

    @Override
    public List<String> getBundles() {
        bundles.add(GSON_BUNDLE);
        bundles.add(GUAVA_BUNDLE);
        bundles.add(AGENT_JAR);
        return bundles;
    }

    public static synchronized UIContainerClient construct(Host host, String startupScriptPath, String configPath)
            throws ContainerNotReadyException {
        return UIContainerClient.construct(host.getHostname(), host.getHttpPort(), startupScriptPath, configPath);
    }

    public static synchronized UIContainerClient construct(String host, int port, String startupScriptPath,
                                                              String configPath)
            throws ContainerNotReadyException {
        UIContainerClient localInstance = instance.get();
        if (localInstance == null) {
            localInstance = new UIContainerClient(host, port, startupScriptPath, configPath);
            instance.set(localInstance);
        }
        return localInstance;
    }

    public String getHostName(){
        return host.getIp();
    }
}
