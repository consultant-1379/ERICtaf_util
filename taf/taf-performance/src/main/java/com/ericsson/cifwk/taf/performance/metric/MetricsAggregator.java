package com.ericsson.cifwk.taf.performance.metric;

import com.google.common.base.Supplier;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;

public final class MetricsAggregator {

    private final Supplier<Long> timeSupplier;
    private final DescriptiveStatistics statistics;
    private final Map<String, Long> results;
    private long startTime;

    public MetricsAggregator(int windowSize, Supplier<Long> timeSupplier) {
        this.timeSupplier = timeSupplier;
        statistics = new DescriptiveStatistics(windowSize);
        results = new HashMap<>();
        reset();
    }

    public synchronized void update(long time, String result) {
        results.put(result, results.get(result) + 1);
        statistics.addValue(time);
    }

    public synchronized void reset() {
        startTime = timeSupplier.get();
        statistics.clear();
        for (OperationResult resultType : OperationResult.values()) {
            results.put(resultType.toString(), 0L);
        }
    }

    public synchronized long getTimeMin() {
        return (long) statistics.getMin();
    }

    public synchronized long getTimeMax() {
        return (long) statistics.getMax();
    }

    public synchronized double getTimeMean() {
        return (long) statistics.getMean();
    }

    public synchronized double getThroughput() {
        return statistics.getN() * 1000d / (timeSupplier.get() - startTime);
    }

    public synchronized long getSuccessCount() {
        return results.get(OperationResult.SUCCESS.toString());
    }

    public synchronized long getFailureCount() {
        return results.get(OperationResult.FAILURE.toString());
    }

    public synchronized long getUnknownCount() {
        return results.get(OperationResult.UNKNOWN.toString());
    }

    public synchronized double getSuccessPercent() {
        return ratio(OperationResult.SUCCESS);
    }

    public synchronized double getFailurePercent() {
        return ratio(OperationResult.FAILURE);
    }

    public synchronized double getUnknownPercent() {
        return ratio(OperationResult.UNKNOWN);
    }

    private double ratio(OperationResult type) {
        return results.get(type.toString()) * 100d / statistics.getN();
    }

    public synchronized long getTotal() {
        return statistics.getN();
    }

}
