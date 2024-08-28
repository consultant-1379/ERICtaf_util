package com.ericsson.cifwk.taf.performance.metric;

import com.google.common.base.Supplier;

public class MetricsAggregatorFactory implements Supplier<MetricsAggregator> {

    private final int windowSize;
    private final Supplier<Long> timeSupplier;

    public MetricsAggregatorFactory(int windowSize) {
        this(windowSize, new Supplier<Long>() {
            @Override
            public Long get() {
                return System.currentTimeMillis();
            }
        });
    }

    public MetricsAggregatorFactory(int windowSize, Supplier<Long> timeSupplier) {
        this.windowSize = windowSize;
        this.timeSupplier = timeSupplier;
    }

    @Override
    public MetricsAggregator get() {
        return new MetricsAggregator(windowSize, timeSupplier);
    }
}
