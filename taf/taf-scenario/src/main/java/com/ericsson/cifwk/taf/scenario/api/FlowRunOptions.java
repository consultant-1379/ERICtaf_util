/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;


@API(Internal)
public final class FlowRunOptions {
    private Predicate<TestStepFlow.State> repeatWhile;
    private final int vUsers;
    private RampUp.Strategy rampUpStrategy;
    private final long startDelay;
    private ScenarioExceptionHandler exceptionHandler;

    private FlowRunOptions(Integer vUsers,
                           Long startDelay,
                           Predicate<TestStepFlow.State> repeatWhile,
                           @Nullable ScenarioExceptionHandler exceptionHandler) {
        this.vUsers = vUsers;
        this.startDelay = startDelay;
        this.repeatWhile = repeatWhile;
        this.exceptionHandler = exceptionHandler;
    }

    protected static FlowRunOptions newFlowRunOptions(Integer vUsers,
                                                      RampUp.StrategyProvider rampUpStrategyProvider,
                                                      Long startDelay,
                                                      Predicate<TestStepFlow.State> repeatWhile,
                                                      @Nullable ScenarioExceptionHandler exceptionHandler) {

        FlowRunOptions flowRunOptions = new FlowRunOptions(vUsers, startDelay, repeatWhile, exceptionHandler);
        flowRunOptions.rampUpStrategy = rampUpStrategyProvider.provideFor(flowRunOptions);

        return flowRunOptions;
    }

    public RampUp.Strategy getRampUpStrategy() {
        return rampUpStrategy;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public int getVUsers() {
        return vUsers;
    }

    public Optional<ScenarioExceptionHandler> getExceptionHandler() {
        return Optional.fromNullable(exceptionHandler);
    }

    public ScenarioExceptionHandler getExceptionHandlerOr(ScenarioExceptionHandler defaultHandler) {
        checkNotNull(defaultHandler);
        return firstNonNull(exceptionHandler, defaultHandler);
    }

    public Predicate<TestStepFlow.State> getRepeatWhile() {
        return repeatWhile;
    }
}
