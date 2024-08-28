package com.ericsson.cifwk.taf.performance.threshold;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DataWatcherTest {

    private ThresholdRule threshold;
    private DataWatcher watcher;
    private ViolationListener listener;

    @Before
    public void setUp() throws Exception {
        threshold = mock(ThresholdRule.class);
        listener = mock(ViolationListener.class);
        watcher = DataWatcher.builder()
                .threshold(threshold)
                .listener(listener)
                .build();
    }

    @Test
    public void shouldUpdate() throws Exception {
        when(threshold.check(anyDouble(), anyDouble())).thenReturn(false);
        when(threshold.check(anyDouble(), eq(101d))).thenReturn(true);
        when(threshold.check(anyDouble(), eq(110d))).thenReturn(true);
        MetricSlice data = graphiteData(90d, 100d, 101d, 110d);
        watcher.update(data);

        verify(listener).onViolate(threshold, 2, 101);
        verify(listener).onViolate(threshold, 3, 110);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldUpdateWithNulls() throws Exception {
        when(threshold.check(anyDouble(), anyDouble())).thenReturn(false);
        when(threshold.check(anyDouble(), eq(10d))).thenReturn(true);

        MetricSlice data = graphiteData(null, null, 10d);
        watcher.update(data);

        verify(listener).onViolate(threshold, 2, 10);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldWorkWithOverlappingPoints() throws Exception {
        when(threshold.check(anyDouble(), anyDouble())).thenReturn(false);
        when(threshold.check(anyDouble(), eq(3d))).thenReturn(true);

        watcher.update(graphiteData(10, null, null, 3d));
        watcher.update(graphiteData(11, null, 3d, 2d));

        verify(listener).onViolate(threshold, 12, 3);
        verifyNoMoreInteractions(listener);
    }

    private MetricSlice graphiteData(Double... data) {
       return graphiteData(0, data);
    }

    private MetricSlice graphiteData(long startTimestamp, Double... data) {
        MetricSlice metricSlice = new MetricSlice();
        metricSlice.startTimestamp = startTimestamp;
        metricSlice.endTimestamp = startTimestamp + data.length;
        metricSlice.seriesStep = 1;
        metricSlice.data = data;
        return metricSlice;
    }

}
