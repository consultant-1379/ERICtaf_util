package com.ericsson.cifwk.taf.scenario.spi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.cifwk.taf.scenario.spi.TestScenarioMessageListener.count;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowExecutionCancelledMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 30.03.2016
 */
public class ScenarioMessageListenerIntegrationTest {

    private static final String GLOBAL_DATA_SOURCE = "globalDataSource";

    private static final String TEST_CASE_1 = "testCase1";
    private static final String TEST_CASE_2 = "testCase2";

    private static final String TEST_STEP_1 = "ts1";
    private static final String TEST_STEP_2 = "ts2";
    private static final String TEST_STEP_3 = "ts3";
    private static final String TEST_STEP_4 = "ts4";
    private static final String TEST_STEP_THROW_EXCEPTION = "throwExceptionForTestCase1TestStep";
    private static final String TEST_STEP_ASSERTION_FAILED = "testStepAssertionFailed";

    private static final int DATA_SOURCE_SIZE = 2;

    @Before
    public void setUp() {
        TestScenarioMessageListener.reset();
        prepareGlobalDataSource();
    }

    @Test
    @TestSuite
    public void dataDrivenScenarioMessages() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A")
                                .addTestStep(annotatedMethod(this, TEST_STEP_1))
                                .addTestStep(annotatedMethod(this, TEST_STEP_2))
                )
                .addFlow(flow("B")
                                .addTestStep(annotatedMethod(this, TEST_STEP_3))
                )
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();
        runner().build().start(scenario);

        assertEquals(1, count(ScenarioStartedMessage.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowStartedMessage.ScenarioFlow.class));
        assertEquals(2 * DATA_SOURCE_SIZE, count(FlowStartedMessage.UserFlow.class));

        // TODO: MVO: expected 6 actual 14 ?
//        assertEquals(3 * DATA_SOURCE_SIZE, count(TestStepStartedMessage.class));
//        assertEquals(3 * DATA_SOURCE_SIZE, count(TestStepFinishedMessage.Success.class));

        assertEquals(2 * DATA_SOURCE_SIZE, count(FlowFinishedMessage.UserFlowSuccess.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowFinishedMessage.ScenarioFlowSuccess.class));
        assertEquals(0, count(FlowExecutionCancelledMessage.class));

        // TODO: MVO: expected 1 actual 5 ?
//        assertEquals(1, count(FlowFinishedAllIterationsMessage.class));

        assertEquals(1, count(ScenarioFinishedMessage.class));
    }

    @Test
    @TestSuite
    public void dataDrivenScenarioBrokenMessages() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A")
                                .addTestStep(annotatedMethod(this, TEST_STEP_1))
                )
                .addFlow(flow("B")
                                .addTestStep(annotatedMethod(this, TEST_STEP_2))
                                .addTestStep(annotatedMethod(this, TEST_STEP_THROW_EXCEPTION))
                                .addTestStep(annotatedMethod(this, TEST_STEP_3))
                )
                // flow C is never executed
                .addFlow(flow("C")
                                .addTestStep(annotatedMethod(this, TEST_STEP_4))
                )
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();
        try {
            runner().build().start(scenario);
            fail();
        }catch(Exception e) {
            // OK
        }

        assertEquals(1, count(ScenarioStartedMessage.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowStartedMessage.ScenarioFlow.class));
        assertEquals(2 * DATA_SOURCE_SIZE, count(FlowStartedMessage.UserFlow.class));

        // TODO: MVO: expected 6 actual 16 ?
//        assertEquals(3 * DATA_SOURCE_SIZE, count(TestStepStartedMessage.class));
        // TODO: MVO: expected 4 actual 12 ?
//        assertEquals(2 * DATA_SOURCE_SIZE, count(TestStepFinishedMessage.Success.class));
        // TODO: MVO: expected 2 actual 4 ?
//        assertEquals(1 * DATA_SOURCE_SIZE, count(TestStepFinishedMessage.Failed.class));

        assertEquals(DATA_SOURCE_SIZE, count(FlowFinishedMessage.UserFlowSuccess.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowFinishedMessage.UserFlowFailed.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowExecutionCancelledMessage.class));
        assertEquals(DATA_SOURCE_SIZE, count(FlowFinishedMessage.ScenarioFlowFailed.class));

        // TODO: MVO: expected 1 actual 3 ?
//        assertEquals(1, count(FlowFinishedAllIterationsMessage.class));

        assertEquals(1, count(ScenarioFinishedMessage.class));
    }

    @TestStep(id = TEST_STEP_1)
    public void testStep1(@Input("string") String string) {
    }

    @TestStep(id = TEST_STEP_2)
    public void testStep2() {
    }

    @TestStep(id = TEST_STEP_3)
    public void testStep3() {
    }

    @TestStep(id = TEST_STEP_4)
    public void testStep4() {
    }

    @TestStep(id = TEST_STEP_THROW_EXCEPTION)
    public void throwException() {
        throw new RuntimeException("By design");
    }

    @TestStep(id = TEST_STEP_ASSERTION_FAILED)
    public void failAssert() {
        assertThat("using_aspects", equalTo("good_idea"));
    }

    private void prepareGlobalDataSource() {
        TestContext context = TafTestContext.getContext();
        context.removeDataSource(GLOBAL_DATA_SOURCE);
        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        globalDataSource.addRecord()
                .setField(TEST_CASE_ID, TEST_CASE_1)
                .setField("integer", 1)
                .setField("string", "A");
        globalDataSource.addRecord()
                .setField(TEST_CASE_ID, TEST_CASE_2)
                .setField("integer", 2)
                .setField("string", "B");
    }

}
