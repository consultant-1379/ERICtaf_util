package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.CAUSE_STEP_THREW_EXCEPTION;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.CAUSE_VOID_RETURN_TYPE;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.junit.Test;


public class ScenarioPipingTest extends ScenarioTest {

    public static final String TEST_STEP_EMPTY_DATA_RECORD_PRODUCER = "emptyDataRecordProducer";
    public static final String TEST_STEP_NAME_CONSUMER = "nameConsumer";
    public static final String TEST_STEP_DATA_RECORD_CONSUMER = "dataRecordConsumer";
    public static final String TEST_STEP_BASE_DATA_RECORD_PRODUCER = "baseDataRecordProducer";

    @Test
    public void shouldCollectValues() throws Exception {
        runner.start(defaultScenario(STEP_VALUE_PRODUCER));

        List<String> result = FluentIterable.from(ServiceRegistry.getTestContextProvider().get().dataSource(MY_DATA_SOURCE_1))
                .transform(new Function<DataRecord, String>() {
                    @Override
                    public String apply(DataRecord input) {
                        return input.getFieldValue(STEP_VALUE_PRODUCER);
                    }
                }).toList();
        assertThat(result, containsInAnyOrder("hello from 1", "hello from 2", "hello from 3"));
    }

    @Test
    public void shouldThrowExceptionOnVoidReturnType() throws Exception {
        TestScenario scenario = defaultScenario(STEP_VOID_PRODUCER);
        expectException(scenario, STEP_VOID_PRODUCER, CAUSE_VOID_RETURN_TYPE);
    }

    @Test
    public void shouldThrowExceptionOnFailedStep() throws Exception {
        TestScenario scenario = scenarioBuilder(STEP_EXCEPTION_PRODUCER)
                .withExceptionHandler(new PropagateIllegalStateException())
                .build();
        expectException(scenario, STEP_EXCEPTION_PRODUCER, CAUSE_STEP_THREW_EXCEPTION);
    }

    @Test
    public void shouldCollectDataRecords() throws Exception {
        runner.start(defaultScenario(STEP_DATA_RECORD_PRODUCER));

        List<String> result = FluentIterable.from(ServiceRegistry.getTestContextProvider().get().dataSource(MY_DATA_SOURCE_1))
                .transform(new Function<DataRecord, String>() {
                    @Override
                    public String apply(DataRecord input) {
                        return input.getFieldValue("name");
                    }
                }).toList();
        assertThat(result, containsInAnyOrder("nodeName1", "nodeName2", "nodeName3"));
    }

    @Test
    public void shouldPassDataSourcesWithDifferentNames2() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(getProducerFlow())
                .addFlow(bindDataSource(MY_DATA_SOURCE_2, MY_DATA_SOURCE_1))
                .addFlow(getConsumerFlowWithDifferentDSName())
                .build();

        runner.start(scenario);

        assertThat(stack, containsInAnyOrder("nodeName1", "nodeName2", "nodeName3"));
    }

    private TestStepFlowBuilder bindDataSource(final String myDataSource2, final String myDataSource1) {
        return flow("convertDs").afterFlow(new Runnable() {
            @Override
            public void run() {
                TestContext context = ServiceRegistry.getTestContextProvider().get();
                context.addDataSource(myDataSource2, context.dataSource(myDataSource1));
            }
        });
    }

    @Test
    public void shouldPassDataSourcesWithDifferentNames3() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(getProducerFlow())
                .addFlow(getConsumerFlowWithDifferentDSName().bindDataSource(MY_DATA_SOURCE_2, dataSource(MY_DATA_SOURCE_1)))
                .build();

        runner.start(scenario);

        assertThat(stack, containsInAnyOrder("nodeName1", "nodeName2", "nodeName3"));
    }

    @Test
    public void testTransformParameterDataRecordToImplementation() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(flow("producer")
                        .addTestStep(annotatedMethod(this, TEST_STEP_BASE_DATA_RECORD_PRODUCER))
                        .addTestStep(annotatedMethod(this, TEST_STEP_DATA_RECORD_CONSUMER)
                                .withParameter(MY_DATA_SOURCE_2, fromTestStepResult(TEST_STEP_BASE_DATA_RECORD_PRODUCER))))
                .build();

        runner.start(scenario);

        System.out.println(stack);

        assertThat(stack, containsInAnyOrder("1"));
    }

    @Test
    public void testEmptyDataRecords() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(flow("producer")
                        .addTestStep(annotatedMethod(this, TEST_STEP_EMPTY_DATA_RECORD_PRODUCER)
                                .collectResultToDatasource(MY_DATA_SOURCE_2))
                        .withVusers(3))
                .addFlow(flow("consumer")
                        .addTestStep(annotatedMethod(this, TEST_STEP_DATA_RECORD_CONSUMER))
                        .withDataSources(dataSource(MY_DATA_SOURCE_2)))
                .build();

        runner.start(scenario);

        assertThat(stack, containsInAnyOrder("1", "3"));
    }

    @Test
    public void testPassEmptyDataRecord() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(flow("producer")
                        .addTestStep(annotatedMethod(this, TEST_STEP_EMPTY_DATA_RECORD_PRODUCER))
                        .addTestStep(annotatedMethod(this, TEST_STEP_DATA_RECORD_CONSUMER)
                                .withParameter(MY_DATA_SOURCE_2, fromTestStepResult(TEST_STEP_EMPTY_DATA_RECORD_PRODUCER)))
                        .withVusers(3))
                .build();

        runner.start(scenario);

        System.out.println(stack);

        assertThat(stack, containsInAnyOrder("1", null, "3"));
    }

    @Test
    public void testPassEmptyDataRecordField() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(flow("producer")
                        .addTestStep(annotatedMethod(this, TEST_STEP_EMPTY_DATA_RECORD_PRODUCER))
                        .addTestStep(annotatedMethod(this, TEST_STEP_NAME_CONSUMER)
                                .withParameter(MY_DATA_SOURCE_2, fromTestStepResult(TEST_STEP_EMPTY_DATA_RECORD_PRODUCER)))
                        .withVusers(3))
                .build();

        runner.start(scenario);

        System.out.println(stack);

        assertThat(stack, containsInAnyOrder((String) null, null, null));
    }

    private TestScenario defaultScenario(String producerStep) {
        return scenarioBuilder(producerStep).build();
    }

    private TestScenarioBuilder scenarioBuilder(String producerStep) {
        return scenario("test1")
                .addFlow(flow("flow")
                        .addTestStep(annotatedMethod(this, producerStep)
                                .collectResultToDatasource(MY_DATA_SOURCE_1))
                        .withVusers(3));
    }

    private TestStepFlow getProducerFlow() {
        return flow("flow")
                .addTestStep(annotatedMethod(this, STEP_DATA_RECORD_PRODUCER)
                        .collectResultToDatasource(MY_DATA_SOURCE_1))
                .withVusers(3)
                .build();
    }

    private TestStepFlow getConsumerFlowWithDifferentDSName() {
        return flow("flow")
                .addTestStep(annotatedMethod(this, TEST_STEP_DATA_RECORD_CONSUMER))
                .withDataSources(dataSource(MY_DATA_SOURCE_2))
                .build();
    }

    @TestStep(id = TEST_STEP_DATA_RECORD_CONSUMER)
    public void testStep1(@Input(MY_DATA_SOURCE_2) NetworkNode node) {
        stack.push(node.getName());
    }

    @TestStep(id = TEST_STEP_EMPTY_DATA_RECORD_PRODUCER)
    public DataRecord testStep2() {
        int vUser = TafTestContext.getContext().getVUser();
        if (vUser == 2) {
            return DataRecordImpl.EMPTY;
        } else {
            return TafDataSources.dataRecordBuilder()
                    .setField("name", vUser)
                    .build();
        }
    }

    @TestStep(id = TEST_STEP_NAME_CONSUMER)
    public void testStep3(@Input("name") String name) {
        stack.push(name);
    }

    @TestStep(id = TEST_STEP_BASE_DATA_RECORD_PRODUCER)
    public DataRecord testStep4() {
        int vUser = TafTestContext.getContext().getVUser();
        return TafDataSources.dataRecordBuilder()
                .setField("name", vUser)
                .build();
    }
}
