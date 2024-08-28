package com.ericsson.cifwk.taf.osgi.client;

import com.ericsson.cifwk.taf.data.Host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Deploy taf-osgi-agent
 */
public class ApiContainerClient extends OsgiContainerClient {
    
    private final static Logger LOG = LoggerFactory.getLogger(ApiContainerClient.class);

    public static final String AGENT_URI = "/agent";
    private static final String GROOVY_BUNDLE = "groovy-all";
    private static final String AGENT_JAR = "taf-osgi-agent";
    private static ThreadLocal<ApiContainerClient> instance = new ThreadLocal<>();
    private static List<String> bundles = new ArrayList<>();

    @Override
    public List<String> getBundles() {
        bundles.add(GROOVY_BUNDLE);
        bundles.add(AGENT_JAR);
        return bundles;
    }

    private ApiContainerClient(Host host, String startupScriptPath, String configPath){
        super(host, startupScriptPath, configPath);
        instance.set(this);
    }

    public static ApiContainerClient getInstance() {
        return instance.get();
    }

    public static synchronized String constructEndpoint(Host host,
                                                        String startupScriptPath, String configPath) {
        ApiContainerClient localInstance = instance.get();
        if (localInstance == null) {
            localInstance = new ApiContainerClient(host, startupScriptPath,
                    configPath);
            instance.set(localInstance);
        }
        return "http://" + host.getIp() + ":" + localInstance.getDataPort()
                + AGENT_URI;
    }
}
