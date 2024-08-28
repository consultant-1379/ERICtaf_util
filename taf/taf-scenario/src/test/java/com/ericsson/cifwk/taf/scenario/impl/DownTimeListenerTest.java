/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static java.lang.String.format;

public class DownTimeListenerTest {

    private SharedFlows sharedFlows = new SharedFlows();

    @Test
    public void downTimeListener() {
        DownTimeListener listener = new DownTimeListener();

        TestScenario scenario = scenario()
                .split(
                        sharedFlows.sharedFlow1(),
                        sharedFlows.sharedFlow2()
                )
                .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                .build();

        runner()
                .withListener(listener)
                .build()
                .start(scenario);

        listener.printResults();
    }

    public static class DownTimeListener extends ExtendedScenarioListener {

        final ConcurrentHashMap<String, Stopwatch> downtimes = new ConcurrentHashMap<>();

        @Override
        public void onTestStepFinished(TestStepInvocation invocation, TestStepResult result) {
            downtimes.putIfAbsent(invocation.getName(), Stopwatch.createUnstarted());
            Stopwatch stopwatch = downtimes.get(invocation.getName());

            synchronized (stopwatch) {
                if (result.isSuccessful() && stopwatch.isRunning()) {
                    stopwatch.stop();
                }

                if (!result.isSuccessful() && !stopwatch.isRunning()) {
                    stopwatch.start();
                }
            }
        }

        void printResults() {
            System.out.println("====================");
            for (Map.Entry<String, Stopwatch> downtime : downtimes.entrySet()) {
                System.out.println(format("Total downtime for %s: %ss",
                        downtime.getKey(),
                        downtime.getValue().elapsed(TimeUnit.SECONDS)));
            }
        }
    }
}
