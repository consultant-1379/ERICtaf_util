package com.ericsson.cifwk.taf.scenario.impl;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

import static junit.framework.Assert.fail;

import com.ericsson.cifwk.taf.scenario.impl.datasource.FilteredDataSourceInfo;
import org.junit.Test;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;

public class DataDrivenScenarioTest extends ScenarioTest {
    @Test
    @TestSuite
    public void filteredDataSourceScenario() {


        int size = FilteredDataSourceInfo.getFilteredDataSourceSize(USER, "roles", "op1");
        System.out.println("Size of the filtered datasource is: " + size);
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("Login Flow")
                        .addTestStep(annotatedMethod(this, LOGIN)))
                .addFlow(flow("Logout Flow")
                        .addTestStep(annotatedMethod(this, LOGOUT)))
                .withScenarioDataSources(dataSource(USER).withFilter("roles contains 'op1'"))
                .doParallel(size)
                .build();
        runner.start(scenario);
    }

    @Test
    @TestSuite
    public void scenarioDataSourceIteration() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                )
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();

        runner.start(scenario);

        assertThat(stack, contains("1", "A", "2", "B"));
    }

    @Test
    @TestSuite
    public void testCaseIndependence() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_SINGLE_EXCEPTION_PRODUCER)
                                        .withParameter("recordValueToFail", "2"))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                )
                .withScenarioDataSources(dataSource(CSV_DATA_SOURCE)
                        .bindColumn("nodeId", "integer")
                        .bindColumn("value", "string"))
                .build();

        try {
            runner.start(scenario);
            fail();
        } catch(RuntimeException e) {
            assertThat(e.getMessage(), containsString("Scenario contains one or more broken/failed test cases"));
            assertThat(stack, contains("1", "111", "3", "333"));
        }

    }

    @Test
    @TestSuite
    public void testScenarioDataSourceIteration_flowDataSource() throws Exception {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                .withDataSources(dataSource(CSV_DATA_SOURCE))

                )
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();

        runner.start(scenario);

        assertThat(stack, contains(
                        "1", "A", "attr1",
                        "1", "A", "attr2",
                        "1", "A", "attr3",

                        "2", "B", "attr1",
                        "2", "B", "attr2",
                        "2", "B", "attr3")
        );
    }

    @Test
    @TestSuite
    public void handledExceptionInSubflow() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A")
                        .addSubFlow(
                                flow("subFlow")
                                        .addTestStep(runnable(counter))
                                        .addTestStep(annotatedMethod(this, STEP_EXCEPTION_PRODUCER))
                                        .addTestStep(runnable(counter)))
                        .addTestStep(runnable(counter))
                )
                .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                .withScenarioDataSources(dataSource(MY_DATA_SOURCE_1).withFilter("string == 'A'"))
                .doParallel(2)
                .build();

        TestContext context = TafTestContext.getContext();

        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        context.addDataSource(MY_DATA_SOURCE_1, shared(globalDataSource));

        runner().build().start(scenario);
    }

    @Test
    @TestSuite
    public void scenarioDataSourceIterationWithVUsers() {
        TestDataSource shared = TafDataSources.shared(csvDataSource);
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared);

        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                )
                .withScenarioDataSources(dataSource(MY_DATA_SOURCE_1))
                .doParallel(4)
                .build();

        runner.start(scenario);

        printStack();
        assertThat(stack, containsInAnyOrder("attr1", "attr2", "attr3"));
    }

    @Test(expected = IllegalStateException.class)
    @TestSuite
    public void scenarioDataSourceThrowExceptionIfDsNotShared() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                )
                .withScenarioDataSources(dataSource(SHARED_DATA_SOURCE), dataSource(GLOBAL_DATA_SOURCE))
                .doParallel(4)
                .build();

        runner.start(scenario);
    }

    @Test
    @TestSuite
    public void scenarioDataSourceWithNotSharedDataSource() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                )
                .withScenarioDataSources(dataSource(CSV_DATA_SOURCE))
                .allowNotSharedFlows()
                .doParallel(2)
                .build();

        runner.start(scenario);

        assertThat(stack, hasSize(6));
    }
}
