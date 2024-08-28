package com.ericsson.cifwk.taf.eiffel;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.testng.CompositeTestNGListener;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.ericsson.duraci.eiffelmessage.mmparser.clitool.EiffelConfig;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import com.ericsson.duraci.eiffelmessage.sending.TestMessageSenderFactory;
import com.ericsson.duraci.logging.JavaLoggerEiffelLog;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_HTTP_CONFIG_URL;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class EiffelAdapterITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EiffelAdapterITest.class);

    static final String MB_HOST = "MB_HOST";
    static final String MB_DOMAIN = "MB_DOMAIN";
    static final String MB_EXCHANGE = "MB_EXCHANGE";
    static final String PARENT_EVENT_ID = "PARENT_EVENT_ID";
    static final String PARENT_EXECUTION_ID = "PARENT_EXECUTION_ID";
    static final String TEST_EXECUTION_ID = "TEST_EXECUTION_ID";

    static final String TEST_ID_UNDEFINED = "UNDEFINED";
    static final String FQN = "com.ericsson.cifwk.taf.eiffel.EiffelAdapterITest$SampleTest:contextTest";

    private TestMessageSenderFactory senderFactory;

    public static final String TEST_STEP_1 = "ts1";
    public static final String TEST_STEP_2 = "ts2";
    public static final String TEST_STEP_ASSERTION_FAILED = "testStepAssertionFailed";

    public static final String TEST_SUITE_1 = "Simple data driven scenario";
    public static final String TEST_SUITE_2 = "assertions";


    public static final String TEST_CASE_1 = "testCase1";
    public static final String TEST_CASE_2 = "testCase2";

    private static final String GLOBAL_DATA_SOURCE = "globalDataSource";

    private static final Lock lock = new ReentrantLock(true);

    @Before
    public void setUp() {
        lock.lock();
        EiffelConfig config = new EiffelConfig(MB_DOMAIN, MB_EXCHANGE, MB_HOST);
        JavaLoggerEiffelLog log = new JavaLoggerEiffelLog(MessageSender.class);
        this.senderFactory = new TestMessageSenderFactory(log, config);
        EiffelAdapter.senderForTests = senderFactory.create();
    }

    private void prepareGlobalDataSource() {
        TestContext context = TafTestContext.getContext();

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

    @org.junit.Test
    public void shouldSendEventIntoCorrectMB() throws Exception {
        Predicate<Void> asserts = new Predicate<Void>() {
            public boolean apply(Void voidd) {
                //
                String mbHost = (String) DataHandler.getAttribute(EiffelAdapter.MB_HOST);
                String mbExchange = (String) DataHandler.getAttribute(EiffelAdapter.MB_EXCHANGE);
                String mbDomain = (String) DataHandler.getAttribute(EiffelAdapter.MB_DOMAIN);
                String parentEventId = (String) DataHandler.getAttribute(EiffelAdapter.PARENT_EVENT_ID);
                String parentExecutionId = (String) DataHandler.getAttribute(EiffelAdapter.PARENT_EXECUTION_ID);
                //
                assertThat(mbHost, equalTo(MB_HOST));
                assertThat(mbDomain, equalTo(MB_DOMAIN));
                assertThat(mbExchange, equalTo(MB_EXCHANGE));
                assertThat(parentEventId, equalTo(PARENT_EVENT_ID));
                assertThat(parentExecutionId, equalTo(PARENT_EXECUTION_ID));
                //
                runTestClass(SampleTest.class);

                int numberOfEvents = 8; // 2 suite + 6 test
                List<EiffelMessage> messages = senderFactory.getMessagesSent();
                assertThat(messages.size(), is(numberOfEvents));

                EiffelMessage testSuiteStarted = messages.get(0);

                final ExecutionId testExecutionId = new ExecutionId(TEST_EXECUTION_ID);

                verifySuiteStarted(testSuiteStarted, testExecutionId);
                //
                EiffelMessage testCaseStarted = messages.get(1);
                verifyTestCaseStarted(testExecutionId, testCaseStarted, TEST_ID_UNDEFINED);
                //
                EiffelMessage testCaseFinished = messages.get(2);
                EiffelTestCaseFinishedEvent testCaseFinishedEvent = (EiffelTestCaseFinishedEvent) testCaseFinished.getEvent();
                assertThat(testCaseFinishedEvent.getOptionalParameter("testId"), equalTo(TEST_ID_UNDEFINED));
                assertThat(testCaseFinishedEvent.getOptionalParameter("testMethod"), equalTo(FQN));
                assertThat(testCaseFinishedEvent.getTestExecutionId(), equalTo(testExecutionId));
                //
                EiffelMessage testSuiteFinished = messages.get(7);
                verifySuiteFinished(testExecutionId, testSuiteStarted, testSuiteFinished);
                return false;
            }
        };
        inJetty(asserts);
    }

    @org.junit.Test
    public void shouldSendEventsFromDataDrivenScenario() throws Exception {
        Predicate<Void> asserts = new Predicate<Void>() {
            public boolean apply(Void voidd) {
                runTestClass(DataDrivenTest.class);

                List<EiffelMessage> messages = senderFactory.getMessagesSent();
                verifyScenarioSuite(messages.subList(1, 7), asList("testCase1", "testCase2"), ResultCode.FAILURE);
                verifyScenarioSuite(messages.subList(7, 13), asList("testCase1", "testCase2"), ResultCode.SUCCESS);

                return false;
            }
        };
        inJetty(asserts);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty(TAF_HTTP_CONFIG_URL);
        CompositeTestNGListener.cleanUpTestNgListeners();
        lock.unlock();
    }


    private void verifyScenarioSuite(List<EiffelMessage> messages, List<String> testIds, ResultCode resultCode) {
        assertThat(messages.size(), Matchers.greaterThanOrEqualTo(2));

        EiffelMessage testSuiteStarted = messages.get(0);

        final ExecutionId testExecutionId = new ExecutionId(TEST_EXECUTION_ID);

        verifySuiteStarted(testSuiteStarted, testExecutionId);
        verifyTestCaseEvents(messages.subList(1, messages.size() - 1), testIds, resultCode, testExecutionId);

        //
        EiffelMessage testSuiteFinished = messages.get(messages.size() - 1);
        verifySuiteFinished(testExecutionId, testSuiteStarted, testSuiteFinished);
    }

    private void verifyTestCaseEvents(List<EiffelMessage> messages, List<String> testIds, ResultCode resultCode, ExecutionId testExecutionId) {
        assertThat("Message count do not match with expected test case count", testIds.size() * 2, is(messages.size()));

        Iterator<String> testIdIterator = testIds.iterator();
        for (Iterator<EiffelMessage> messageIterator = messages.iterator(); messageIterator.hasNext(); ) {
            //
            EiffelMessage testCaseStarted = messageIterator.next();
            verifyTestCaseStarted(testExecutionId, testCaseStarted, testIdIterator.next());
            //
            EiffelMessage testCaseFinished = messageIterator.next();
            EiffelTestCaseFinishedEvent testCaseFinishedEvent = (EiffelTestCaseFinishedEvent) testCaseFinished.getEvent();
            assertThat(testCaseFinishedEvent.getTestExecutionId(), equalTo(testExecutionId));
            EiffelTestCaseStartedEvent testCaseStartedEvent = (EiffelTestCaseStartedEvent) testCaseStarted.getEvent();
            assertThat(testCaseFinishedEvent.getTestCaseExecutionId(), is(testCaseStartedEvent.getTestCaseExecutionId()));
            assertThat(testCaseFinishedEvent.getResultCode(), is(resultCode));
        }
    }

    private void verifyTestCaseStarted(ExecutionId testExecutionId, EiffelMessage testCaseStarted, String testCaseId) {
        EiffelTestCaseStartedEvent testCaseStartedEvent = (EiffelTestCaseStartedEvent) testCaseStarted.getEvent();
        assertThat(testCaseStartedEvent.getTestId(), equalTo(testCaseId));
        assertThat(testCaseStartedEvent.getTestExecutionId(), equalTo(testExecutionId));
    }

    private void verifySuiteFinished(ExecutionId testExecutionId, EiffelMessage testSuiteStarted, EiffelMessage testSuiteFinished) {
        EiffelTestSuiteFinishedEvent testSuiteFinishedEvent = (EiffelTestSuiteFinishedEvent) testSuiteFinished.getEvent();
        assertThat(testSuiteFinishedEvent.getTestExecutionId(), equalTo(testExecutionId));
        EiffelTestSuiteStartedEvent testSuiteStartedEvent = (EiffelTestSuiteStartedEvent) testSuiteStarted.getEvent();
        assertThat(testSuiteStartedEvent.getTestSuiteExecutionId(), equalTo(testSuiteFinishedEvent.getTestSuiteExecutionId()));
    }

    private void verifySuiteStarted(EiffelMessage testSuiteStarted, ExecutionId testExecutionId) {
        EiffelTestSuiteStartedEvent testSuiteStartedEvent = (EiffelTestSuiteStartedEvent) testSuiteStarted.getEvent();

        assertThat(testSuiteStarted.getInputEventIds().get(0), equalTo(new EventId(PARENT_EVENT_ID)));
        assertThat(testSuiteStartedEvent.getParentExecutionId(), equalTo(new ExecutionId(PARENT_EXECUTION_ID)));
        assertThat(testSuiteStartedEvent.getTestExecutionId(), equalTo(testExecutionId));
    }

    private void inJetty(Predicate<Void> predicate) {
        EmbeddedJetty jetty = new EmbeddedJetty();
        try {
            jetty.start();
            String CONFIG_URL = "http://localhost:" + jetty.JETTY_PORT + "/taf_config/";
            LOGGER.debug("EmbeddedJetty.JETTY_PORT set to {} for URL {}", jetty.JETTY_PORT, CONFIG_URL);
            System.setProperty(TAF_HTTP_CONFIG_URL, CONFIG_URL);
            predicate.apply(null);
        } finally {
            jetty.stop();
        }
    }

    private void runTestClass(Class testClass) {
        Suite suite = new Suite().addClass(testClass);
        try {
            prepareGlobalDataSource();
            runTaf(suite.build());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void runTaf(String suite) throws Exception {
        //
        Path tempFile = Files.createTempFile("taf-suite-", ".xml");
        File file = tempFile.toFile();
        file.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(suite.getBytes("UTF-8"));
        }
        runTestNg(file);
    }

    private void runTestNg(File suiteFile) throws IOException {
        TestNG testNG = new TestNG(false);
        List<String> suites = Lists.newArrayList();
        String pathToSuite = suiteFile.getAbsolutePath();
        Preconditions.checkState(suiteFile.exists(), "Suite file %s not found", pathToSuite);
        suites.add(pathToSuite);
        testNG.setTestSuites(suites);
        testNG.run();
    }

    private class Suite {
        List<String> classes = new ArrayList<>();

        Suite addClass(Class clazz) {
            classes.add(clazz.getName());
            return this;
        }

        String build() {
            StringBuilder suite = new StringBuilder()
                    .append("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\" >")
                    .append("<suite name=\"test\">")
                    .append("<test name=\"TafEiffelAdapterITest\">")
                    .append("<classes>");
            for (String clazz : classes) {
                suite.append("<class name=\"").append(clazz).append("\"/>");
            }
            suite.append("</classes>")
                    .append("</test>")
                    .append("</suite>");
            return suite.toString();
        }
    }

    @Test(enabled = false)
    public static class SampleTest {

        @Test
        public void successTest() {
            assertTrue(true);
        }

        @Test
        public void contextTest() {
            assertTrue(true);
        }

        @Test
        public void failingTest() {
            throw new RuntimeException();
        }

    }

    @Test(enabled = false)
    public static class DataDrivenTest {

        @Test
        @TestSuite(TEST_SUITE_1)
        public void simpleScenario() {

            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .build();
            runner().build().start(scenario);
        }

        @Test
        @TestSuite(TEST_SUITE_2)
        public void assertionInStep() {
            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_ASSERTION_FAILED))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                    .build();
            runner().build().start(scenario);
        }

        @TestStep(id = TEST_STEP_1)
        public void testStep1(@Input("string") String string) {
        }

        @TestStep(id = TEST_STEP_2)
        public void testStep2() {
        }

        @TestStep(id = TEST_STEP_ASSERTION_FAILED)
        public void failAssert() {
            MatcherAssert.assertThat("using_aspects", Matchers.equalTo("good_idea"));
        }

    }

}
