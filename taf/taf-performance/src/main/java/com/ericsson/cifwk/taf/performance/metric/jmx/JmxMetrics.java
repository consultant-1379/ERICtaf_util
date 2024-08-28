package com.ericsson.cifwk.taf.performance.metric.jmx;

import com.ericsson.cifwk.taf.performance.metric.MetricsAggregator;
import com.ericsson.cifwk.taf.performance.metric.MetricsAggregatorFactory;
import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import com.ericsson.cifwk.taf.performance.metric.OperationResult;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

public class JmxMetrics extends StandardMBean implements MetricsMBean {

    private final MetricsAggregator metrics;
    private final String jmxName;

    public JmxMetrics(MetricsAggregator metrics, String jmxName) {
        super(MetricsMBean.class, false);
        this.metrics = metrics;
        this.jmxName = jmxName;
    }

    public static Supplier<JmxMetrics> supplier(MetricsAggregatorFactory aggregatorFactory,
                                                final String jmxName) {
        return Suppliers.compose(new Function<MetricsAggregator, JmxMetrics>() {
            @Override
            public JmxMetrics apply(MetricsAggregator aggregator) {
                return new JmxMetrics(aggregator, jmxName);
            }
        }, aggregatorFactory);
    }

    public static String name(String prefix, MetricsName metricsName) {
        final StringBuilder builder = new StringBuilder(prefix);
        metricsName.handleParts(new MetricsName.PartHandler() {
            @Override
            public void handle(String key, String value) {
                builder.append(',')
                        .append(key)
                        .append('=')
                        .append(value);
            }
        });
        return builder.toString();
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return super.preRegister(server, new ObjectName(jmxName));
    }

    public void update(long time, String result) {
        metrics.update(time, result);
    }

    @Override
    public long getTimeMin() {
        return metrics.getTimeMin();
    }

    @Override
    public long getTimeMax() {
        return metrics.getTimeMax();
    }

    @Override
    public double getTimeMean() {
        return metrics.getTimeMean();
    }

    @Override
    public double getThroughput() {
        return metrics.getThroughput();
    }

    @Override
    public long getSuccessCount() {
        return metrics.getSuccessCount();
    }

    @Override
    public long getFailureCount() {
        return metrics.getFailureCount();
    }

    @Override
    public long getUnknownCount() {
        return metrics.getUnknownCount();
    }

    @Override
    public double getSuccessPercent() {
        return metrics.getSuccessPercent();
    }

    @Override
    public double getFailurePercent() {
        return metrics.getFailurePercent();
    }

    @Override
    public double getUnknownPercent() {
        return metrics.getUnknownPercent();
    }

    @Override
    public long getTotal() {
        return metrics.getTotal();
    }

}
