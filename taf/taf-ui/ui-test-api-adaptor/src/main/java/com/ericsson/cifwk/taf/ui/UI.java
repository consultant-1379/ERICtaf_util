package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * TAF UI service providing gateway.
 */
@API(Stable)
public class UI extends UiToolkit {

    private UI() {
    }

    @SuppressWarnings("unchecked")
    public static DesktopNavigator newSwtNavigator(Host host) {
        return UiToolkit.newSwtNavigator(host.getHostname(), host.getHttpPort());
    }
}
