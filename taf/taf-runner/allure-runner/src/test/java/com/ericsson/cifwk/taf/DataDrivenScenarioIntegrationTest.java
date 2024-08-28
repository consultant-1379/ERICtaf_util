package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.ext.exporter.SvgExporter;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioDataSourceContext;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import org.junit.After;
import org.junit.Before;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.AllureTestUtils.VerySpecialException;
import static com.ericsson.cifwk.taf.AllureTestUtils.getAttachmentsByType;
import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCaseNames;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestStepNames;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static com.ericsson.cifwk.taf.ReadableIsIterableContainingInAnyOrder.containsInAnyOrder;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_ENABLED;
import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Mihails Volkovs (mihails.volkovs@ericsson.com)
 *         11/08/2015
 */

public class DataDrivenScenarioIntegrationTest {

    public static final String ROOT_SUITE = "Data Driven Suite";
    public static final String TEST_SUITE_1 = "Simple data driven scenario";
    public static final String TEST_SUITE_2 = "Parallel/subflow data driven scenario";
    public static final String TEST_SUITE_3 = "scenario with unhandled exceptions";
    public static final String TEST_SUITE_4 = "scenario with subflow unhandled exceptions";
    public static final String TEST_SUITE_5 = "scenario with parallel unhandled exceptions";
    public static final String TEST_SUITE_6 = "handled exceptions in subflow";
    public static final String TEST_SUITE_7 = "assertions";
    public static final String TEST_SUITE_8 = "data driven graph attachemnts";
    public static final String TEST_SUITE_9 = "allure report generated when incorrect datasource used";
    public static final String TEST_SUITE_10 = "allure report generated when DataSourceDefinition.provideIterator throws exception";
    public static final String TEST_SUITE_11 = "data driven scenario with missing testCaseId from data records";
    public static final String TEST_SUITE_12 = "data driven scenario with empty datasource on flow";

    public static final String TEST_STEP_1 = "ts1";
    public static final String TEST_STEP_2 = "ts2";
    public static final String TEST_STEP_3 = "ts3";
    public static final String TEST_STEP_4 = "ts4";
    public static final String TEST_STEP_5 = "ts5";
    public static final String TEST_STEP_THROW_EXCEPTION = "throwExceptionForTestCase1TestStep";
    public static final String TEST_STEP_ASSERTION_FAILED = "testStepAssertionFailed";

    public static final String TEST_STEP_TITLE_1A = TEST_STEP_1 + ": testStep1[A]";
    public static final String TEST_STEP_TITLE_1B = TEST_STEP_1 + ": testStep1[B]";
    public static final String TEST_STEP_TITLE_2 = TEST_STEP_2 + ": testStep2";
    public static final String TEST_STEP_TITLE_3 = TEST_STEP_3 + ": testStep3";
    public static final String TEST_STEP_TITLE_4 = TEST_STEP_4 + ": testStep4";
    public static final String TEST_STEP_FAILED_TITLE = TEST_STEP_THROW_EXCEPTION + ": throwException";
    public static final String ASSERTION_FAILED_TITLE = TEST_STEP_ASSERTION_FAILED + ": failAssert";

    public static final String TEST_CASE_1 = "testCase1";
    public static final String TEST_CASE_2 = "testCase2";
    public static final String TEST_CASE_NOT_STARTED = "TestId not found as test case didn't start successfully";
    public static final String MISSING_TEST_ID_MESSAGE = "Missing testCaseId field";
    public static final String TEST_CASE_ID_NOT_FOUND_MESSAGE = "Field 'testCaseId' could not be found in Data Record\n" +
            "You must provide 'testCaseId' field when using Data Driven Scenarios";

    public static final String PARALLEL_THREADS = "parallelThreads";

    private static final String GLOBAL_DATA_SOURCE = "globalDataSource";
    private static final String INCORRECT_GLOBAL_DATA_SOURCE = "incorrectdatasource";
    private static final String NO_TEST_CASE_ID_DATA_SOURCE = "noTestCaseIdDataSource";
    private static final String EMPTY_DATA_SOURCE = "emptyDataSource";

    final static Stack<String> stack = new Stack<>();

    @org.junit.Test
    public void allureReportSequential() throws Exception {
        runTestNg(ROOT_SUITE, DataDrivenTest.class);
        testXmlReports();
    }

    @org.junit.Test
    public void allureReportParallel() throws Exception {
        System.setProperty(PARALLEL_THREADS, "2");

        TafDataSources.shareDataSource(GLOBAL_DATA_SOURCE).run();
        runTestNg(ROOT_SUITE, DataDrivenTest.class);
        testXmlReports();
    }

    @org.junit.Test
    public void allureReportParallelMoreVUsers() throws Exception {
        System.setProperty(PARALLEL_THREADS, "10");

        TafDataSources.shareDataSource(GLOBAL_DATA_SOURCE).run();
        runTestNg(ROOT_SUITE, DataDrivenTest.class);
        testXmlReports();
    }

    @org.junit.Test
    public void allureReportMissingTestCaseId() throws Exception {
        runTestNg(ROOT_SUITE, DataDrivenTest.class);
        testNoTestCaseIdXmlReport();
    }

    @org.junit.Test
    public void allureReportEmptyDataSourceOnFlow(){
        runTestNg(ROOT_SUITE, DataDrivenTest.class);
        testEmptyDataSourceXmlReport();
    }

    @Before
    public void prepareGlobalDataSource() {
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

        prepareNoTestCaseIdDataSource();
        prepareEmptyDataSource();
    }

    private void prepareEmptyDataSource() {
        TestContext context = TafTestContext.getContext();
        context.removeDataSource(EMPTY_DATA_SOURCE);
        context.dataSource(EMPTY_DATA_SOURCE);
    }

    private void prepareNoTestCaseIdDataSource() {
        TestContext context = TafTestContext.getContext();

        context.removeDataSource(NO_TEST_CASE_ID_DATA_SOURCE);
        stack.clear();
        TestDataSource<DataRecord> globalDataSource = context.dataSource(NO_TEST_CASE_ID_DATA_SOURCE);
        globalDataSource.addRecord()
                .setField("test", 1)
                .setField("string", "A");
        globalDataSource.addRecord()
                .setField("test", 2)
                .setField("string", "B");
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(PARALLEL_THREADS);
        System.clearProperty(TAF_SCENARIO_DEBUG_ENABLED);
    }

    private void testXmlReports() throws Exception {

        TestSuiteResult testSuite1 = getLatestTestSuite(TEST_SUITE_1);
        assertThat(getTestCaseNames(testSuite1), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite1, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_TITLE_2));
        assertThat(getTestStepNames(testSuite1, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, TEST_STEP_TITLE_2));

        TestSuiteResult testSuite2 = getLatestTestSuite(TEST_SUITE_2);
        assertThat(getTestCaseNames(testSuite2), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite2, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_TITLE_2, TEST_STEP_TITLE_3, TEST_STEP_TITLE_4));
        assertThat(getTestStepNames(testSuite2, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, TEST_STEP_TITLE_2, TEST_STEP_TITLE_3, TEST_STEP_TITLE_4));

        TestSuiteResult testSuite3 = getLatestTestSuite(TEST_SUITE_3);
        assertThat(getTestCaseNames(testSuite3), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite3, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_FAILED_TITLE, TEST_STEP_TITLE_3));
        assertThat(getTestStepNames(testSuite3, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, TEST_STEP_FAILED_TITLE, TEST_STEP_TITLE_3));
        assertThat(testSuite3.getTestCases().get(0).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite3.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite3.getTestCases().get(1).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite3.getTestCases().get(1).getStatus(), equalTo(Status.BROKEN));

        TestSuiteResult testSuite4 = getLatestTestSuite(TEST_SUITE_4);
        assertThat(getTestCaseNames(testSuite4), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite4, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_FAILED_TITLE));
        assertThat(testSuite4.getTestCases().get(0).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite4.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
        assertThat(getTestStepNames(testSuite4, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, TEST_STEP_FAILED_TITLE));
        assertThat(testSuite4.getTestCases().get(1).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite4.getTestCases().get(1).getStatus(), equalTo(Status.BROKEN));

        TestSuiteResult testSuite5 = getLatestTestSuite(TEST_SUITE_5);
        assertThat(getTestCaseNames(testSuite5), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite5, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_FAILED_TITLE, TEST_STEP_TITLE_3));
        assertThat(testSuite5.getTestCases().get(0).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite5.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
        assertThat(getTestStepNames(testSuite5, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, TEST_STEP_FAILED_TITLE, TEST_STEP_TITLE_3));
        assertThat(testSuite5.getTestCases().get(1).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite5.getTestCases().get(1).getStatus(), equalTo(Status.BROKEN));

        TestSuiteResult testSuite6 = getLatestTestSuite(TEST_SUITE_6);
        assertThat(getTestCaseNames(testSuite6), containsInAnyOrder(TEST_CASE_1));
        assertThat(getTestStepNames(testSuite6, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, TEST_STEP_FAILED_TITLE, TEST_STEP_TITLE_2, TEST_STEP_TITLE_3));
        assertThat(testSuite6.getTestCases().get(0).getSteps().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite6.getTestCases().get(0).getStatus(), equalTo(Status.PASSED));

        TestSuiteResult testSuite7 = getLatestTestSuite(TEST_SUITE_7);
        assertThat(getTestCaseNames(testSuite7), containsInAnyOrder(TEST_CASE_1, TEST_CASE_2));
        assertThat(getTestStepNames(testSuite7, TEST_CASE_1), containsInAnyOrder(TEST_STEP_TITLE_1A, ASSERTION_FAILED_TITLE));
        assertThat(testSuite7.getTestCases().get(0).getSteps().get(1).getStatus(), equalTo(Status.FAILED));
        assertThat(testSuite7.getTestCases().get(0).getStatus(), equalTo(Status.FAILED));
        assertThat(getTestStepNames(testSuite7, TEST_CASE_2), containsInAnyOrder(TEST_STEP_TITLE_1B, ASSERTION_FAILED_TITLE));
        assertThat(testSuite7.getTestCases().get(1).getSteps().get(1).getStatus(), equalTo(Status.FAILED));
        assertThat(testSuite7.getTestCases().get(1).getStatus(), equalTo(Status.FAILED));

        TestSuiteResult testSuite8 = getLatestTestSuite(TEST_SUITE_8);
        TestCaseResult testCase = getLast(testSuite8.getTestCases());
        List<Attachment> attachments = getAttachmentsByType(testCase, SvgExporter.SVG_MIME);
        assertThat(attachments.get(0).getTitle(), containsString("testAttachments"));

        TestSuiteResult rootSuite = getLatestTestSuite(ROOT_SUITE);
        assertThat(getTestCaseNames(rootSuite), hasItems(TEST_CASE_1, TEST_CASE_2));

        TestSuiteResult testSuite9 = getLatestTestSuite(TEST_SUITE_9);
        assertThat(getTestCaseNames(testSuite9), containsInAnyOrder(TEST_CASE_NOT_STARTED));

        TestSuiteResult testSuite10 = getLatestTestSuite(TEST_SUITE_10);
        assertThat(getTestCaseNames(testSuite10), containsInAnyOrder(TEST_CASE_NOT_STARTED));
        assertThat(testSuite10.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
    }

    private void testNoTestCaseIdXmlReport() throws Exception {
        TestSuiteResult testSuite11 = getLatestTestSuite(TEST_SUITE_11);
        assertThat(testSuite11.getTestCases().get(0).getName(), equalTo(MISSING_TEST_ID_MESSAGE));
        assertThat(testSuite11.getTestCases().get(0).getFailure().getMessage(), containsString(TEST_CASE_ID_NOT_FOUND_MESSAGE));
        assertThat(testSuite11.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuite11.getTestCases().get(1).getName(), equalTo(MISSING_TEST_ID_MESSAGE));
        assertThat(testSuite11.getTestCases().get(1).getFailure().getMessage(), containsString(TEST_CASE_ID_NOT_FOUND_MESSAGE));
        assertThat(testSuite11.getTestCases().get(1).getStatus(), equalTo(Status.BROKEN));
        assertThat(stack, hasSize(2));
    }

    private void testEmptyDataSourceXmlReport() {
        TestSuiteResult testSuiteResult12 = getLatestTestSuite(TEST_SUITE_12);
        assertThat(testSuiteResult12.getTestCases().get(0).getStatus(), equalTo(Status.BROKEN));
        assertThat(testSuiteResult12.getTestCases().get(1).getStatus(), equalTo(Status.BROKEN));
    }
    public static class DataDrivenTest {
        private Integer parallelThreads =
                TafConfigurationProvider.provide().getProperty(PARALLEL_THREADS, 1, Integer.class);

        @Test
        @TestSuite(TEST_SUITE_1)
        public void simpleScenario() {

            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_2)
        public void subflowAndParallel() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addSubFlow(
                                            flow("subFlow")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_1)))
                            .split(
                                            flow("parallel")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_2)),
                                            flow("parallel")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_3)))

                            .addTestStep(annotatedMethod(this, TEST_STEP_4))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_3)
        public void unhandledExceptionInStep() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_THROW_EXCEPTION))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                            .addTestStep(annotatedMethod(this, TEST_STEP_3).alwaysRun())

                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_4)
        public void unhandledExceptionInSuflow() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addSubFlow(
                                            flow("subFlow")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_1))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_THROW_EXCEPTION))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_2)))
                            .addTestStep(annotatedMethod(this, TEST_STEP_3))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_5)
        public void unhandledExceptionInParallel() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .split(
                                            flow("parallel")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_1))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_THROW_EXCEPTION))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_2)),
                                            flow("parallel")
                                                    .pause(1000, TimeUnit.MILLISECONDS)
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_3)))
                            .addTestStep(annotatedMethod(this, TEST_STEP_4))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_6)
        public void handledExceptionInSubflow() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addSubFlow(
                                            flow("subFlow")
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_1))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_THROW_EXCEPTION))
                                                    .addTestStep(annotatedMethod(this, TEST_STEP_2)))
                            .addTestStep(annotatedMethod(this, TEST_STEP_3))
                    )
                    .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE).withFilter(TEST_CASE_ID + " == '" + TEST_CASE_1 + "'"))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_7)
        public void assertionInStep() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_ASSERTION_FAILED))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_8)
        public void graphAttachments() {
            System.setProperty(TAF_SCENARIO_DEBUG_ENABLED, "true");
            TestScenario scenario = dataDrivenScenario("testAttachments")
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE).withFilter(TEST_CASE_ID + " == '" + TEST_CASE_1 + "'"))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite
        public void shouldAddTestCasesToRootSuiteIfSuiteNameIsNotSet() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .doParallel(parallelThreads)
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_9)
        public void incorrectDatasource() {
            TestScenario scenario = dataDrivenScenario("testIncorrectDatasource")
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(INCORRECT_GLOBAL_DATA_SOURCE))
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_10)
        public void provideIteratorTrowsException() {
            TestScenario scenario = dataDrivenScenario("testIncorrectDatasource")
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(new DataSourceDefinitionBuilder("throwing") {
                        @Override
                        public DataSourceDefinition build() {
                            return new ThrowingDataSourceDefinition();
                        }
                    })
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_11)
        public void shouldAddTestCaseWithNullPointerWhenTestCaseIdIsMissing() {
            TestScenario scenario = dataDrivenScenario("testMissingTestCaseId")
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_5))
                    )
                    .withScenarioDataSources(dataSource(NO_TEST_CASE_ID_DATA_SOURCE))
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_12)
        public void emptyDataSourceTest() {
            TestScenario scenario = dataDrivenScenario("My Scenario")
                    .addFlow(myFlow()).withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE)).build();
            runner().withListener(new LoggingScenarioListener()).build().start(scenario);
        }

        private TestStepFlow myFlow() {
            return flow("my Flow")
                    .addTestStep(annotatedMethod(this, TEST_STEP_1))
                    .withDataSources(dataSource(EMPTY_DATA_SOURCE))
                    .build();
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

        @TestStep(id = TEST_STEP_5)
        public void testStep5() {
            stack.push("Single count");
        }

        @TestStep(id = TEST_STEP_THROW_EXCEPTION)
        public void throwException() {
            throw new VerySpecialException();
        }

        @TestStep(id = TEST_STEP_ASSERTION_FAILED)
        public void failAssert() {
            assertThat("using_aspects", equalTo("good_idea"));
        }
    }

    public static class ThrowingDataSourceDefinition implements DataSourceDefinition<DataRecord> {

        @Override
        public Supplier<Optional<DataRecord>> provideSupplier(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources) {
            throw new InvalidDataSourceException("provideIterator method threw exception");
        }

        @Override
        public String getName() {
            return "throwing";
        }

        @Override
        public boolean allowsEmpty() {
            return false;
        }
    }
}
