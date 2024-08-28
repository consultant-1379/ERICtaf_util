package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceControl;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.google.common.base.Predicate;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.iterable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TafScenarioRunnerTest extends ScenarioTest {

    private static final String STEP_OK = "STEP_OK";
    private static final String STEP_CONSUMER = "STEP_CONSUMER";
    private static final String STEP_NO_VALUE = "STEP_NO_VALUE";
    private static final String STEP_FAIL = "STEP_FAIL";
    private static final String STEP_CHECK_DATASOURCE = "STEP_CHECK_DATASOURCE";
    private static final String STEP_PUSH_GLOBAL_DS = "PUSH_GLOBAL_DS";
    private static final String STEP_PUSH_CSV_AND_GLOBAL_DS = "STEP_PUSH_CSV_AND_GLOBAL_DS";
    private static final String STEP_WITHOUT_INPUT_ANNOTATION = "STEP_WITHOUT_INPUT_ANNOTATION";
    private static final String STEP_CYCLIC_DATASOURCE = "STEP_CYCLIC_DATASOURCE";

    private static final String CYCLIC_DATA_SOURCE = "cyclicDs";

    private Integer integerParam;
    private String stringParam;

    @Before
    public void setUp() {
        super.setUp();

        integerParam = null;
        stringParam = null;
    }

    @Test
    public void helloTest() {
        runner.start(scenario().build());
    }

    @Test
    public void shouldRunWithoutDataSources() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .addTestStep(runnable(counter))
                .addTestStep(runnable(counter))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(9));
    }

    @Test
    public void shouldRunWithDataSources() {
        List<Map<String, Object>> list = prepareList(3);

        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(iterable("a", list))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void shouldRunWithFilteredDataSources() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(CSV_DATA_SOURCE).withFilter("nodeId==1")
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
    }

    @Test
    public void shouldRunWithPredicateDataSources() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(CSV_DATA_SOURCE).withFilter(new Predicate<DataRecord>() {
                            @Override
                            public boolean apply(DataRecord dataRecord) {
                                return "1".equals(dataRecord.getFieldValue("nodeId"));
                            }
                        })
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
    }

    @Test
    public void shouldRunWithDataSourceOnFlowLevel() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(iterable("a", prepareList(3)))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void shouldPopulateTestStepParameters() {
        TestStepFlow flow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_OK)
                        .withParameter("integer", 42))
                .withDataSources(iterable("x", prepareList(1)))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
        assertThat(integerParam, equalTo(42));
        assertThat(stringParam, nullValue());
    }

    @Test
    public void shouldPopulateParametersWithConversion() {
        TestStepFlow flow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_OK)
                        .withParameter("integer", "42")
                        .withParameter("string", "A"))
                .withDataSources(iterable("x", prepareList(1)))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
        assertThat(integerParam, equalTo(42));
        assertThat(stringParam, equalTo("A"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailTest() {
        TestScenario scenario = scenario()
                .addFlow(singleStepFlow("fail", annotatedMethod(this, STEP_FAIL)))
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldIgnoreException() {
        runner = new TafScenarioRunner(ScenarioExceptionHandler.IGNORE, ScenarioExceptionHandler.IGNORE, Collections.EMPTY_LIST);

        TestScenario scenario = scenario()
                .addFlow(singleStepFlow("fail", annotatedMethod(this, STEP_FAIL)))
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldWorkWithGlobalDataSource() {
        TestStepFlow flow = flow("Global data source test")
                .addTestStep(annotatedMethod(this, STEP_OK))
                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .withDefaultVusers(1)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(2));
        assertThat(integerParam, equalTo(2));
        assertThat(stringParam, equalTo("B"));
    }

    @Test
    public void shouldWorkWithTestStepsReturningValues() {
        TestStepFlow flow = flow("Producer/Consumer")
                .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                .addTestStep(annotatedMethod(this, STEP_CONSUMER))
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .withDefaultVusers(5)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldPAssAttributesBetweenFlows() {
        TestStepFlow producer = flow("Producer")
                .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                .build();

        TestStepFlow consumer = flow("Consumer")
                .addTestStep(annotatedMethod(this, STEP_CONSUMER))
                .build();

        TestScenario scenario = scenario()
                .addFlow(producer)
                .addFlow(consumer)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldCleanAttributesBetweenRuns() {
        {
            TestStepFlow flow = flow("Producer/Consumer")
                    .addTestStep(annotatedMethod(this, STEP_VALUE_PRODUCER))
                    .addTestStep(annotatedMethod(this, STEP_CONSUMER))
                    .build();

            TestScenario scenario = scenario()
                    .addFlow(flow)
                    .build();

            runner.start(scenario);
        }

        {
            TestStepFlow flow = flow("Producer/Consumer")
                    .addTestStep(annotatedMethod(this, STEP_NO_VALUE))
                    .build();

            TestScenario scenario = scenario()
                    .addFlow(flow)
                    .build();

            runner.start(scenario);
        }
    }

    @Test
    public void shouldResetDataSourceBetweenScenarios() {
        TestScenario scenario = scenario()
                .addFlow(flow("Flow")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                .build();

        runner.start(scenario);
        assertThat(count.get(), equalTo(2));
        count.set(0);

        runner.start(scenario);
        assertThat(count.get(), equalTo(2));
    }

    @Test
    public void shouldResetDataSourceBetweenFlows() {
        TestScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                .addFlow(flow("Flow 2")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                .build();

        runner.start(scenario);
        assertThat(count.get(), equalTo(4));
        count.set(0);

        runner.start(scenario);
        assertThat(count.get(), equalTo(4));
    }

    @Test
    public void shouldResetDataSourceBetweenFlows_csv() {
        TestScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(CSV_DATA_SOURCE)))
                .addFlow(flow("Flow 2")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(CSV_DATA_SOURCE)))
                .build();

        runner.start(scenario);
        assertThat(count.get(), equalTo(6));
        count.set(0);

        runner.start(scenario);
        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void shouldSupportSubFlows_ContextDataSource() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, "outer"))
                        .addSubFlow(flow("B")
                                .withDataSources(dataSource("session"))
                                .addTestStep(annotatedMethod(this, "inner"))
                                .build()
                        )
                        .addTestStep(annotatedMethod(this, "end"))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains("1", "A", "B", "C", "1", "2", "A", "B", "C", "2"));
    }

    @Test
    public void shouldSupportSubFlows_ScenarioDataSource() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, "outer"))
                        .addSubFlow(flow("B")
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                                .addTestStep(annotatedMethod(this, "inner"))
                                .build()
                        )
                        .addTestStep(annotatedMethod(this, "end"))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains("1", null, null, "1", "2", null, null, "2"));
    }

    @Test
    public void shouldSupportSubFlows_DataRecordsFromParentSubFlow() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                                .build()
                        )
                ).build();

        runner.start(scenario);
        assertThat(stack, contains("1", "A", "1", "A", "2", "B", "2", "B"));
    }

    @Test
    public void shouldSupportSubFlows_DataRecordsFromParentSubFlowOfSubFlow() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                                .addSubFlow(flow("C")
                                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                                        .build())
                                .build()
                        )
                ).build();

        runner.start(scenario);
        assertThat(stack, contains("1", "A", "1", "A", "1", "A",
                "2", "B", "2", "B", "2", "B"));
    }

    @Test
    public void shouldSupportSubFlows_DataRecordsCombinedFromParentSubFlowAndDataSource() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                .withDataSources(dataSource(CSV_DATA_SOURCE))
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_AND_GLOBAL_DS))
                                .build()
                        )
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "1", "A",
                "attr1", "1", "A",
                "attr2", "1", "A",
                "attr3", "1", "A",
                "2", "B",
                "attr1", "2", "B",
                "attr2", "2", "B",
                "attr3", "2", "B"));
    }


    @Test
    public void checkMultipleDataRecordsOnTestStep() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();

        context.dataSource("apples").addRecord().setField("kind", "red").setField("size", 3).setField("fresh", true);
        context.dataSource("oranges").addRecord().setField("kind", "tasty").setField("diameter", 2).setField("fresh", false);

        TestScenario scenario = scenario()
                .addFlow(flow("test")
                        .withDataSources(
                                dataSource("apples"),
                                dataSource("oranges")
                        )
                        .addTestStep(annotatedMethod(this, "multirecord"))
                ).build();

        runner.start(scenario);
    }

    @Test
    public void shouldRunWithSharedDataSources() {
        final String SHARED_DS = "sharedDs";

        TestDataSource<DataRecord> csvDataSource = fromCsv("data/node.csv");

        TestDataSource fmNodes = TafDataSources.shared(csvDataSource);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(SHARED_DS, fmNodes);

        TestStepFlow fmFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(SHARED_DS)
                )
                .build();


        TestScenario scenario = scenario()
                .addFlow(fmFlow)
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void shouldRunWithCyclicDataSources() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/node.csv");

        TestDataSource fmNodes = TafDataSources.cyclic(csvDataSource);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(CYCLIC_DATA_SOURCE, fmNodes);

        TestStepFlow fmFlow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_CYCLIC_DATASOURCE))
                .withDataSources(
                        dataSource(CYCLIC_DATA_SOURCE)
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(fmFlow)
                .build();

        runner.start(scenario);

        assertThat(stack, contains("attr1", "attr2", "attr3",
                "attr1", "attr2", "attr3",
                "attr1", "attr2"));
    }

    @Test
    public void shouldRunWithCyclicSharedDataSources_CIS_7605() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/node.csv");

        TestDataSource fmNodes = TafDataSources.cyclic(csvDataSource);
        fmNodes = TafDataSources.shared(fmNodes);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(CYCLIC_DATA_SOURCE, fmNodes);

        TestStepFlow fmFlow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_CYCLIC_DATASOURCE))
                .withDataSources(
                        dataSource(CYCLIC_DATA_SOURCE)
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(fmFlow)
                .build();

        runner.start(scenario);

        assertThat(stack, contains("attr1", "attr2", "attr3",
                "attr1", "attr2", "attr3",
                "attr1", "attr2"));
    }

    @Test
    public void shouldRunWithCyclicSharedDataSources_CIS_7605_2() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/node.csv");

        TestDataSource fmNodes = TafDataSources.cyclic(csvDataSource);
        fmNodes = TafDataSources.shared(fmNodes);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(CYCLIC_DATA_SOURCE, fmNodes);

        TestStepFlow fmFlow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_AND_GLOBAL_DS))
                .withDataSources(
                        dataSource(CYCLIC_DATA_SOURCE),
                        dataSource(GLOBAL_DATA_SOURCE)
                )
                .withVusers(3)
                .build();

        TestScenario scenario = scenario()
                .addFlow(fmFlow)
                .build();

        runner.start(scenario);

        assertThat(stack, hasSize(18)); // 3 arguments (attribute, integerParam, stringParam) * 2 datarecords from GLOBAL_DATA_SOURCE * 3 Vusers
        assertThat(stack, hasItem("attr1")); // we can not guarantee that attr2 and attr3 will be given to vUser
    }

    @Test
    public void shouldRunWithFilteredSharedDataSources() {
        final String FM_NODES = "fmNodes";
        final String PM_NODES = "pmNodes";

        TestDataSource<DataRecord> csvDataSource = fromCsv("data/node.csv");

        TestDataSource fmNodes = TafDataSources.shared(TafDataSources.filter(csvDataSource, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord dataRecord) {
                Object nodeId = dataRecord.getFieldValue("nodeId");
                return "1".equals(nodeId) || "2".equals(nodeId);
            }
        }));

        TestDataSource pmNodes = TafDataSources.shared(TafDataSources.filter(csvDataSource, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord dataRecord) {
                Object nodeId = dataRecord.getFieldValue("nodeId");
                return "3".equals(nodeId) || "2".equals(nodeId);
            }

        }));

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(FM_NODES, fmNodes);
        context.addDataSource(PM_NODES, pmNodes);

        TestStepFlow fmFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(FM_NODES)
                )
                .build();

        TestStepFlow pmFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(PM_NODES)
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(fmFlow)
                .addFlow(pmFlow)
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(4));
    }

    @Test
    public void shouldDoParralelExecution() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow("parallel1").addTestStep(runnable(counter)),
                        flow("parallel2").addTestStep(runnable(counter)),
                        flow("parallel3").addTestStep(runnable(counter))
                )
                .addFlow(
                        flow("sequential2").addTestStep(runnable(counter))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(2 * 2 + 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfTestStepIdDoesntExist(){
        TestScenario scenario = scenario().addFlow(flow("").addTestStep(annotatedMethod(this, "nonExistentTestSte"))).build();
        TestScenarios.runner().build().start(scenario);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfNoInputAnnotationIsSpecified() {
        scenario()
                .addFlow(flow("A")
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                                .addTestStep(annotatedMethod(this, STEP_WITHOUT_INPUT_ANNOTATION))
                ).build();
    }

    @Test
    public void shouldPreserveContext() {
        int vUsers = 5;
        TestScenario scenario = scenario("test attributes")
                .addFlow(
                        flow("A")
                                .addTestStep(putVUserToContext())
                                .addTestStep(assertVUserInContext())
                )
                .withDefaultVusers(vUsers)
                .build();

        runner.start(scenario);

        for (int i = 1; i <= vUsers; i++) {
            String vUser = String.valueOf(i);
            assertThat(Collections.frequency(stack, vUser), equalTo(1));
        }
    }

    @Test
    public void shouldPreserveContextBetweenSubflows() {
        int vUsers = 5;
        TestScenario scenario = scenario("test attributes")
                .addFlow(
                        flow("A")
                                .addTestStep(putVUserToContext())
                                .addSubFlow(flow("subflow1")
                                        .addTestStep(assertVUserInContext())
                                        .addSubFlow(
                                                flow("subsubflow1")
                                                        .addTestStep(assertVUserInContext())))
                )
                .withDefaultVusers(vUsers)
                .build();

        runner.start(scenario);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfSameFlowAddedTwice() {
        TestStepFlow flow = new TestStepFlowImpl("flow",
                null, null, null, null, null, null);
        scenario()
                .addFlow(flow)
                .addFlow(flow)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfSameFlowAddedTwiceInHolder() {
        TestStepFlow flow = new TestStepFlowImpl("flow",
                null, null, null, null, null, null);
        scenario()
                .addFlow(flow)
                .addFlow(flow)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfSameTestStepAddedTwice() {
        TestStepDefinition testStep = runnable(counter);
        scenario()
                .addFlow(flow("A")
                        .addTestStep(testStep)
                        .addTestStep(testStep)
                )
                .build();
    }

    @TestStep(id = "multirecord")
    public void multipleRecords(@Input("apples") Apple apple, @Input("oranges") Orange orange) {
        assertThat(apple.getKind(), equalTo("red"));
        assertThat(apple.getSize(), equalTo(3));

        assertThat(orange.getKind(), equalTo("tasty"));
        assertThat(orange.getDiameter(), equalTo(2));

        assertTrue(apple.isFresh());
        assertTrue(apple.getFresh());
        assertFalse(orange.isFresh());
        assertFalse(orange.getFresh());
    }

    @TestStep(id = "outer")
    public void outer(@Input("integer") Integer i) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.setAttribute("vuser", context.getVUser());
        context.setAttribute("integer", i);

        if (!context.doesDataSourceExist("session")) {
            TestDataSource<DataRecord> dataSource = context.dataSource("session");
            dataSource.addRecord().setField("user", "A");
            dataSource.addRecord().setField("user", "B");
            dataSource.addRecord().setField("user", "C");
        }

        stack.push(i.toString());
    }

    @TestStep(id = "inner")
    public void inner(@Input("user") String user) {
        stack.push(user);
    }

    @TestStep(id = "end")
    public void inner(@Input("integer") Integer i) {
        stack.push(i.toString());
    }

    @TestStep(id = STEP_PUSH_GLOBAL_DS)
    public void pushFromGlobalDS(@Input("string") String stringParam, @Input("integer") Integer integerParam) {
        stack.push(String.valueOf(integerParam));
        stack.push(stringParam);
    }

    @TestStep(id = STEP_PUSH_CSV_AND_GLOBAL_DS)
    public void pushFromCsvAndGlobalDS(@Input("attribute") String attribute, @Input("integer") Integer integerParam, @Input("string") String stringParam) {
        stack.push(attribute);
        stack.push(String.valueOf(integerParam));
        stack.push(stringParam);
    }

    private TestStepFlow singleStepFlow(String name, TestStepDefinition testStep) {
        return flow(name).addTestStep(testStep).withDataSources(iterable("x", prepareList(1))).build();
    }

    @TestStep(id = STEP_CONSUMER)
    public void consumeValue() {
        int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
        TestStepResult attribute = ServiceRegistry.getTestContextProvider().get().getAttribute(STEP_VALUE_PRODUCER);
        assertThat(attribute.getResult().get().toString(), equalTo("hello from " + vUser));
    }

    @TestStep(id = STEP_NO_VALUE)
    public void expectNoValue() {
        Object attribute = ServiceRegistry.getTestContextProvider().get().getAttribute(STEP_VALUE_PRODUCER);
        assertThat(attribute, nullValue());
    }

    @TestStep(id = STEP_OK)
    public void parametrizedTestStep(@Input("integer") Integer integerParam, @Input("string") String stringParam) {
        this.integerParam = integerParam;
        this.stringParam = stringParam;
        count.incrementAndGet();
    }

    @TestStep(id = STEP_FAIL)
    public void failure() {
        throw new NullPointerException();
    }

    @TestStep(id = STEP_CHECK_DATASOURCE)
    public void checkDatasource(@Input("attribute") String attribute, @Input("value") Integer value) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        AtomicInteger attr = context.getAttribute(attribute);
        attr.incrementAndGet();
        count.incrementAndGet();
    }

    @TestStep(id = STEP_WITHOUT_INPUT_ANNOTATION)
    public void stepWithoutInputAnnotation(Integer integerParam, @Input("string") String stringParam) {
        stack.push(String.valueOf(integerParam));
    }

    @TestStep(id = STEP_CYCLIC_DATASOURCE)
    public void stepCyclicDatasource(@Input("attribute") String attribute) {
        stack.push(String.valueOf(attribute));
        count.incrementAndGet();
        if (count.intValue() == 8) {
            DataSourceControl.stopExecution(CYCLIC_DATA_SOURCE);
        }
    }

    public static interface Apple extends DataRecord {
        String getKind();
        Integer getSize();
        boolean isFresh();
        boolean getFresh();
    }

    public static interface Orange extends DataRecord {
        String getKind();
        Integer getDiameter();
        boolean isFresh();
        boolean getFresh();
    }

}
