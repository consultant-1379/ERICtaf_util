package com.ericsson.cifwk.taf.handlers.netsim.spi;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionFinishedEvent;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * NetSim listener that closes all open NetSim contexts when tests finish
 */
@API(Internal)
public class NetSimTestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetSimTestListener.class);

    @Subscribe
    public void onTestCaseEvent(TestCaseEvent event) {
        boolean closeOnTest = NetSimCommandHandler.isCloseOnTest();
        switch (event.getExecutionState()) {
            case STARTED:
                // Do nothing
                break;
            case SUCCEEDED:
            case FAILED:
            case SKIPPED:
                if (closeOnTest) {
                    closeAllContexts();
                }
                break;
        }
    }

    @Subscribe
    public void onTestSessionFinishEvent(TestSessionFinishedEvent event) {
        if (NetSimCommandHandler.isCloseOnExecution()) {
            closeAllContexts();
        }
    }

    @Subscribe //after suite
    public void onTestSuiteFinishEvent(TestGroupFinishedEvent event) {
        if (NetSimCommandHandler.isCloseOnSuite()) {
            closeAllContexts();
        }
    }

    @VisibleForTesting
    protected void closeAllContexts() {
        LOGGER.debug("NetSim test listener: closing all open NetSim sessions");
        try {
            NetSimCommandHandler.closeAllContexts();
            LOGGER.debug("NetSim test listener: all open NetSim sessions closed");
        } catch (Exception e) {
            LOGGER.error("Failed to close NetSim sessions successfully", e);
        }
    }
}
