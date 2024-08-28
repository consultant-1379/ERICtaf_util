/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.examples;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.de.scenariorx.api.RxApi.contextDataSource;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.CONSUMER;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.CREATE_NODE;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.CREATE_NODE_2;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.DO_STUFF;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.LOGIN;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.LOGIN2;
import static com.ericsson.de.scenariorx.examples.ContextExample.TestIds.PRODUCER;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBuilder;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxContextDataSource;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.api.ScenarioContext;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.examples.mocks.HttpTool;
import com.ericsson.de.scenariorx.examples.mocks.Operator;
import com.google.common.collect.Lists;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(moduleFactory = OperatorLookupModuleFactory.class)
public class ContextExample {

    private static final Stack<String> STACK = new Stack<>();

    static class TestIds {
        public static final String LOGIN = "LOGIN";
        public static final String DO_STUFF = "DO_STUFF";
        public static final String PRODUCER = "PRODUCER";
        public static final String CONSUMER = "CONSUMER";
        public static final String LOGIN2 = "LOGIN2";
        public static final String CREATE_NODE = "CREATE_NODE";
        public static final String CREATE_NODE_2 = "CREATE_NODE_2";
    }

    @BeforeMethod
    public void clearStack() {
        STACK.clear();
    }

    @Inject
    private Operator operator;

    // START SNIPPET: PASSING_DATA_BETWEEN_STEPS
    @TestStep(id = LOGIN)
    public DataRecord login() {
        HttpTool tool = operator.login("username", "password");

        return new DataRecordBuilder()
                .setField("session", tool)
                .build(); // ← value will be put to context
    }

    @TestStep(id = DO_STUFF)
    public void checkDataRecordName(
            //existing approach ↓
            @Input("dataSourceName.dataRecordName") String value1, // ← Injection from Data Source full name
            @Input("dataRecordName") String value2, // ← Injection from Data Source Data Record name
            @Input("dataSourceName") DataRecord dataRecord, // ← Injection from Data Source

            //Rx Scenario approach ↓
            @Input("session") HttpTool tool1, // ← Injection from context
            @Input("scenarioContext.session") HttpTool tool2, // ← Injection from context full name
            @Input(ScenarioContext.CONTEXT_RECORD_NAME) ScenarioContext scenarioContext) { // ← Injection from context all values

        tool1.request(); //...
        HttpTool tool3 = scenarioContext.getFieldValue("session"); //note that there is no setValue!
    }
    // END SNIPPET: PASSING_DATA_BETWEEN_STEPS

    // START SNIPPET: RETURN_MULTIPLE_OBJECTS_EXAMPLE
    @TestStep(id = PRODUCER)
    public DataRecord produce() {
        Object object1 = operator.getObject1();
        Object object2 = operator.getObject2();
        Object object3 = operator.getObject3();

        STACK.push(PRODUCER);

        return new DataRecordBuilder()
                .setField("object1", object1)
                .setField("object2", object2)
                .setField("object2", object3)
                .build();
    }

    @TestStep(id = CONSUMER)
    public void consumer(
            @Input("object1") Object object1,
            @Input("object2") Object object2,
            @Input("object3") Object object3) {
        STACK.push(CONSUMER);
        //...
    }
    // END SNIPPET: RETURN_MULTIPLE_OBJECTS_EXAMPLE

    @TestStep(id = LOGIN2)
    public HttpTool login2() {
        HttpTool tool = operator.login("username", "password");
        STACK.push(LOGIN2);
        return tool; // ← does not put value to context, but could be usable in other steps
    }

    /**
     * @see #createNode2 as alternative approach
     */
    @TestStep(id = CREATE_NODE)
    public DataRecord createNode() {
        DataRecord node = operator.createNode();
        STACK.push(CREATE_NODE);
        return node; // ← could be usable in other steps
    }

    @TestStep(id = CREATE_NODE_2)
    public DataRecord createNode2() {
        DataRecord node = operator.createNode();
        STACK.push(CREATE_NODE_2);
        return new DataRecordBuilder()
                .setField("nodeType", "nodeType")
                .setField("networkElementId", "networkElementId")
                .build(TafNode.class);
    }

    @Test
    public void passByDifferentName() throws Exception {
        //definition
        Iterable<String> iterable = Lists.newArrayList("1", "2");
        RxTestStep loginStep = TafRxScenarios.annotatedMethod(this, LOGIN2);
        RxTestStep createNodeStep = TafRxScenarios.annotatedMethod(this, CREATE_NODE);
        RxDataSource<String> dataSource = fromIterable("dataSource1", iterable);

        RxScenario scenario = scenario()
                .addFlow(
                        flow()
                                .addTestStep(loginStep) //use definition
                                .addTestStep(createNodeStep)
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, CONSUMER)
                                                .withParameter("object1").fromTestStepResult(loginStep)
                                                .withParameter("object2").fromDataSource(dataSource)
                                                .withParameter("object3").fromTestStepResult(createNodeStep)
                                )
                                .withDataSources(dataSource)
                )
                .build();

        RxApi.run(scenario);

        assertThat(STACK).containsExactly(LOGIN2, CREATE_NODE, CONSUMER, LOGIN2, CREATE_NODE, CONSUMER);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Parameter 'object1' for test step 'ContextExample#CONSUMER' value is null")
    public void passByDifferentNameAllowedOnlyForTheOneFlowScope() {
        //definition
        Iterable<String> iterable = Lists.newArrayList("1", "2");
        RxTestStep loginStep = TafRxScenarios.annotatedMethod(this, LOGIN2);
        RxTestStep createNodeStep = TafRxScenarios.annotatedMethod(this, CREATE_NODE);
        RxDataSource<String> dataSource = fromIterable("dataSource1", iterable);

        RxScenario scenario = scenario()
                .addFlow(
                        flow()
                                .addTestStep(loginStep) //use definition
                                .addTestStep(createNodeStep)
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, CONSUMER)
                                                .withParameter("object1").fromTestStepResult(loginStep)
                                                .withParameter("object2").fromDataSource(dataSource)
                                                .withParameter("object3").fromTestStepResult(createNodeStep)
                                )
                                .withDataSources(dataSource)
                )
                .addFlow(
                        flow()
                                .addTestStep( //will throw exception because passing only works in scope of one flow
                                        TafRxScenarios.annotatedMethod(this, CONSUMER)
                                                .withParameter("object1").fromTestStepResult(loginStep)
                                                .withParameter("object2").fromDataSource(dataSource)
                                                .withParameter("object3").fromTestStepResult(createNodeStep)
                                )
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void adaptTestStep() throws Exception {
        RxDataSource<TafNode> nodes = TafRxScenarios.dataSource("NODES", fromCsv("data/node.csv"), TafNode.class);

        // START SNIPPET:  ADAPTING_TEST_STEPS
        RxTestStep adaptedConsumer = TafRxScenarios.annotatedMethod(this, CONSUMER)
                .withParameter("object1").fromDataSource(nodes, TafNode.NETWORK_ELEMENT_ID)
                .withParameter("object2").fromDataSource(nodes, TafNode.NODE_TYPE);

        RxScenario scenario = scenario()
                .addFlow(
                        flow()
                                .addTestStep(
                                        adaptedConsumer
                                                .withParameter("object3").value("constant1")
                                )
                                .withDataSources(nodes)
                )
                .addFlow(
                        flow()
                                .addTestStep(
                                        adaptedConsumer
                                                .withParameter("object3").value("constant2")
                                )
                                .withDataSources(nodes)
                )
                .build();
        // END SNIPPET: ADAPTING_TEST_STEPS

        RxApi.run(scenario);

        assertThat(STACK).containsExactly(CONSUMER, CONSUMER, CONSUMER, CONSUMER, CONSUMER, CONSUMER);
    }

    @Test(enabled = false)
    public void passDataBetweenFlows() throws Exception {
        RxContextDataSource<TafNode> dataSource = contextDataSource("name", TafNode.class);

        RxScenario scenario = scenario()
                .addFlow(
                        flow(PRODUCER)
                                .addTestStep(TafRxScenarios.annotatedMethod(this, CREATE_NODE)
                                        .collectResultsToDataSource(dataSource))
                                .withVUsers(4) // will produce 4 Data Records
                )
                .addFlow(
                        flow(CONSUMER)
                                .addTestStep(TafRxScenarios.annotatedMethod(this, CONSUMER))
                                .withVUsers(2) // each vUser will get 2 Data Records
                                .withDataSources(dataSource)
                )
                .build();

        RxApi.run(scenario);

        assertThat(STACK).containsExactly(PRODUCER, CONSUMER, PRODUCER, CONSUMER, PRODUCER, PRODUCER);
    }

    @TestStep(id = "CREATE_MULTIPLE_NODES")
    public Collection<DataRecord> createMultipleNodes() {
        //...
        List<DataRecord> nodes = newArrayList();
        for (int i = 0; i < 3; i++) {
            TafNode node = new DataRecordBuilder()
                    .setField("nodeType", "nodeType")
                    .setField("networkElementId", "networkElementId")
                    .build(TafNode.class);

            nodes.add(node);
        }

        return nodes;
    }
}
