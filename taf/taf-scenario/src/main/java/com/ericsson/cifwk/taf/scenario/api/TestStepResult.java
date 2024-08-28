package com.ericsson.cifwk.taf.scenario.api;


import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.api.Nullable;
import com.google.common.base.Optional;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.Optional.absent;

@API(Stable)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TestStepResult {

    public static final String LAST_FAILED_TEST_STEP = "lastFailedTestStep";

    private final String name;
    private Optional<Object> result;
    private final Throwable exception;
    private long testStepStartTime;
    private long testStepEndTime;

    public static TestStepResult success(String name, Optional<Object> result) {
        return new TestStepResult(name, result, null);
    }

    public static TestStepResult success(String name, Optional<Object> result, long testStepExecutionStartTime, long testStepExecutionEndTime) {
        return new TestStepResult(name, result, null, testStepExecutionStartTime, testStepExecutionEndTime);
    }

    public static TestStepResult failure(String name, @Nullable Throwable exception) {
        return new TestStepResult(name, absent(), exception);
    }

    public static TestStepResult failure(String name, @Nullable Throwable exception, long testStepExecutionStartTime, long testStepExecutionEndTime) {
        return new TestStepResult(name, absent(), exception, testStepExecutionStartTime, testStepExecutionEndTime);
    }

    private TestStepResult(String name, Optional<Object> result, @Nullable Throwable exception) {
        this.name = name;
        this.result = result;
        this.exception = exception;
    }

    private TestStepResult(String name, Optional<Object> result, @Nullable Throwable exception, long testStepExecutionStartTime, long testStepExecutionEndTime) {
        this.name = name;
        this.result = result;
        this.exception = exception;
        this.testStepStartTime = testStepExecutionStartTime;
        this.testStepEndTime = testStepExecutionEndTime;
    }

    public String getName() {
        return name;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isSuccessful() {
        return exception == null;
    }

    public Optional<Object> getResult() {
        return result;
    }

    public long getTestStepExecutionTime () {
        return testStepEndTime - testStepStartTime;
    }

    public long getTestStepStartTime() {
        return testStepStartTime;
    }

    public long getTestStepEndTime() {
        return testStepEndTime;
    }
}
