package com.ericsson.cifwk.taf.performance.sample;

import com.google.common.base.Throwables;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Date;

public final class SampleBuilder {

    private final Sample sample;

    public SampleBuilder() {
        sample = new Sample();
        sample.setThreadId(Thread.currentThread().getId());
    }

    public SampleBuilder threadId(long threadId) {
        sample.setThreadId(threadId);
        return this;
    }

    public SampleBuilder vuserId(String vuserId) {
        sample.setVuserId(vuserId);
        return this;
    }

    public SampleBuilder executionId(String executionId) {
        sample.setExecutionId(executionId);
        return this;
    }

    public SampleBuilder testSuite(String testSuite) {
        sample.setTestSuite(testSuite);
        return this;
    }

    public SampleBuilder testCase(String testCase) {
        sample.setTestCase(testCase);
        return this;
    }

    public SampleBuilder eventTime(Date eventTime) {
        sample.setEventTime(eventTime);
        return this;
    }

    public SampleBuilder protocol(String protocol) {
        sample.setProtocol(protocol);
        return this;
    }

    public SampleBuilder target(URI target) {
        try {
            // Remove query string and fragment
            sample.setTarget(new URI(
                    target.getScheme(),
                    target.getUserInfo(),
                    target.getHost(),
                    target.getPort(),
                    target.getPath(),
                    null, null
            ));
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
        return this;
    }

    public SampleBuilder requestType(String requestType) {
        sample.setRequestType(requestType);
        return this;
    }

    public SampleBuilder requestSize(long requestSize) {
        sample.setRequestSize(requestSize);
        return this;
    }

    public SampleBuilder requestBody(String requestBody, Charset charset) {
        sample.setRequestSize(requestBody.getBytes(charset).length);
        return this;
    }
    
    public SampleBuilder responseCode(int responseCode) {
        sample.setResponseCode(responseCode);
        return this;
    }

    public SampleBuilder success(boolean success) {
        sample.setSuccess(success);
        return this;
    }

    public SampleBuilder responseTime(int responseTime) {
        sample.setResponseTime(responseTime);
        return this;
    }

    public SampleBuilder latency(int latency) {
        sample.setLatency(latency);
        return this;
    }

    public SampleBuilder responseSize(long responseSize) {
        sample.setResponseSize(responseSize);
        return this;
    }

    public SampleBuilder responseBody(String responseBody, Charset charset) {
        sample.setResponseSize(responseBody.getBytes(charset).length);
        return this;
    }

    public Sample build() {
        return sample;
    }

}
