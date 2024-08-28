package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.scenario.TestScenario;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.allElementsAreDifferent;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class ScenarioSplitTest extends ScenarioTest {

    @Test
    public void shouldNotMixContextAttributesInSplit() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow("parallel1")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE)),
                        flow("parallel2")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE)),
                        flow("parallel3")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(stack, allElementsAreDifferent());
    }

    @Test
    public void shouldNotMixContextAttributesInSplitAndSubflow() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow("parallel1")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE)),
                        flow("parallel2")
                                .addSubFlow(
                                        flow("subflow-inside-split1")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))),
                        flow("parallel3")
                                .addSubFlow(
                                        flow("subflow-inside-split2")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE)))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(stack, allElementsAreDifferent());
    }

    @Test
    public void shouldNotMixContextAttributesInSplit_multipleVUsers() {
        int vUsers = 3;
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow("parallel1")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                .withVusers(vUsers),
                        flow("parallel2")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                .withVusers(vUsers),
                        flow("parallel3")
                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                .withVusers(vUsers)
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(stack.size(), equalTo(vUsers * 3 /* flows */ * 2 /* vuserId + Object hash */));
        assertThat(stack, allElementsAreDifferent());
    }

    @Test
    public void shouldSupportSplitOnFlowLevel() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1")
                                .addTestStep(runnable(pushToStack("sequential1-start")))
                                .syncPoint("wait-for-start")
                                .split(
                                        flow("parallel1")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                                .withVusers(2),
                                        flow("parallel2")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                                .withVusers(3),
                                        flow("parallel3")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                                .withVusers(5)
                                )
                                .addTestStep(runnable(pushToStack("sequential1-end")))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(stack.subList(0, 2), contains("sequential1-start", "sequential1-start"));
        assertThat(stack.subList(2, stack.size() - 2), allElementsAreDifferent());
        assertThat(stack.subList(stack.size() - 2, stack.size()), contains("sequential1-end", "sequential1-end"));
        assertThat(stack.size(), equalTo((2 * 2 + 2 * 3 + 2 * 5 + 2) * 2 /* vuserId + Object hash */));
    }

}
