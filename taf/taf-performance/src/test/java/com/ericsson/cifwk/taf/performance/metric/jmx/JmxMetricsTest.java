package com.ericsson.cifwk.taf.performance.metric.jmx;

import com.ericsson.cifwk.taf.performance.PerformancePluginServices;
import com.ericsson.cifwk.taf.performance.metric.MetricsAggregatorFactory;
import com.ericsson.cifwk.taf.performance.metric.MetricsName;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;

public class JmxMetricsTest {

    private MetricsMBean metrics;

    @Before
    public void setUp() {
        MetricsAggregatorFactory aggregatorFactory =
                new MetricsAggregatorFactory(PerformancePluginServices.INFINITE_METRICS_WINDOW);
        metrics = new JmxMetrics(aggregatorFactory.get(), PerformancePluginServices.DEFAULT_JMX_KEY);
    }

    @Test
    public void shouldRegister() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectInstance instance = mBeanServer.registerMBean(metrics, null);
        mBeanServer.unregisterMBean(instance.getObjectName());
    }

    @Test
    public void shouldBuildName() {
        MetricsName metricsName = MetricsName.builder()
                .group("suites")
                .protocol("http")
                .suite("suite1")
                .test("test1")
                .build();

        String actual = JmxMetrics.name(PerformancePluginServices.DEFAULT_JMX_KEY, metricsName);
        String expected = PerformancePluginServices.DEFAULT_JMX_KEY +
                ",group=suites,suite=suite1,test=test1,protocol=http";

        assertEquals(expected, actual);
    }

}
