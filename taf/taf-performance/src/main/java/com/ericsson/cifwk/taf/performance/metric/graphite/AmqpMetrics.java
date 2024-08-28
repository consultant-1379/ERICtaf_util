package com.ericsson.cifwk.taf.performance.metric.graphite;

import com.ericsson.cifwk.taf.performance.metric.MetricsAggregator;
import com.ericsson.cifwk.taf.performance.metric.MetricsAggregatorFactory;
import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import com.ericsson.cifwk.taf.performance.metric.OperationResult;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class AmqpMetrics {

    private final MetricsAggregator metrics;

    public AmqpMetrics(MetricsAggregator metrics) {
        this.metrics = metrics;
    }

    public static Supplier<AmqpMetrics> supplier(MetricsAggregatorFactory aggregatorFactory) {
        return Suppliers.compose(new Function<MetricsAggregator, AmqpMetrics>() {
            @Override
            public AmqpMetrics apply(MetricsAggregator aggregator) {
                return new AmqpMetrics(aggregator);
            }
        }, aggregatorFactory);
    }
    private static String prepare(String name) {
        return name.replaceAll("\\ ", "_");
    }
    public static String name(String prefix, MetricsName metricsName) {
        final StringBuilder builder = new StringBuilder(prefix);
        metricsName.handleParts(new MetricsName.PartHandler() {
            @Override
            public void handle(String key, String value) {
                builder.append('.')
                        .append(value);
            }
        });
        return prepare(builder.toString());
    }

    public void update(long time, String result) {
        metrics.update(time, result);
    }

    public MetricsAggregator getMetrics() {
        return metrics;
    }

}
