/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;


import static com.ericsson.cifwk.taf.scenario.CustomMatchers.allElementsAreSame;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.differenceIsGreaterThanOrEqualTo;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.differenceIsLessThanOrEqualTo;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.pause;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.api.RampUp.during;
import static com.ericsson.cifwk.taf.scenario.api.RampUp.vUsers;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assume.assumeFalse;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.base.Predicate;
import org.junit.Test;

public class PerformanceTest extends ScenarioTest {
    final Stack<Long> timeStackSec = new Stack<>();

    @Override
    public void setUp() {
        String skip = System.getProperty("taf.scenario.test.performance.skip");
        assumeFalse(Boolean.valueOf(skip) && skip != null);
        super.setUp();
        timeStackSec.clear();
    }

    @Test
    public void testIterations() throws Exception {
        int vUsers = 3;
        int iterationsPerVuser = 10;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(new Counter()))
                                .withIterationsPerVuser(iterationsPerVuser)
                                .withVusers(vUsers)

                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(vUsers * iterationsPerVuser));
    }

    @Test
    public void testDuration() throws Exception {

        long duration = 10;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(sleepForVUser(1))
                                .withDuration(duration, SECONDS)
                                .withVusers(3)
                )
                .build();

        runner.start(scenario);

        long starts = System.currentTimeMillis();
        runner.start(scenario);

        long runningTime = MILLISECONDS.toSeconds(System.currentTimeMillis() - starts);
        assertThat(runningTime, greaterThanOrEqualTo(duration));
    }

    @Test
    public void testDurationWithIterations_durationFinishesFirst() throws Exception {

        long duration = 10;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(sleepForVUser(1))
                                .withIterationsPerVuser(100)
                                .withDuration(duration, SECONDS)
                                .withVusers(3)
                )
                .build();

        long starts = System.currentTimeMillis();
        runner.start(scenario);

        long runningTime = MILLISECONDS.toSeconds(System.currentTimeMillis() - starts);
        assertThat(runningTime, lessThanOrEqualTo(duration + 1));
    }

    @Test
    public void testDurationWithIterations_iterationsFinishesFirst() throws Exception {
        long duration = 10;
        int iterationsPerVuser = 2;
        int vUsers = 3;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(counter))
                                .withIterationsPerVuser(iterationsPerVuser)
                                .withDuration(duration, SECONDS)
                                .withVusers(vUsers)
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(vUsers * iterationsPerVuser));
    }


    @Test
    public void testIterationsWithDataSource() throws Exception {

        int vUsers = 3;
        int iterationsPerVuser = 10;
        int expectedRecordCount = 2;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(new Counter()))
                                .withIterationsPerVuser(iterationsPerVuser)
                                .withVusers(vUsers)
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(vUsers * iterationsPerVuser * expectedRecordCount));
    }

    @Test
    public void testPredicate() throws Exception {
        final int totalIterations = 10;
        final AtomicInteger iterationsLeft = new AtomicInteger(totalIterations);

        Predicate<TestStepFlow.State> condition = new Predicate<TestStepFlow.State>() {
            @Override
            public boolean apply(TestStepFlow.State input) {
                return iterationsLeft.decrementAndGet() >= 0;
            }
        };

        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(new Counter()))
                                .repeatWhile(condition)
                                .withVusers(30)

                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(totalIterations));
    }

    @Test
    public void testThreadUnsafePredicate() throws Exception {
        final int totalIterations = 10;

        Predicate<TestStepFlow.State> condition = new Predicate<TestStepFlow.State>() {
            int threadUnsafeState;

            @Override
            public boolean apply(TestStepFlow.State input) {
                unsafeIncrement();

                return threadUnsafeState <= totalIterations;
            }

            private void unsafeIncrement() {
                int threadUnsafe = threadUnsafeState;
                sleep(100);
                threadUnsafeState = threadUnsafe + 1;
            }
        };

        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(new Counter()))
                                .repeatWhile(condition)
                                .withVusers(30)

                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(totalIterations));
    }

    @Test(timeout = 10000)
    public void interFlowCommunication() throws Exception {
        final AtomicBoolean shouldRun = new AtomicBoolean(true);

        TestScenario scenario = scenario("performance")
                .split(
                        flow("controlledFlow")
                                .addTestStep(runnable(new Counter()))
                                .repeatWhile(new Predicate<TestStepFlow.State>() {
                                    @Override
                                    public boolean apply(TestStepFlow.State input) {
                                        return shouldRun.get();
                                    }
                                }),
                        flow("controlFlow")
                                .pause(1, TimeUnit.SECONDS)
                                .addTestStep(runnable(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                shouldRun.set(false);
                                            }
                                        }
                                ))
                )
                .build();

        long starts = System.currentTimeMillis();
        runner.start(scenario);
        long runningTime = MILLISECONDS.toSeconds(System.currentTimeMillis() - starts);

        assertThat(count.get(), greaterThan(0));
        assertThat(runningTime, greaterThanOrEqualTo(1L));
    }

    @Test
    public void testPause() throws Exception {
        int pauseBetweenIterationsSec = 3;
        int durationSec = 10;
        long pauseBeforeFlow = 7;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .beforeFlow(pause(pauseBeforeFlow, SECONDS))
                                .addTestStep(runnable(pushTimeSec()))
                                .addTestStep(sleepForVUser(1))
                                .pause(pauseBetweenIterationsSec, SECONDS)
                                .withDuration(durationSec, SECONDS)
                )
                .build();

        long startTime = MILLISECONDS.toSeconds(System.currentTimeMillis());
        runner.start(scenario);
        assertThat(timeStackSec.get(0) - startTime, greaterThanOrEqualTo(pauseBeforeFlow));
        assertThat(timeStackSec, differenceIsGreaterThanOrEqualTo(pauseBetweenIterationsSec));
        assertThat(timeStackSec, differenceIsLessThanOrEqualTo(pauseBeforeFlow - 1));
    }

    @Test
    public void testRampUp() throws Exception {
        int vUsers = 3;
        int rampUpTime = 9;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushTimeSec()))
                                .addTestStep(sleepForVUser(1, 2, 3))
                                .withRampUp(during(rampUpTime, SECONDS))
                                .withVusers(vUsers)
                )
                .build();

        runner.start(scenario);

        assertThat(timeStackSec, differenceIsGreaterThanOrEqualTo(rampUpTime / vUsers - 1));
    }

    @Test
    public void testRampUpStep() throws Exception {
        long vUsers = 9;
        long rampUpTime = 18;
        int rampUpUsers = 3;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushTimeSec()))
                                .addTestStep(sleepForVUser())
                                .withRampUp(vUsers(rampUpUsers).every(10, SECONDS))
                                .withVusers((int) vUsers)
                )
                .build();

        runner.start(scenario);

        long prev = Long.MIN_VALUE;
        for (int i = 0; i <= vUsers; i += rampUpUsers) {
            List<Long> rampUpStep = timeStackSec.subList(i, i += rampUpUsers);
            assertThat(prev - rampUpStep.get(0), greaterThanOrEqualTo(rampUpTime / (vUsers / rampUpUsers)));
            assertThat(rampUpStep, allElementsAreSame());
        }
    }

    @Test
    public void testRampUpWithDuration() throws Exception {
        int vUsers = 3;
        long rampUpTime = 6;
        long duration = 10;

        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushTimeSec()))
                                .addTestStep(sleepForVUser())
                                .withRampUp(during(rampUpTime, SECONDS))
                                .withDuration(duration, SECONDS)
                                .withVusers(vUsers)
                )
                .build();

        long starts = System.currentTimeMillis();
        runner.start(scenario);
        long runningTime = MILLISECONDS.toSeconds(System.currentTimeMillis() - starts);
        assertThat(runningTime, greaterThanOrEqualTo(duration));
        assertThat(timeStackSec.subList(0, vUsers), differenceIsGreaterThanOrEqualTo(rampUpTime / vUsers - 1));
        assertThat(timeStackSec, differenceIsLessThanOrEqualTo(rampUpTime / vUsers + 1));
    }

    @Test
    public void testStartDelay() throws Exception {

        int delay = 10;
        TestScenario scenario = scenario("performance")
                .addFlow(
                        flow("flow")
                                .beforeFlow(pause(delay, SECONDS))
                                .addTestStep(runnable(pushTimeSec()))
                )
                .build();

        pushTimeSec().run(); //NOSONAR
        runner.start(scenario);

        assertThat(timeStackSec, differenceIsGreaterThanOrEqualTo(delay));
    }

    @Test
    public void testSplit() throws Exception {
        int firstTestVUsers = 10;
        int duration = 10;
        int delay = (duration - 5);
        int secondTestVUsers = 30;
        TestScenario scenario = scenario("performance")
                .split(
                        flow("background load")
                                .addTestStep(runnable(counter))
                                .addTestStep(sleepForVUser())
                                .withDuration(duration, SECONDS)
                                .withVusers(firstTestVUsers),
                        flow("event")
                                .beforeFlow(pause(delay, SECONDS))
                                .addTestStep(runnable(pushTimeSec()))
                                .withVusers(secondTestVUsers)
                )
                .build();

        long startTime = MILLISECONDS.toSeconds(System.currentTimeMillis());
        runner.start(scenario);

        assertThat(count.get(), greaterThanOrEqualTo((duration - 1) * firstTestVUsers));
        assertThat(timeStackSec, hasSize(secondTestVUsers));
        assertThat(timeStackSec, differenceIsLessThanOrEqualTo(1)); //started +- at same time
        assertThat(timeStackSec.get(0) - startTime, greaterThanOrEqualTo((long) delay)); //had delay
    }


    private Runnable pushTimeSec() {
        return new Runnable() {
            @Override
            public void run() {
                long item = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                timeStackSec.push(item);
            }
        };
    }
}
