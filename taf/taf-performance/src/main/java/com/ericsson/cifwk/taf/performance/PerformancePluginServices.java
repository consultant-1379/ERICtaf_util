package com.ericsson.cifwk.taf.performance;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.performance.metric.CompositeMetricsWriter;
import com.ericsson.cifwk.taf.performance.metric.MetricsAggregatorFactory;
import com.ericsson.cifwk.taf.performance.metric.MetricsWriter;
import com.ericsson.cifwk.taf.performance.metric.graphite.AmqpPublisher;
import com.ericsson.cifwk.taf.performance.metric.graphite.AmqpWriter;
import com.ericsson.cifwk.taf.performance.metric.jmx.JmxMetrics;
import com.ericsson.cifwk.taf.performance.metric.jmx.JmxRegistrationService;
import com.ericsson.cifwk.taf.performance.metric.jmx.JmxWriter;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PerformancePluginServices {

    public static final int DEFAULT_METRICS_WINDOW = 691_200;
    public static final String DEFAULT_AMQP_EXCHANGE = "eiffel.poc.graphite";
    public static final String DEFAULT_JMX_KEY = "com.ericsson.cifwk.taf.performance:type=Metrics";
    public static final String DEFAULT_GRAPHITE_KEY = "com.ericsson.cifwk.taf.performance";
    public static final int INFINITE_METRICS_WINDOW = -1;

    private static final DataHandlerWrapper DH = new DataHandlerWrapper();
    private static final Logger logger = LoggerFactory.getLogger(PerformancePluginServices.class);
    private static MetricsAggregatorFactory metricsAggregatorFactory;

    private static class DataHandlerWrapper {

        public Boolean getBoolean(String key, boolean defaultValue) {
            String value = getString(key, null);
            if (value == null) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        }

        public Integer getInteger(String key, int defaultValue) {
            String value = getString(key, null);
            if (value == null) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        }

        public String getString(String key, String defaultValue) {
            Object value = DataHandler.getAttribute(key);
            if (value == null) {
                return defaultValue;
            }
            return value.toString();
        }
    }

    private static JmxRegistrationService<JmxMetrics> jmxService;
    private static AmqpPublisher amqpPublisher;
    private static ScheduledExecutorService scheduler;
    private static MetricsWriter defaultMetricsWriter;

    public static MetricsAggregatorFactory getMetricsAggregatorFactory() {
        if (metricsAggregatorFactory == null) {
            int window = DH.getInteger("taf.performance.metrics.window", DEFAULT_METRICS_WINDOW);
            metricsAggregatorFactory = new MetricsAggregatorFactory(window);
        }
        return metricsAggregatorFactory;
    }

    public static JmxRegistrationService<JmxMetrics> getJmxService() {
        if (jmxService == null) {
            jmxService = new JmxRegistrationService<>();
        }
        return jmxService;
    }

    public static AmqpPublisher getAmqpPublisher() {
        if (amqpPublisher == null) {
            String hostName = DH.getString("taf.performance.writer.graphite.amqp.host", null);
            String exchange = DH.getString("taf.performance.writer.graphite.amqp.exchange", DEFAULT_AMQP_EXCHANGE);
            if (hostName == null) {
                throw new RuntimeException("No AMQP host specified");
            }
            Host host = DataHandler.getHostByName(hostName);
            if (host == null) {
                throw new RuntimeException(String.format("No AMQP host specified for host name '%s'", hostName));
            }
            amqpPublisher = new AmqpPublisher(host, exchange);
            try {
                amqpPublisher.connect();
            } catch (IOException e) {
                logger.error(String.format("Failed to connect to AMQP host '%s' exchange '%s'", host.toString(), exchange));
                throw Throwables.propagate(e);
            }
        }
        return amqpPublisher;
    }

    public static ScheduledExecutorService getDefaultScheduler() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }
        return scheduler;
    }

    public static MetricsWriter getDefaultMetricsWriter() {
        if (defaultMetricsWriter == null) {
            HashSet<MetricsWriter> writers = new HashSet<>();
            if (DH.getBoolean("taf.performance.writer.jmx.enabled", false)) {
                logger.info("Enabling JMX writer for metrics");
                writers.add(getJmxWriter());
            }
            if (DH.getBoolean("taf.performance.writer.graphite.enabled", false)) {
                logger.info("Enabling AMQP writer for metrics");
                writers.add(getAmqpWriter());
            }
            if (writers.isEmpty()) {
                logger.warn("No writers are enabled for metrics");
            }
            defaultMetricsWriter = new CompositeMetricsWriter(writers);
        }
        return defaultMetricsWriter;
    }

    private static JmxWriter getJmxWriter() {
        String keyPrefix = DH.getString("taf.performance.writer.jmx.key", DEFAULT_JMX_KEY);
        return new JmxWriter(
                getMetricsAggregatorFactory(),
                getJmxService(),
                keyPrefix
        );
    }

    private static AmqpWriter getAmqpWriter() {
        String keyPrefix = DH.getString("taf.performance.writer.graphite.key", DEFAULT_GRAPHITE_KEY);
        return new AmqpWriter(
                getMetricsAggregatorFactory(),
                getAmqpPublisher(),
                getDefaultScheduler(),
                keyPrefix
        );
    }

    public static void shutdown() {
        if (jmxService != null) {
            jmxService.shutdown();
        }
        if (amqpPublisher != null) {
            try {
                amqpPublisher.shutdown();
            } catch (IOException e) {
                logger.error("AMQP publisher failed to shutdown properly", e);
            }
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

}
