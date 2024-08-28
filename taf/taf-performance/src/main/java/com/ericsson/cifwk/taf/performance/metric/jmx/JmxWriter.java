package com.ericsson.cifwk.taf.performance.metric.jmx;

import com.ericsson.cifwk.taf.performance.metric.MetricsAggregatorFactory;
import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import com.ericsson.cifwk.taf.performance.metric.MetricsWriter;
import com.ericsson.cifwk.taf.performance.metric.OperationResult;

public class JmxWriter implements MetricsWriter {

    private final JmxRegistrationService<JmxMetrics> jmxService;
    private final MetricsAggregatorFactory aggregatorFactory;
    private final String namePrefix;

    public JmxWriter(MetricsAggregatorFactory aggregatorFactory,
                     JmxRegistrationService<JmxMetrics> jmxService,
                     String namePrefix) {
        this.aggregatorFactory = aggregatorFactory;
        this.jmxService = jmxService;
        this.namePrefix = namePrefix;
    }

    @Override
    public void update(MetricsName metricsName, long time, String result) {
        String name = JmxMetrics.name(namePrefix, metricsName);
        JmxMetrics mBean = jmxService.findOrRegister(name, JmxMetrics.supplier(aggregatorFactory, name));
        mBean.update(time, result);
    }

}
