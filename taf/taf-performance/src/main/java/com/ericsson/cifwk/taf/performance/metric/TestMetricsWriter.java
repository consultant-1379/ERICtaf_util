package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.performance.PerformancePluginServices;

public class TestMetricsWriter {

    public static final String TAF = "taf";
    public static final String HTTP = "http";
    public static final String JMS = "jms";
    public static final String EJB = "ejb";
    public static final String RMI = "rmi";
    public static final String CORBA = "corba";

    public static final String ALL_GROUP = "all";
    public static final String SUITES_GROUP = "suites";
    public static final String TESTS_GROUP = "tests";

    private final String protocol;
    private final MetricsWriter writer;

    public TestMetricsWriter(String protocol) {
        this(protocol, PerformancePluginServices.getDefaultMetricsWriter());
    }

    public TestMetricsWriter(String protocol, MetricsWriter writer) {
        this.protocol = protocol;
        this.writer = writer;
    }

    public void update(long time, String result) {
        String suite = TestContext.getCurrentSuiteName();
        String test = TestContext.getCurrentTestName();
        update(suite, test, time, result);
    }

    void update(String suite, String test, long time, String result) {
        writer.update(MetricsName.builder()
                .group(ALL_GROUP)
                .protocol(protocol)
                .build(), time, result);

        writer.update(MetricsName.builder()
                .group(SUITES_GROUP)
                .suite(suite)
                .protocol(protocol)
                .build(), time, result);

        writer.update(MetricsName.builder()
                .group(TESTS_GROUP)
                .suite(suite)
                .test(test)
                .protocol(protocol)
                .build(), time, result);
    }

}
