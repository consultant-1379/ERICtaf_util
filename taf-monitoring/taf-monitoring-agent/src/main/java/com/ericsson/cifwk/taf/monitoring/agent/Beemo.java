package com.ericsson.cifwk.taf.monitoring.agent;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.data.DataHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Beemo {

    private static TafConfiguration configuration = DataHandler.getConfiguration();
    public static final String TAF_SHUTDOWN = "taf.shutdown.agent";
    public static final String TAF_RELOAD_CONFIG = "taf.config.reload";
    public static final String TAF_MONITORING_INTERVAL = "taf.monitoring.interval";
    private static final long DEFAULT_RELOAD_CONFIG = 10L;
    private static final long DEFAULT_MONITORING_CONFIG = 1000L;

    private Beemo () {
    }
    /**
     * @return true if agent should stop
     */
    public static synchronized boolean shouldShutdown(){
        return getConfiguration().getProperty(TAF_SHUTDOWN, false, Boolean.class);
    }

    /**
     * Seconds
     * @return interval for reloading configuration
     */
    public static synchronized long getReloadInterval(){
        return getConfiguration().getProperty(TAF_RELOAD_CONFIG, DEFAULT_RELOAD_CONFIG, Long.class);
    }

    /**
     * Seconds
     * @return interval for monitoring
     */
    public static synchronized long getMonitoringInterval(){
        return getConfiguration().getProperty(TAF_MONITORING_INTERVAL, DEFAULT_MONITORING_CONFIG, Long.class);
    }

    /**
     * @return monitoring configuration
     */
    public static synchronized TafConfiguration getConfiguration(){
        return configuration;
    }

    /**
     * Close drone
     */
    public static void close(){
        executorService.shutdown();
    }

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static void reload(){
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getConfiguration().reload();
            }
        },0,getReloadInterval(), TimeUnit.SECONDS);
    }


    static {reload();}


}
