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

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.base.Predicate;
import org.junit.Test;

public class RampUpTest {
    RampUp.StrategyProvider mockProvider = mock(RampUp.StrategyProvider.class);
    Predicate<TestStepFlow.State> repeatWhile = mock(Predicate.class);

    @Test
    public void testStepRampUp() throws Exception {
        int totalVUsers = 9;
        int timePeriod = 10;
        int step = 3;

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(totalVUsers, mockProvider, 0L, repeatWhile, null);
        RampUp.Strategy strategy = RampUp.vUsers(step).every(timePeriod, MILLISECONDS).provideFor(flowRunOptions);

        assertThat(collect(strategy, totalVUsers), contains(0, 0, 0, timePeriod, 0, 0, timePeriod, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStepRampUp_TooManyUsers() throws Exception {
        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(5, mockProvider, 0L, repeatWhile, null);
        RampUp.vUsers(10).every(1, MILLISECONDS).provideFor(flowRunOptions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStepRampUp_TooLong() throws Exception {
        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(5, mockProvider, 0L, repeatWhile, null);
        RampUp.vUsers(10).every(1000, MILLISECONDS).provideFor(flowRunOptions);
    }

    @Test
    public void testStepRampUp_maxValues() throws Exception {
        int totalVUsers = 10;
        int timePeriod = 10;
        int step = 10;

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(totalVUsers, mockProvider, 0L, repeatWhile, null);
        RampUp.Strategy strategy = RampUp.vUsers(step).every(timePeriod, MILLISECONDS).provideFor(flowRunOptions);

        assertThat(collect(strategy, totalVUsers), contains(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    @Test
    public void testStepRampUp_unevenStep() throws Exception {
        int totalVUsers = 10;
        int timePeriod = 10;
        int step = 9;

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(totalVUsers, mockProvider, 0L, repeatWhile, null);
        RampUp.Strategy strategy = RampUp.vUsers(step).every(timePeriod, MILLISECONDS).provideFor(flowRunOptions);

        assertThat(collect(strategy, totalVUsers), contains(0, 0, 0, 0, 0, 0, 0, 0, 0, timePeriod));
    }

    @Test
    public void testDurationRampUp() throws Exception {
        int totalVUsers = 10;
        int timePeriod = 20;
        int delta = timePeriod / totalVUsers;

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(totalVUsers, mockProvider, 0L, repeatWhile, null);
        RampUp.Strategy strategy = RampUp.during(timePeriod, MILLISECONDS).provideFor(flowRunOptions);

        assertThat(collect(strategy, totalVUsers), contains(delta, delta, delta, delta, delta, delta, delta, delta, delta, delta));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDurationRampUp_tooManyVusers() throws Exception {
        int totalVUsers = 1000;
        int timePeriod = 10;

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(totalVUsers, mockProvider, 0L, repeatWhile, null);
        RampUp.during(timePeriod, MILLISECONDS).provideFor(flowRunOptions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRampUp_noVUsers() throws Exception {
        flow("with default vUsers").withRampUp(RampUp.during(10, SECONDS)).build();
    }

    private List<Integer> collect(RampUp.Strategy strategy, int totalVUsers) {
        List<Integer> result = new ArrayList<>(totalVUsers);

        for (int vUser = 0; vUser < totalVUsers; vUser++) {
            result.add(new Long(strategy.nextVUserDelayDelta()).intValue());
        }

        return result;
    }
}
