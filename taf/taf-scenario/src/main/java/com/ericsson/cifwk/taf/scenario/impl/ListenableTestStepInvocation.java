package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.google.common.base.Optional;

import static com.ericsson.cifwk.taf.scenario.api.TestStepResult.failure;
import static com.ericsson.cifwk.taf.scenario.api.TestStepResult.success;

abstract class ListenableTestStepInvocation implements TestStepInvocation {

    Optional<Object> runWithListener(ScenarioListener listener, Object... args) throws Exception {
        Optional<Object> result;
        long startTime = 0L;
        try {
            listener.onTestStepStarted(this, args);
            startTime = System.currentTimeMillis();
            result = execute(args);
            final long endTime = System.currentTimeMillis();
            onTestStepFinished(listener, success(getName(), result, startTime, endTime));
            return result;
        } catch (Exception e) {
            final long endTime = System.currentTimeMillis();
            onTestStepFinished(listener, failure(getName(), e, startTime, endTime));
            throw e;
        }
    }

    protected abstract Optional<Object> execute(Object... args) throws Exception;

    public void onTestStepFinished(ScenarioListener listener, TestStepResult result) {
        if (listener instanceof ExtendedScenarioListener) {
            ExtendedScenarioListener.class.cast(listener).onTestStepFinished(this, result);
        } else {
            listener.onTestStepFinished(this);
        }
    }
}
