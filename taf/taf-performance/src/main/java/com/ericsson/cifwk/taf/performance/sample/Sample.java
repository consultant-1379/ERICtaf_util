package com.ericsson.cifwk.taf.performance.sample;

import com.esotericsoftware.kryo.NotNull;

import java.net.URI;
import java.util.Date;

/**
 * Performance sample of SUT invocation from test. Represents a generic request/response pair using any protocol.
 */
public final class Sample {

    /**
     * Thread id, which initiated the request
     */    
    private long threadId;
    /**
     * VUser id, which was used to initiate the request
     */
    @NotNull
    private String vuserId;
    /**
     * Eiffel execution id. Used for traceability purposes
     */
    @NotNull
    private String executionId;
    /**
     * Name of the test suite
     */
    @NotNull
    private String testSuite;
    /**
     * Name if the test case
     */
    @NotNull
    private String testCase;
    /**
     * Timestamp of the request
     */
    @NotNull
    private Date eventTime;
    /**
     * Protocol, which was used to send the request (e.g. http)
     */
    @NotNull
    private String protocol;
    /**
     * Logical URI of request endpoint
     */
    @NotNull
    private URI target;
    /**
     * Type of the request
     */
    @NotNull
    private String requestType;
    /**
     * Size of the request content in bytes
     */    
    private long requestSize;
    /**
     * Response code or status
     */    
    private int responseCode;
    /**
     * Tells whether response is successful
     */    
    private boolean success;
    /**
     * Roundtrip time of the request
     */    
    private int responseTime;
    /**
     * Time from request invocation to get the first byte of the response
     */    
    private int latency;
    /**
     * Response content size in bytes
     */
    private long responseSize;

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getVuserId() {
        return vuserId;
    }

    public void setVuserId(String vuserId) {
        this.vuserId = vuserId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public URI getTarget() {
        return target;
    }

    public void setTarget(URI target) {
        this.target = target;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public long getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(long requestSize) {
        this.requestSize = requestSize;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(long responseSize) {
        this.responseSize = responseSize;
    }

    public static SampleBuilder builder() {
        return new SampleBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sample sample = (Sample) o;

        if (latency != sample.latency) return false;
        if (requestSize != sample.requestSize) return false;
        if (responseCode != sample.responseCode) return false;
        if (responseSize != sample.responseSize) return false;
        if (responseTime != sample.responseTime) return false;
        if (success != sample.success) return false;
        if (threadId != sample.threadId) return false;
        if (eventTime != null ? !eventTime.equals(sample.eventTime) : sample.eventTime != null) return false;
        if (executionId != null ? !executionId.equals(sample.executionId) : sample.executionId != null) return false;
        if (protocol != null ? !protocol.equals(sample.protocol) : sample.protocol != null) return false;
        if (requestType != null ? !requestType.equals(sample.requestType) : sample.requestType != null) return false;
        if (target != null ? !target.toString().equals(sample.target.toString()) : sample.target != null) return false;
        if (testCase != null ? !testCase.equals(sample.testCase) : sample.testCase != null) return false;
        if (testSuite != null ? !testSuite.equals(sample.testSuite) : sample.testSuite != null) return false;
        if (vuserId != null ? !vuserId.equals(sample.vuserId) : sample.vuserId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (threadId ^ (threadId >>> 32));
        result = 31 * result + (vuserId != null ? vuserId.hashCode() : 0);
        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
        result = 31 * result + (testSuite != null ? testSuite.hashCode() : 0);
        result = 31 * result + (testCase != null ? testCase.hashCode() : 0);
        result = 31 * result + (eventTime != null ? eventTime.hashCode() : 0);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (requestType != null ? requestType.hashCode() : 0);
        result = 31 * result + (int) (requestSize ^ (requestSize >>> 32));
        result = 31 * result + responseCode;
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + responseTime;
        result = 31 * result + latency;
        result = 31 * result + (int) (responseSize ^ (responseSize >>> 32));
        return result;
    }

}
