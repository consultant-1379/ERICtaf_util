package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.meta.API.Quality.Experimental;

import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.User;

/**
 * Represents generic Host without any specific implementation
 * Could be used in Predicates
 */
@API(Experimental)
public interface HostFilter {
    Map<String, String> getPorts();

    String getIp();

    HostFilter getIlo();

    String getIpv6();

    String getHostName();

    String getType();

    List<? extends HostFilter> getNodes();

    List<User> getUsers();

    String getGroup();

    String getUnit();

    String getParentName();
}
