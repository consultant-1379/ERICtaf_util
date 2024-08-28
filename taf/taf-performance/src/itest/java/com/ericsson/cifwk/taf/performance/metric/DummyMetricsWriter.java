package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.performance.PerformancePluginServices;
import com.ericsson.cifwk.taf.performance.util.SynchronizedRepository;

public class DummyMetricsWriter implements MetricsWriter {

    private final SynchronizedRepository<String, MetricsAggregator> repository;

    public DummyMetricsWriter() {
        MetricsAggregatorFactory factory =
                new MetricsAggregatorFactory(PerformancePluginServices.DEFAULT_METRICS_WINDOW);
        repository = new SynchronizedRepository<>(factory);
    }

    @Override
    public void update(MetricsName name, long time, String result) {
        MetricsAggregator metrics = repository.getOrCreate(name(name));
        metrics.update(time, result);
    }

    private String name(MetricsName name) {
        final StringBuilder builder = new StringBuilder();
        name.handleParts(new MetricsName.PartHandler() {
            @Override
            public void handle(String key, String value) {
                builder.append(key)
                        .append('=')
                        .append(value)
                        .append(';');
            }
        });
        return builder.toString();
    }
}
