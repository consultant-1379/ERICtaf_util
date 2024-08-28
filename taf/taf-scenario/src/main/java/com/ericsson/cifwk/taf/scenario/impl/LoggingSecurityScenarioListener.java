/*
 * ------------------------------------------------------------------------------
 * ******************************************************************************
 * COPYRIGHT Ericsson 2018
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ******************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.cifwk.taf.scenario.impl;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.google.common.base.Joiner;

/**
 * Extension point to get notified about scenario execution events, hiding password details from logs using regex patterns.
 */
public class LoggingSecurityScenarioListener implements ScenarioListener {

    private LoggingScenarioListener loggingScenarioListener = new LoggingScenarioListener();
    private static final String REGEX_PATTERNS = "(?i:password[^,]*|(;[^)]*))";
    private static final String PASSWORD_REPLACEMENT = " ********";

    @Override
    public void onScenarioStarted(TestScenario scenario) {
        loggingScenarioListener.onScenarioStarted(scenario);
    }

    @Override
    public void onScenarioFinished(TestScenario scenario) {
        loggingScenarioListener.onScenarioFinished(scenario);
    }

    @Override
    public void onFlowStarted(TestStepFlow flow) {
        loggingScenarioListener.onFlowStarted(flow);
    }

    @Override
    public void onFlowFinished(TestStepFlow flow) {
        loggingScenarioListener.onFlowFinished(flow);
    }

    @Override
    public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
        final String argumentString = Joiner.on("; ").useForNull("null").join(args);
        final List<String> arguments = new ArrayList<>();
        arguments.add(argumentString.replaceFirst(REGEX_PATTERNS, PASSWORD_REPLACEMENT));
        loggingScenarioListener.onTestStepStarted(invocation, (Object[]) arguments.toArray());
    }

    @Override
    public void onTestStepFinished(TestStepInvocation invocation) {
        loggingScenarioListener.onTestStepFinished(invocation);
    }

}
