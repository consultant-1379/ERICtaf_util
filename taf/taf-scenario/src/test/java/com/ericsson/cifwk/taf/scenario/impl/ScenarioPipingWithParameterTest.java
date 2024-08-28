package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.CAUSE_STEP_RESULT_NOT_FOUND;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.CAUSE_STEP_THREW_EXCEPTION;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.CAUSE_VOID_RETURN_TYPE;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromDataSourceField;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;


public class ScenarioPipingWithParameterTest extends ScenarioTest {

    @Test
    public void shouldPipeValueBetweenTestSteps() throws Exception {
        runner.start(defaultScenario(STEP_VALUE_PRODUCER));
        assertThat(stack, contains("hello from 1"));
    }

    @Test
    public void shouldThrowExceptionOnVoidProducer() throws Exception {
        TestScenario scenario = defaultScenario(STEP_VOID_PRODUCER);
        expectException(scenario, STEP_VOID_PRODUCER, CAUSE_VOID_RETURN_TYPE);
    }

    @Test
    public void shouldPipeDataRecordBetweenTestSteps() throws Exception {
        runner.start(defaultScenario(STEP_DATA_RECORD_PRODUCER, STEP_DATA_RECORD_CONSUMER));
        assertThat((String) dataRecord.getFieldValue("name"), equalTo("nodeName1"));
        assertThat(dataRecord.getDataSourceName(), equalTo("unknown"));
    }

    @Test
    public void shouldThrowExceptionOnBrokenProducer() throws Exception {
        TestScenario scenario = scenarioBuilder(STEP_EXCEPTION_PRODUCER, STEP_PUSH_CSV_DS_TO_STACK, STEP_EXCEPTION_PRODUCER)
                .withExceptionHandler(new PropagateIllegalStateException()).build();
        expectException(scenario, STEP_EXCEPTION_PRODUCER, CAUSE_STEP_THREW_EXCEPTION);
    }

    @Test
    public void shouldThrowExceptionOnWrongStepName() throws Exception {
        TestScenario scenario = defaultScenario(STEP_VALUE_PRODUCER, STEP_PUSH_CSV_DS_TO_STACK, "WRONG_STEP_NAME");
        expectException(scenario, "WRONG_STEP_NAME", CAUSE_STEP_RESULT_NOT_FOUND);
    }

    @Test
    public void parameterValueShouldOverrideDataSourceInput() throws Exception {
        // precondition test
        TestScenario scenario = scenario("test1")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE))
                ).build();
        runner.start(scenario);
        assertThat(stack, contains("attr1", "attr2", "attr3"));
        stack.clear();
        // actual test
        scenario = scenario("test1")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)
                                        .withParameter("attribute", fromTestStepResult(STEP_VALUE_PRODUCER)))
                                .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE))
                ).build();
        runner.start(scenario);
        assertThat(stack, contains("hello from 1", "hello from 1", "hello from 1"));
    }

    @Test
    public void shouldPassNullParameter() throws Exception {
        TestScenario scenario = scenario("test1")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)))
                .build();
        runner.start(scenario);
        assertThat(stack, contains((String) null));
    }

    @Test
    public void withParameterValueShouldOverrideDataRecordInput() throws Exception {
        TestScenario scenario = scenario("WithParameter overrides DataRecord values")
                .addFlow(flow("flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_DATARECORD_DS_TO_STACK))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_DATARECORD_DS_TO_STACK)
                                .withParameter("simpleProvider.nodeId", "4"))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_DATARECORD_DS_TO_STACK))
                        .withDataSources(dataSource(SIMPLE_PROVIDER))
                )
                .build();
        runner.start(scenario);
        assertThat(stack, contains("1", "4", "1",
                "2", "4", "2",
                "3", "4", "3"));
    }

    @Test
    public void shouldReplaceDatasourceFieldWithDataFromAnotherField() {
        TestScenario scenario = scenario("replace data in datasource scenario")
                .addFlow(flow("cyclic data flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)                   //should run as normal when parameter not used
                                .withParameter("nodeId", fromDataSourceField("value")))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)                   //should replace data for single column
                                .withParameter("attribute", fromDataSourceField("subscription")))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_MULTIPLE_CSV_DS_COLUMNS_TO_STACK)  //should replace multiple columns
                                .withParameter("attribute", fromDataSourceField("value"))
                                .withParameter("value", fromDataSourceField("collection")))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_MULTIPLE_CSV_DS_COLUMNS_TO_STACK)  //should replace with original value not an already replaced value
                                .withParameter("value", fromDataSourceField("collection"))
                                .withParameter("attribute", fromDataSourceField("value")))
                        .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE))
                ).build();
        runner.start(scenario);
        assertThat(stack, contains("attr1", "subscription1", "111", "collection1", "111", "collection1",
                "attr2", "subscription2", "222", "collection2", "222", "collection2",
                "attr3", "subscription3", "333", "collection3", "333", "collection3"));
    }

    @Test
    public void shouldReplaceDatasourceFieldWithDataFromAnotherDataSource() {
        TestScenario scenario = scenario("replace data in datasource scenario")
                .addFlow(flow("cyclic data flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_DATARECORD_DS_TO_STACK)
                                .withParameter("simpleProvider.nodeId", fromDataSourceField(SIMPLE_PROVIDER2, "attribute2")))
                        .withDataSources(dataSource(SIMPLE_PROVIDER), dataSource(SIMPLE_PROVIDER2))
                ).build();
        runner.start(scenario);
        assertThat(stack, contains("attr4", "attr5"));
    }

    @Test
    public void shouldReplaceDatasourceFieldWithDataFromAnotherRecursiveDataSource() {
        TestScenario scenario = scenario("replace data in datasource scenario")
                .addFlow(flow("cyclic data flow")
                        .addTestStep(annotatedMethod(this, STEP_PUSH_DATARECORD_DS_TO_STACK)
                                .withParameter("simpleProvider.nodeId", fromDataSourceField(SIMPLE_PROVIDER2_REPEAT, "attribute2")))
                        .withDataSources(dataSource(SIMPLE_PROVIDER), dataSource(SIMPLE_PROVIDER2_REPEAT))
                ).build();
        runner.start(scenario);
        assertThat(stack, contains("attr4", "attr5", "attr4"));
    }

    @Test
    public void shouldThrowParameterAlreadySetException() {
        try {
            TestScenario scenario = scenario("replace data in datasource scenario")
                    .addFlow(flow("cyclic data flow")
                            .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)
                                    .withParameter("attribute", "errorShouldBeThrown")
                                    .withParameter("attribute", fromDataSourceField("errorShouldBeThrown")))
                            .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE))
                    ).build();
            fail("Parameter already set error was not thrown");
        } catch (IllegalArgumentException expectedException) {
            assertThat(expectedException.getLocalizedMessage(), equalTo("Parameter is already set : attribute"));
        }
    }

    @Test
    public void shouldThrowNoDataSourceParameterFoundException() {
        try {
            TestScenario scenario = scenario("replace data in datasource scenario")
                    .addFlow(flow("cyclic data flow")
                            .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK)
                                    .withParameter("attribute", fromDataSourceField("invalidColumnName")))
                            .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE))
                    ).build();
            runner.start(scenario);
            fail("Parameter not found error was not thrown");
        } catch (IllegalArgumentException expectedException) {
            assertThat(expectedException.getLocalizedMessage(), equalTo("Replacement parameter 'invalidColumnName' could not be found"));
        }
    }

    private TestScenario defaultScenario(String step1) {
        return scenarioBuilder(step1, STEP_PUSH_CSV_DS_TO_STACK, step1).build();
    }

    private TestScenario defaultScenario(String step1, String step2) {
        return scenarioBuilder(step1, step2, step1).build();
    }

    private TestScenario defaultScenario(String step1, String step2, String parameterFrom) {
        return scenarioBuilder(step1, step2, parameterFrom).build();
    }

    private TestScenarioBuilder scenarioBuilder(String step1, String step2, String parameterFrom) {
        return scenario("test1")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, step1))
                                .addTestStep(annotatedMethod(this, step2)
                                        .withParameter("attribute", fromTestStepResult(parameterFrom))));
    }

}
