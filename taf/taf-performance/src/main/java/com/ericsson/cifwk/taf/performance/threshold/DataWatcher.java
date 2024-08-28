package com.ericsson.cifwk.taf.performance.threshold;

import com.ericsson.cifwk.taf.performance.threshold.rules.MaxCap;
import com.ericsson.cifwk.taf.performance.threshold.rules.MaxLinearCap;
import com.ericsson.cifwk.taf.performance.threshold.rules.MinCap;
import com.ericsson.cifwk.taf.performance.threshold.rules.MinLinearCap;
import com.ericsson.cifwk.taf.performance.threshold.rules.StandardDeviationCap;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for watching data updates and invoke according listeners if thresholds are reached.
 * Construct new instances using builder() method.
 */
public final class DataWatcher {

    private final Set<ThresholdRule> thresholds;
    private final Set<ViolationListener> listeners;
    private long lastTimestamp;

    private DataWatcher(Builder builder) {
        thresholds = ImmutableSet.copyOf(builder.thresholds);
        listeners = ImmutableSet.copyOf(builder.listeners);
        lastTimestamp = 0;
    }

    /**
     * Constructs a new Builder instance.
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    protected void report(ThresholdRule threshold, long timestamp, double exceedance) {
        for (ViolationListener listener : listeners) {
            listener.onViolate(threshold, timestamp, exceedance);
        }
    }

    public void update(MetricSlice metricSlice) {
        long timestamp = metricSlice.getStartTimestamp();
        Double[] data = metricSlice.getData();
        int seriesStep = metricSlice.getSeriesStep();

        int shift = 0;
        if (timestamp < lastTimestamp) {
            shift = (int) ((lastTimestamp - timestamp) / seriesStep) + 1;
        }

        for (int i = shift; i < data.length; i++) {
            Double value = data[i];
            if (value != null) {
                for (ThresholdRule threshold : thresholds) {
                    if (threshold.check(timestamp, value)) {
                        report(threshold, timestamp, value);
                    }
                }
            }
            timestamp += seriesStep;
        }

        long endTimestamp = metricSlice.getEndTimestamp()-1;
        if (lastTimestamp < endTimestamp) {
            lastTimestamp = endTimestamp;
        }
    }

    public static class Builder {

        private Set<ThresholdRule> thresholds = new HashSet<>();
        private Set<ViolationListener> listeners = new HashSet<>();

        private Builder() {
        }

        public Builder threshold(ThresholdRule threshold) {
            thresholds.add(threshold);
            return this;
        }

        public Builder listener(ViolationListener listener) {
            listeners.add(listener);
            return this;
        }

        public Builder rule(ThresholdRule rule) {
            thresholds.add(rule);
            return this;
        }

        public Builder max(double value) {
            thresholds.add(new MaxCap(value));
            return this;
        }

        public Builder min(double value) {
            thresholds.add(new MinCap(value));
            return this;
        }

        public Builder linearMax(double x, double y, double slope) {
            thresholds.add(new MaxLinearCap(x, y, slope));
            return this;
        }

        public Builder linearMin(double x, double y, double slope) {
            thresholds.add(new MinLinearCap(x, y, slope));
            return this;
        }

        public Builder stdDev(double maxSigma) {
            thresholds.add(new StandardDeviationCap(maxSigma));
            return this;
        }

        public Builder stdDev(double maxSigma, int windowSize) {
            thresholds.add(new StandardDeviationCap(maxSigma, windowSize));
            return this;
        }

        public DataWatcher build() {
            return new DataWatcher(this);
        }

    }

}
