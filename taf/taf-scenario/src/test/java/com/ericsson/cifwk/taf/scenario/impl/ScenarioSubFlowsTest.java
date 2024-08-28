/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.allElementsAreDifferent;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ScenarioSubFlowsTest extends ScenarioTest {
    private static final String STEP_NODE_PREPARATION = "nodePreparation";
    public static final String ATTRIBUTE = "attribute";
    public static final String PUSH_VUSER = "pushVUser";
    public static final String SET_ATTRIBUTE = "setAttribute";
    public static final String CHECK_ATTRIBUTE = "checkAttribute";
    private static final Logger logger = LoggerFactory
            .getLogger(ScenarioSubFlowsTest.class);

    @Test
    public void shouldSupportSubFlows_CIS_5633() {
        String NODES_TEMP = "NODES_TEMP";
        String AI_OPTIONS = "AI_OPTIONS";
        String SEC_OPTIONS = "SEC_OPTIONS";

        TestContext context = ServiceRegistry.getTestContextProvider().get();

        context.dataSource(NODES_TEMP).addRecord().setField("attribute1", "node1").setField("value1", 111);
        context.dataSource(AI_OPTIONS).addRecord().setField("attribute2", "node2").setField("value2", 222);
        context.dataSource(SEC_OPTIONS).addRecord().setField("attribute3", "node3").setField("value3", 333);

        TestStepFlow prepareNodes = flow("use all node templates")
                .withDataSources(dataSource(NODES_TEMP))
                .addTestStep(annotatedMethod(this, STEP_NODE_PREPARATION))
                .build();

        TestStepFlow combineAiOptionsWithNodeTemplates = flow("use all ai options")
                .withDataSources(dataSource(AI_OPTIONS))
                .addSubFlow(prepareNodes)
                .build();

        TestScenario dataPopulator = scenario("populate data")
                .addFlow(flow("populate nodes")
                        .addSubFlow(flow("use all sec options")
                                .withDataSources(dataSource(SEC_OPTIONS))
                                .addSubFlow(combineAiOptionsWithNodeTemplates)
                                .build()
                        )
                )
                .build();

        runner.start(dataPopulator);
    }

    @TestStep(id = STEP_NODE_PREPARATION)
    public void nodePreparation(
            @Input("attribute1") String attribute1, @Input("value1") Integer value1,
            @Input("attribute2") String attribute2, @Input("value2") Integer value2,
            @Input("attribute3") String attribute3, @Input("value3") Integer value3) {
        assertThat(attribute1, equalTo("node1"));
        assertThat(value1, equalTo(111));
        assertThat(attribute2, equalTo("node2"));
        assertThat(value2, equalTo(222));
        assertThat(attribute3, equalTo("node3"));
        assertThat(value3, equalTo(333));
    }

    @Test
    public void subflowFlowVUsers_CIS_6465() {
        TestStepFlow flow = flow("complex flow")
                .addSubFlow(flow("sub-flow")
                        .addTestStep(runnable(counter))
                        .build())
                .withVusers(3)
                .build();

        runner().build().start(
                scenario()
                        .withDefaultVusers(3)
                        .addFlow(flow)
                        .build());

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void flowVUsers_CIS_6465() {
        TestStepFlow flow = flow("complex flow")
                .addTestStep(runnable(counter))
                .withVusers(3)
                .build();

        runner().build().start(
                scenario()
                        .withDefaultVusers(3)
                        .addFlow(flow)
                        .build());

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void shouldFinishVUserThreadBeforeStartingNextOne_CIS_9566() {
        final int VUSERS = 2;
        TestStepFlow flow = flow("complex flow")
                .addSubFlow(flow("sub-flow")
                        .addTestStep(runnable(new Runnable() {
                            @Override
                            public void run() {
                                int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
                                stack.push("" + vUser + "start");
                                sleep(1000L);
                                stack.push("" + vUser + "end");
                            }
                        }))
                        .build())
                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .withVusers(VUSERS)
                .build();

        runner().build().start(
                scenario()
                        .withDefaultVusers(VUSERS)
                        .addFlow(flow)
                        .build());

        logger.info("Stack:" + stack);

        assertThat(stack, hasSize(8));
        assertThat(Iterables.filter(stack, startsWith("1")), contains("1start", "1end", "1start", "1end"));
        assertThat(Iterables.filter(stack, startsWith("2")), contains("2start", "2end", "2start", "2end"));
    }

    private Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return s.startsWith(prefix);
            }
        };
    }

    @Test
    public void shouldUseDifferentVUsersInDoParallel() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1")
                                .addTestStep(runnable(pushToStack("sequential1-start")))
                                .syncPoint("wait-for-start")
                                .split(flow("subflow1")
                                        .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                        .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                        .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                        .split(flow("subsubflow1")
                                                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                                                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                                                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                                                .withVusers(2))
                                        .withVusers(2)
                                )
                                .syncPoint("wait-for-end")
                                .addTestStep(runnable(pushToStack("sequential1-end")))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        logger.info("Stack:" + stack);

        assertThat(stack.subList(0, 2), Matchers.contains("sequential1-start", "sequential1-start"));
        assertThat(stack.subList(2, stack.size() - 2), allElementsAreDifferent());
        assertThat(stack.subList(stack.size() - 2, stack.size()), Matchers.contains("sequential1-end", "sequential1-end"));
    }
}
