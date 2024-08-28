package com.ericsson.cifwk.taf.performance.metric;

import java.util.Set;

public class CompositeMetricsWriter implements MetricsWriter {

    private final Set<MetricsWriter> writers;

    public CompositeMetricsWriter(Set<MetricsWriter> writers) {
        this.writers = writers;
    }

    @Override
    public void update(MetricsName name, long time, String result) {
        for (MetricsWriter writer : writers) {
            writer.update(name, time, result);
        }
    }
}
