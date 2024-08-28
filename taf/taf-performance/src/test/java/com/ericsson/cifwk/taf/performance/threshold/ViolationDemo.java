package com.ericsson.cifwk.taf.performance.threshold;

import com.ericsson.cifwk.taf.performance.threshold.listener.LoggingViolationListener;
import com.ericsson.cifwk.taf.performance.threshold.rules.MaxCap;
import com.ericsson.cifwk.taf.performance.threshold.rules.MaxLinearCap;

import java.util.Date;

public final class ViolationDemo {

    private long timestamp = new Date().getTime();

    public static void main(String[] args) {
        ViolationDemo demo = new ViolationDemo();
        demo.run();
    }

    private void run() {
        DataWatcher watcher = DataWatcher.builder()
                .threshold(new MaxCap(100))
                .threshold(new MaxLinearCap(timestamp, 100, 2))
                .listener(new LoggingViolationListener())
                .build();

        watcher.update(graphiteData(80d, 90d, 100d, 101d, 110d));
    }

    private MetricSlice graphiteData(Double... data) {
        MetricSlice metricSlice = new MetricSlice();
        metricSlice.startTimestamp = timestamp;
        metricSlice.seriesStep = 1;
        metricSlice.data = data;
        return metricSlice;
    }

}
