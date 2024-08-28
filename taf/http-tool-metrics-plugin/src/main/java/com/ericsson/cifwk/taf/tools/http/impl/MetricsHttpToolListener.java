package com.ericsson.cifwk.taf.tools.http.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.performance.metric.TestContext;
import com.ericsson.cifwk.taf.performance.sample.Sample;
import com.ericsson.cifwk.taf.performance.sample.SampleWriter;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpClient;
import com.ericsson.cifwk.taf.performance.sample.impl.AmqpSampleWriter;
import com.ericsson.cifwk.taf.tools.http.HttpToolListener;
import com.ericsson.cifwk.taf.tools.http.RequestEvent;
import com.google.common.base.Preconditions;

public class MetricsHttpToolListener implements HttpToolListener {

    private static final Logger log = LoggerFactory.getLogger(MetricsHttpToolListener.class);

    private static final String UNKNOWN = "UNKNOWN";

    private final String executionId;
    private SampleWriter writer;

    public MetricsHttpToolListener(String executionId) {
        this.executionId = executionId;
    }

    public void initialize(AmqpClient amqp) throws IOException {
        SampleWriter writer = AmqpSampleWriter.create(amqp);
        writer.initialize();
        this.writer = writer;
    }

    @Override
    public void onRequest(RequestEvent event) {
        try {
            String suiteName = TestContext.getCurrentSuiteName();
            String testName = TestContext.getCurrentTestName();
            Preconditions.checkNotNull(suiteName, "HTTP request outside of suite run");
            Preconditions.checkNotNull(testName, "HTTP request outside of test run");

            URI target = URI.create(event.getRequestTarget());

            Sample sample = Sample.builder().vuserId(UNKNOWN).executionId(executionId).testSuite(suiteName).testCase(testName).eventTime(new Date())
                    .protocol(target.getScheme()).target(target).requestType(event.getRequestType()).requestSize(event.getRequestSize())
                    .responseCode(event.getResponseCode()).responseTime((int) event.getResponseTimeToEntityMillis())
                    .latency((int) event.getResponseTimeMillis()).responseSize(event.getResponseSize()).build();

            writer.write(sample);
        } catch (NullPointerException | IOException exception) {
            log.info("Reporting of HTTP request failed due to exception: {}", exception);
        }
    }

    public void shutdown() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

}
