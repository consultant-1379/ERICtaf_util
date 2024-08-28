package com.ericsson.cifwk.taf.guice;

import java.util.List;

import com.ericsson.cifwk.taf.configuration.TafDataHandler;
import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.data.Host;
import com.google.common.annotations.VisibleForTesting;

class TafHostFilter {

    private static TafHostFilter instance;

    static synchronized TafHostFilter getInstance() {
        if (instance == null) {
            instance = new TafHostFilter();
        }

        return instance;
    }

    List<Host> filter(TafHost annotation) {
        TafDataHandler.HostFinder<Host> hostFinder = TafDataHandler.findHost();

        if (isValueSet(annotation.hostname())) {
            hostFinder.withHostname(annotation.hostname());
        }

        if (isValueSet(annotation.group())) {
            hostFinder.withGroup(annotation.group());
        }

        if (isValueSet(annotation.type())) {
            hostFinder.withType(annotation.type());
        }

        return hostFinder.getAll();
    }

    @VisibleForTesting
    static boolean isValueSet(String tafAnnotationValue) {
        return !tafAnnotationValue.equals(TafHost.NOT_SET);
    }
}
