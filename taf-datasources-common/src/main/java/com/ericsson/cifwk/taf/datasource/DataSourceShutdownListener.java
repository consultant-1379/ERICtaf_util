package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.management.TafRunnerContext;
import com.ericsson.cifwk.taf.testapi.events.TestSessionFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Listener that closes data sources on test execution finish.
 */
public class DataSourceShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceShutdownListener.class);

    @Subscribe
    public void onTestSessionFinish(TestSessionFinishedEvent event) {
        closeAllDataSources();
    }

    private void closeAllDataSources() {
        TafRunnerContext context = TafRunnerContext.getContext();
        List<Closeable> closeables = context.getCloseables();
        logger.info("Closing Data Sources ({})", closeables.size());

        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
