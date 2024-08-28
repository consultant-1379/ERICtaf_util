package com.ericsson.cifwk.taf.performance.metric;

public interface MetricsWriter {
    void update(MetricsName name, long time, String result);
}
