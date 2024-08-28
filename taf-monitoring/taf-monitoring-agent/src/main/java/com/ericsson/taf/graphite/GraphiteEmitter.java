package com.ericsson.taf.graphite;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.ConsoleReporter;
import com.yammer.metrics.Gauge;
import com.yammer.metrics.MetricFilter;
import com.yammer.metrics.MetricRegistry;

public class GraphiteEmitter implements Closeable {

    private static final Logger log = LoggerFactory
            .getLogger(GraphiteEmitter.class);
    public static final String DEFAULT_PREFIX = "taf-monitoring";
    private String prefix;
    private AmqpGraphiteReporter reporter;
    private MetricRegistry registry;

    public GraphiteEmitter() {
        setPrefix(DEFAULT_PREFIX);
    }

    public final void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    private String hostname = null;

    private String getHostname() {
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.debug("Could not get hostname {}", e);
                hostname = "UNKNOWN";
            }
        }
        return hostname;
    }

    AmqpGraphiteReporter getReporter() {
        if (reporter == null) {
            reporter = AmqpGraphiteReporter.forRegistry(getRegistry())
                    .prefixedWith(prefix + "." + getHostname())
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL).build();
        }
        return reporter;
    }

    private MetricRegistry getRegistry() {
        if (registry == null) {
            registry = new MetricRegistry(DEFAULT_PREFIX);
        }
        return registry;
    }

    public void registerGauges(final String prefix, final Monitor monitor) {
        for (final Object key : monitor.getSample().keySet()) {
            getRegistry().register(prefix + "." + key, new Gauge<Number>() {
                @Override
                public Number getValue() {
                    Number result = null;
                    try {
                        result = monitor.getSample().get(key);
                    } catch (Exception e) {
                        log.warn(
                                "Sample for {} from monitor {} not reported due to {}",
                                prefix, monitor, e);
                    }
                    return result;
                }
            });
        }
    }

    public void start(final long period, final TimeUnit unit) {
        if (log.isTraceEnabled()) {
            ConsoleReporter.forRegistry(registry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build()
                    .start(period, unit);
        }
        getReporter().start(period, unit);
    }

    public void stop() {
        if (reporter != null) {
            reporter.stop();
        }

    }

    @Override
    public void close() throws IOException {
        stop();
    }

}
