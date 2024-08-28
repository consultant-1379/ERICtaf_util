package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.taf.data.Host;
import com.google.inject.Inject;

import java.util.List;

public class InjectableHostExample {

    @Inject
    @TafHost(hostname = "sc1")
    private Host hostByName;

    @Inject
    @TafHost(type = "selenium_grid")
    private Host hostByType;

    @Inject
    @TafHost(group = "kpiserv")
    private List<Host> hosts;

}
