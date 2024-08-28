package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.performance.PerformancePluginServices;
import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public class MetricsAggregatorTest {

    private MetricsAggregator metrics;
    private long currentTime;

    @Before
    public void setUp() throws Exception {
        currentTime = 0;
        Supplier<Long> timeSupplier = new Supplier<Long>() {
            @Override
            public Long get() {
                return currentTime;
            }
        };
        metrics = new MetricsAggregator(PerformancePluginServices.INFINITE_METRICS_WINDOW, timeSupplier);
    }

    @Test
    public void shouldCountTests() throws Exception {
        metrics.update(1, OperationResult.SUCCESS.toString());
        metrics.update(1, OperationResult.FAILURE.toString());
        metrics.update(1, OperationResult.SUCCESS.toString());
        metrics.update(1, OperationResult.UNKNOWN.toString());

        assertEquals(4, metrics.getTotal());

        assertEquals(2, metrics.getSuccessCount());
        assertEquals(1, metrics.getFailureCount());
        assertEquals(1, metrics.getUnknownCount());
    }

    @Test
    public void shouldCalculateTestPercent() throws Exception {
        metrics.update(1, OperationResult.SUCCESS.toString());
        metrics.update(1, OperationResult.FAILURE.toString());
        metrics.update(1, OperationResult.SUCCESS.toString());
        metrics.update(1, OperationResult.UNKNOWN.toString());

        assertEquals(4, metrics.getTotal());

        assertEquals(50, metrics.getSuccessPercent(), 0);
        assertEquals(25, metrics.getFailurePercent(), 0);
        assertEquals(25, metrics.getUnknownPercent(), 0);
    }

    @Test
    public void shouldRecordTime() throws Exception {
        metrics.update(100, OperationResult.SUCCESS.toString());
        metrics.update(50, OperationResult.SUCCESS.toString());
        metrics.update(30, OperationResult.FAILURE.toString());

        assertEquals(30, metrics.getTimeMin());
        assertEquals(100, metrics.getTimeMax());
        assertEquals(60, metrics.getTimeMean(), 0);
    }

    @Test
    public void shouldCalculateThroughput() throws Exception {
        for (int i = 0; i < 20; i++) {
            metrics.update(1, OperationResult.SUCCESS.toString());
        }
        currentTime = 5000;

        assertEquals(4, metrics.getThroughput(), 0);
    }

    @Test
    public void shouldHandleConcurrentWrites() throws Exception {
        concurrently(100, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                metrics.update(10, OperationResult.SUCCESS.toString());
                return null;
            }
        });

        assertEquals(100, metrics.getTotal());
        assertEquals(10, metrics.getTimeMean(), 0);
    }

    @Test(timeout = 100)
    public void shouldHandleLargeAmounts() throws Exception {
        for (int i = 0; i < 5000; i++) {
            metrics.update(1000, OperationResult.SUCCESS.toString());
        }
    }

    private static <T> void concurrently(int poolSize, Callable<T> callable)
            throws InterruptedException {
        List<Callable<T>> tasks = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            tasks.add(callable);
        }
        ExecutorService es = Executors.newFixedThreadPool(poolSize);
        es.invokeAll(tasks);
    }

}
