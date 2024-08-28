package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static java.lang.String.format;

@API(Stable)
public final class SwtNavigator extends SwtUiNavigator {

    public SwtNavigator(String host, int port) {
        super(host, port);
    }

    public SwtNavigator(Host host, String startupScriptPath, String configPath, String display, Long timeout) {
        super(host.getHostname(), host.getHttpPort(), startupScriptPath, configPath, display, timeout);
    }

    @Override
    protected OsgiHost startContainerOnDemand(String host, int port, String startupScriptPath, String configPath,
                              String display, Long timeout) {
        UIContainerClient client;
        try {
            client = UIContainerClient.construct(host, port, startupScriptPath, configPath);
            client.prepare(display, timeout);
            return new OsgiHost(client.getHostName(), client.getDataPort());
        } catch (ContainerNotReadyException e) {
            String message = format("Could not connect to container with host %s", host);
            throw new RuntimeException(message, e); // NOSONAR
        }
    }


}
