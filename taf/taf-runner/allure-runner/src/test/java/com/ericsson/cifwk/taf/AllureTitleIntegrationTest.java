package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Attachment;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCase;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCaseNames;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCases;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static com.ericsson.cifwk.taf.AllureTitleIntegrationTest.DataDrivenScenarioTest.TEST_CASE_1;
import static com.ericsson.cifwk.taf.AllureTitleIntegrationTest.DataDrivenScenarioTest.TEST_CASE_2;
import static com.ericsson.cifwk.taf.AllureTitleIntegrationTest.DataDrivenScenarioTest.TEST_CASE_3;
import static com.ericsson.cifwk.taf.ReadableIsIterableContainingInAnyOrder.containsInAnyOrder;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Mihails Volkovs (mihails.volkovs@ericsson.com)
 *         11/08/2015
 */

public class AllureTitleIntegrationTest {

    public static final String ROOT_SUITE = AllureTitleIntegrationTest.class.getSimpleName();

    public static final String SIMPLE_TEST = "testCaseIdOfSimpleTest";
    public static final String TEST_WITH_TEST_STEPS = "idOfTestCaseWithTestSteps";
    public static final String TEST_WITH_ATTACHMENTS = "makeAttachment";
    public static final String TEST_STEP_WITHOUT_PARAMETER = "testStepWithoutParameter";
    public static final String TEST_STEP_WITH_VARIABLE_PARAMETER = "testStepId";
    public static final String PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID = "parameterizedTest";
    public static final String PARAMETERIZED_TEST_WITH_TEST_ID_IN_DS = "TestIdFromTestNgDataSource";

    // for checking please go to https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/TAF_Scenarios_0092
    public static final String REAL_TEST_IN_TMS = "TAF_Scenarios_0092";
    public static final String REAL_TEST_TITLE_IN_TMS = "Test ID as input parameter from data source";

    private static TestSuiteResult testSuite1;
    private static TestSuiteResult testSuite2;

    @BeforeClass
    public static void generateReport() {
        runTestNg(ROOT_SUITE,
                AllureTitles.class,
                ParameterizedTest.class,
                ParameterizedTest2.class,
                DataDrivenScenarioTest.class);

        testSuite1 = getLatestTestSuite(ROOT_SUITE);
        assertNotNull(testSuite1);

        testSuite2 = getLatestTestSuite("AllureTitleIntegrationTest.DataDrivenScenarioTest");
        assertNotNull(testSuite2);
    }

    @Test
    public void allTestCasesFound() {
        assertThat(getTestCaseNames(testSuite1), containsInAnyOrder(
                SIMPLE_TEST,
                REAL_TEST_IN_TMS,
                TEST_WITH_TEST_STEPS,
                TEST_WITH_ATTACHMENTS,
                PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID,
                PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID,
                PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID,
                PARAMETERIZED_TEST_WITH_TEST_ID_IN_DS
        ));

        assertThat(getTestCaseNames(testSuite2), containsInAnyOrder(
                TEST_CASE_1,
                TEST_CASE_2,
                TEST_CASE_3
        ));
    }

    @Test
    public void testTitleShouldBeEqualToTestId() {
        TestCaseResult testCase = getTestCase(testSuite1, SIMPLE_TEST);
        assertEquals(SIMPLE_TEST, testCase.getTitle());
    }

    @Test
    @Ignore("Test requires access to TMS and doesn't work without VPN")
    public void realTestTitleShouldBeEqualToTestId  () {
        TestCaseResult testCase = getTestCase(testSuite1, REAL_TEST_IN_TMS);
        assertEquals(REAL_TEST_TITLE_IN_TMS, testCase.getTitle());
    }

    @Test
    public void ifTestStepIdEqualsToMethodNameShouldShowJustOnceInStepTitle() {
        TestCaseResult testCase = getTestCase(testSuite1, TEST_WITH_TEST_STEPS);
        Iterator<Step> testSteps = testCase.getSteps().iterator();

        // title should contain both test step ID and method name
        assertStep(testSteps, "testStepWithoutParameter", "testStepWithoutParameter");

        // if test step ID equals to method name show just one in the title
        assertStep(testSteps, "testStepWithParameter", "testStepId: testStepWithParameter");
    }


    @Test
    public void testStepsTitlesShouldNeverContainEmptysquareBrackets() {
        TestCaseResult testCase = getTestCase(testSuite1, TEST_WITH_TEST_STEPS);
        assertEquals("idOfTestCaseWithTestSteps", testCase.getTitle());
        List<Step> steps = testCase.getSteps();
        assertEquals(6, steps.size());
        Iterator<Step> testSteps = steps.iterator();

        // skipping first 2 test steps (as being tested in separate test)
        testSteps.next();
        testSteps.next();

        // empty [] should never appear in the titles
        assertStep(testSteps, "testStepWithParameter", "testStepId: testStepWithParameter[null]");
        assertStep(testSteps, "testStepWithParameter", "testStepId: testStepWithParameter");
        assertStep(testSteps, "testStepWithParameter", "testStepId: testStepWithParameter");
        assertStep(testSteps, "testStepWithParameter", "testStepId: testStepWithParameter[parameter]");
    }

    @Test
    public void attachmentTitlesShouldNeverContainParameters() {
        TestCaseResult testCase = getTestCase(testSuite1, TEST_WITH_ATTACHMENTS);
        assertEquals(TEST_WITH_ATTACHMENTS, testCase.getTitle());
        Collection<String> attachmentTitles = Collections2.transform(testCase.getAttachments(), new Function<ru.yandex.qatools.allure.model.Attachment, String>() {
            @Override
            public String apply(ru.yandex.qatools.allure.model.Attachment attachment) {
                return attachment.getTitle();
            }
        });
        assertThat(attachmentTitles, containsInAnyOrder(
                "makeAttachment",
                "makeAnotherAttachment",
                "My attachment title with parameter \"screenshot\"",
                "log.txt"));
    }

    @Test
    public void testNgParameterizedTestShouldNeverDisplayEmptyParameters() {
        List<TestCaseResult> testCases = getTestCases(testSuite1, PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID);
        assertEquals(3, testCases.size());
        Iterator<TestCaseResult> tests = testCases.iterator();
        TestCaseResult testCase = tests.next();
        assertEquals("parameterizedTest", testCase.getTitle());
        testCase = tests.next();
        assertEquals("parameterizedTest[null]", testCase.getTitle());
        testCase = tests.next();
        assertEquals("parameterizedTest[value]", testCase.getTitle());
    }

    @Test
    public void testNgParameterizedTestTitleShouldExcludeTestIdParameter() {
        TestCaseResult testCase = getTestCase(testSuite1, PARAMETERIZED_TEST_WITH_TEST_ID_IN_DS);
        assertEquals("TestIdFromTestNgDataSource[parameter]", testCase.getTitle());
    }

    @Test
    public void dataDrivenScenarioShouldWorkAsWell() {

        // Test
        TestCaseResult testCase = getTestCase(testSuite2, TEST_CASE_1);
        assertEquals("testCase1", testCase.getTitle());
        Iterator<Step> testSteps = testCase.getSteps().iterator();
        assertStep(testSteps, "testStep1", "ts1: testStep1[null]");
        assertStep(testSteps, "testStep2", "testStep2[null]");

        // Test
        testCase = getTestCase(testSuite2, TEST_CASE_2);
        assertEquals("testCase2", testCase.getTitle());
        testSteps = testCase.getSteps().iterator();
        assertStep(testSteps, "testStep1", "ts1: testStep1");
        assertStep(testSteps, "testStep2", "testStep2");

        // Test
        testCase = getTestCase(testSuite2, TEST_CASE_3);
        assertEquals("testCase3", testCase.getTitle());
        testSteps = testCase.getSteps().iterator();
        assertStep(testSteps, "testStep1", "ts1: testStep1[A]");
        assertStep(testSteps, "testStep2", "testStep2[A]");
    }

    private static void assertStep(Iterator<Step> step, String expectedName, String expectedTitle) {
        Step testStep = step.next();
        assertEquals(expectedName, testStep.getName());
        assertEquals(expectedTitle, testStep.getTitle());
    }

    public static class AllureTitles {

        @org.testng.annotations.Test
        @TestId(id = SIMPLE_TEST)
        public void simpleTestMethodName() {
            // empty
        }

        @org.testng.annotations.Test
        @TestId(id = REAL_TEST_IN_TMS)
        public void realTestCaseExistingInTms() {
            // empty
        }

        @org.testng.annotations.Test
        @TestId(id = TEST_WITH_TEST_STEPS)
        public void testCaseWithTestSteps() {
            testStepWithoutParameter();
            testStepWithParameter();
            testStepWithParameter((String) null);
            testStepWithParameter("");
            testStepWithParameter("", "");
            testStepWithParameter("parameter");
        }

        @org.testng.annotations.Test
        @TestId(id = TEST_WITH_ATTACHMENTS)
        public void attachmentTitleShouldNotHaveParametersAtAll() {
            makeAttachment("parameter");
            makeAnotherAttachment();
            makeAttachmentWithTemplate("screenshot");
        }

        @TestStep(id = TEST_STEP_WITHOUT_PARAMETER)
        public void testStepWithoutParameter() {
            // empty
        }

        @TestStep(id = TEST_STEP_WITH_VARIABLE_PARAMETER)
        public void testStepWithParameter(String... args) {
            // empty
        }

        @Attachment
        public byte[] makeAttachment(String parameter) {
            System.out.println("Forcing log.txt attachment to be created");
            return parameter.getBytes();
        }

        @Attachment
        public byte[] makeAnotherAttachment() {
            return "Without parameter".getBytes();
        }

        @Attachment("My attachment title with parameter '{0}'")
        public byte[] makeAttachmentWithTemplate(String parameter) {
            return parameter.getBytes();
        }

    }

    public static class ParameterizedTest {

        @org.testng.annotations.DataProvider(name = "test1")
        public static Object[][] dataProvider() {
            return new Object[][]{{""}, {null}, {"value"}};
        }

        @org.testng.annotations.Test(dataProvider = "test1")
        @TestId(id = PARAMETERIZED_TEST_WITH_HARDCODED_TEST_ID)
        public void parameterizedTest(String value) {
            // empty
        }

    }

    public static class ParameterizedTest2 {

        @org.testng.annotations.DataProvider(name = "test2")
        public static Object[][] dataProvider() {
            return new Object[][]{{PARAMETERIZED_TEST_WITH_TEST_ID_IN_DS, "parameter"}};
        }

        @org.testng.annotations.Test(dataProvider = "test2")
        public void parameterizedTest(@TestId String value, String parameter) {
            // empty
        }

    }

    public static class DataDrivenScenarioTest {

        public static final String DATA_DRIVEN_SCENARIO_SUITE = "AllureTitleIntegrationTest.DataDrivenScenarioTest";
        private static final String DATA_SOURCE = DATA_DRIVEN_SCENARIO_SUITE;

        public static final String TEST_CASE_1 = "testCase1";
        public static final String TEST_CASE_2 = "testCase2";
        public static final String TEST_CASE_3 = "testCase3";

        public static final String TEST_STEP_1 = "ts1";
        public static final String TEST_STEP_2 = "testStep2";

        @org.testng.annotations.Test
        @TestSuite(DATA_DRIVEN_SCENARIO_SUITE)
        public void simpleScenario() {
            prepareDataSource();

            TestScenario scenario = dataDrivenScenario()
                    .addFlow(flow("A")
                            .addTestStep(annotatedMethod(this, TEST_STEP_1))
                            .addTestStep(annotatedMethod(this, TEST_STEP_2))
                    )
                    .withScenarioDataSources(dataSource(DATA_SOURCE))
                    .build();
            runner().build().start(scenario);
        }

        private void prepareDataSource() {
            TestContext context = TafTestContext.getContext();

            TestDataSource<DataRecord> dataSource = context.dataSource(DATA_SOURCE);
            dataSource.addRecord()
                    .setField(TEST_CASE_ID, TEST_CASE_1)
                    .setField("string", null);
            dataSource.addRecord()
                    .setField(TEST_CASE_ID, TEST_CASE_2)
                    .setField("string", "");
            dataSource.addRecord()
                    .setField(TEST_CASE_ID, TEST_CASE_3)
                    .setField("string", "A");
        }

        @TestStep(id = TEST_STEP_1)
        public void testStep1(@Input("string") String string) {
            // empty
        }

        @TestStep(id = TEST_STEP_2)
        public void testStep2(@Input("string") String string) {
            // empty
        }

    }

}
