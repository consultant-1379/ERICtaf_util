package com.ericsson.de.scenariorx.examples;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.junit.Test;

/**
 * Compare with com.ericsson.cifwk.taf.scenario.examples.SharedDataSources
 */
public class RxSharedDataSourcesExample {

    private static final String STEP = "STEP";

    private RxDataSource<Integer> dataSource = fromIterable("integer", asList(1, 2));
    private final Stack<String> stack = new Stack<>();

    @TestStep(id = STEP)
    public void pushGlobalDsToStack(@Input("name") String name, @Input("integer") Integer integer) {
        stack.push(name + ": Data Record " + integer);
    }

    @Test
    public void depletion() throws Exception {
        // START SNIPPET: SHARED_DATA_DEPLETION
        RxScenario scenario = scenario("Scenario1")
                .addFlow(
                        flow("parent")
                                .addSubFlow(
                                        flow("subFlow1")
                                                .addTestStep(
                                                        TafRxScenarios.annotatedMethod(this, STEP)
                                                                .withParameter("name").value("subFlow1")
                                                )
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared())
                                )
                                .addSubFlow(
                                        flow("subFlow2")
                                                .addTestStep(
                                                        TafRxScenarios.annotatedMethod(this, STEP)
                                                                .withParameter("name").value("subFlow2")
                                                )
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared())
                                )
                )
                .build();
        // END SNIPPET: SHARED_DATA_DEPLETION

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("subFlow1: Data Record 1", "subFlow1: Data Record 2", "subFlow2: Data Record 1", "subFlow2: Data Record 2");
    }

    /**
     * In comparison to com.ericsson.cifwk.taf.scenario.examples.SharedDataSources#depletionInconsistency() Shared Data Source
     * behaves consistent regardless Flow or SubFlow.
     */
    @Test
    public void depletion2() throws Exception {
        // START SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_SUBFLOWS
        RxScenario scenario = scenario("Scenario1")
                .addFlow(
                        flow("parent")
                                .addSubFlow(
                                        flow("subFlow1")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                                        .withParameter("name").value("subFlow1"))
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared()))
                                .addSubFlow(
                                        flow("subFlow2")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                                        .withParameter("name").value("subFlow2"))
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared()))).build();
        // END SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_SUBFLOWS

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("subFlow1: Data Record 1", "subFlow1: Data Record 2", "subFlow2: Data Record 1", "subFlow2: Data Record 2");

        stack.clear();

        // START SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_FLOWS
        RxScenario scenario2 = scenario("Scenario2")
                .addFlow(
                        flow("flow1")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                        .withParameter("name").value("flow1"))
                                .withVUsers(2)
                                .withDataSources(dataSource.shared()))
                .addFlow(
                        flow("flow2")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                        .withParameter("name").value("flow2"))
                                .withVUsers(2)
                                .withDataSources(dataSource.shared()))
                .build();
        // END SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_FLOWS

        RxApi.run(scenario2);

        assertThat(stack).containsExactlyInAnyOrder("flow1: Data Record 1", "flow1: Data Record 2", "flow2: Data Record 1", "flow2: Data Record 2");
    }

    /**
     * For each execution of parentDataSource, subFlow1 will run with shared Data Source.
     */
    @Test
    public void hierarchicalDepletion() throws Exception {
        // START SNIPPET: SHARED_DATA_SOURCE_IN_SUB_FLOWS_EXAMPLE
        RxScenario scenario = scenario("Scenario1")
                .addFlow(
                        flow("parent")
                                .addSubFlow(
                                        flow("subFlow1")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                                        .withParameter("name").value("flow1"))
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared())
                                )
                                .withDataSources(fromIterable("parentDataSource", asList("a", "b"))) // 2 Data Records
                )
                .build();
        // END SNIPPET: SHARED_DATA_SOURCE_IN_SUB_FLOWS_EXAMPLE

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("flow1: Data Record 1", "flow1: Data Record 2", "flow1: Data Record 1", "flow1: Data Record 2");
    }

    /**
     * Each parallel flows works with own copy of Shared Data source. Each Data Source will get predictable Data Record count
     */
    @Test
    public void parallelDepletion() throws Exception {
        // START SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_FLOWS_RUNNING_TWO_FLOWS_IN_PARALLEL
        RxScenario scenario = scenario("Scenario1")
                .split(
                        flow("parent")
                                .split(
                                        flow("parallelFlow1")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                                        .withParameter("name").value("parallelFlow1"))
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared()),
                                        flow("parallelFlow2")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP)
                                                        .withParameter("name").value("parallelFlow2"))
                                                .withVUsers(2)
                                                .withDataSources(dataSource.shared())
                                )
                )
                .build();
        // END SNIPPET: SHARED_DATA_SOURCE_IN_MULTIPLE_FLOWS_RUNNING_TWO_FLOWS_IN_PARALLEL

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("parallelFlow1: Data Record 1", "parallelFlow1: Data Record 2",
                "parallelFlow2: Data Record 1", "parallelFlow2: Data Record 2");
    }

}
