package com.ericsson.cifwk.taf.management.jmx;

import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.management.TafManager;
import com.ericsson.cifwk.taf.management.TafRunnerContext;

public class TafJMXManager extends StandardMBean implements TafManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafManager.class);

    public TafJMXManager() {
        super(TafManager.class, false);
    }

    @Override
    public String getName() {
        return TafRunnerContext.getContext().getName();
    }

    @Override
    public void shutdown() {
        LOGGER.error("[Shutdown] received from TAF JMX Management API");
        TafRunnerContext.getContext().interrupt();
    }

    @Override
    public boolean isTerminated() {
        return TafRunnerContext.getContext().isTerminated();
    }

    @Override
    public void kill() {
        LOGGER.error("[Kill] received from TAF JMX Management API");
        TafRunnerContext.getContext().interrupt();
        System.exit(255);
    }

}
