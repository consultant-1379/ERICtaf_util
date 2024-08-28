package com.ericsson.cifwk.taf.performance.metric.jmx;

public interface MetricsMBean {
    long getTimeMin();

    long getTimeMax();

    double getTimeMean();

    double getThroughput();

    long getSuccessCount();

    long getFailureCount();

    long getUnknownCount();

    double getSuccessPercent();

    double getFailurePercent();

    double getUnknownPercent();

    long getTotal();
}
