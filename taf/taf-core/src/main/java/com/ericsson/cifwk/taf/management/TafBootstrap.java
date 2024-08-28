package com.ericsson.cifwk.taf.management;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.management.jmx.TafJMXManagement;
import com.ericsson.cifwk.taf.spi.TafPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Component for starting up and tearing down TAF runtime environment.
 */
@API(Internal)
public class TafBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafBootstrap.class);

    private static final AtomicBoolean started = new AtomicBoolean();

    TafJMXManagement tafManagement;
    ServiceLoader<TafPlugin> plugins;

    public void init() {
        if (started.compareAndSet(false, true)) {
            tafManagement = new TafJMXManagement();
            tafManagement.start();
            initializePlugins();
        }
    }

    private void initializePlugins() {
        LOGGER.info("Initializing TAF plugins");
        plugins = ServiceLoader.load(TafPlugin.class);
        int pluginCount = 0;
        long startTime = System.currentTimeMillis();
        for (TafPlugin plugin : plugins) {
            String pluginName = plugin.getClass().getSimpleName();
            LOGGER.info("Initializing TAF plugin : {}", pluginName);
            plugin.init();
            pluginCount++;
            LOGGER.info("TAF plugin {} initialized", pluginName);
        }
        LOGGER.info("Loaded {} TAF plugins in {} milliseconds", pluginCount, System.currentTimeMillis() - startTime);
    }

    public void shutdown() {
        LOGGER.info("Shutting down TAF plugins");
        for (TafPlugin plugin : plugins) {
            String pluginName = plugin.getClass().getSimpleName();
            LOGGER.info("Shutting down TAF plugin : {}", pluginName);
            plugin.shutdown();
            LOGGER.info("TAF plugin {} shut down", pluginName);
        }
        LOGGER.info("Closing resources");
        closeResources();
        tafManagement.stop();
        started.set(false);
    }

    private void closeResources() {
        TafRunnerContext context = TafRunnerContext.getContext();
        List<Closeable> closeables = context.getCloseables();
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
