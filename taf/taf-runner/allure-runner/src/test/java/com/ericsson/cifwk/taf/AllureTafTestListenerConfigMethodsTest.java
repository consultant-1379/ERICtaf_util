package com.ericsson.cifwk.taf;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.IOException;
import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static org.junit.Assert.assertThat;

public class AllureTafTestListenerConfigMethodsTest {

    @org.junit.Test
    public void testParallelMethods() throws IOException {
        runTestNg("ParallelMethods_suite.xml");
        validateSimpleSuiteResult(getLatestTestSuite("ParallelMethods Suite"));
    }

    @org.junit.Test
    public void testMultipleClass() throws IOException {
        runTestNg("MultipleClass_suite.xml");
        validateMultipleClassResult(getLatestTestSuite("MultipleClass Suite"));
    }

    @org.junit.Test
    public void testParallelMultipleClass() throws IOException {
        runTestNg("ParallelMultipleClass_suite.xml");
        validateMultipleClassResult(getLatestTestSuite("ParallelMultipleClass Suite"));
    }

    @org.junit.Test
    public void testMultipleTests() throws IOException {
        runTestNg("MultipleTests_suite.xml");
        validateMultipleTestResult(getLatestTestSuite("MultipleTests Suite"));
    }

    @org.junit.Test
    public void testParallelMultipleTests() throws IOException {
        runTestNg("ParallelMultipleTests_suite.xml");
        validateMultipleTestResult(getLatestTestSuite("ParallelMultipleTests Suite"));
    }

    private void validateSimpleSuiteResult(TestSuiteResult suiteResult) {
        List<TestCaseResult> testCases = suiteResult.getTestCases();
        assertThat(testCases.size(), Matchers.is(3));

        validateEachCaseHasSteps(testCases, "@BeforeMethod", "@AfterMethod");
        validateFirstCaseHasSteps(testCases, "@BeforeSuite", "@BeforeTest");
        validateLastCaseHasSteps(testCases, "@AfterSuite", "@AfterTest");

    }

    private void validateMultipleClassResult(TestSuiteResult suiteResult) {
        List<TestCaseResult> testCases = suiteResult.getTestCases();
        assertThat(testCases.size(), Matchers.is(6));

        List<TestCaseResult> firstClassCases = filterTestCases(testCases, "TORF-49968");
        List<TestCaseResult> secondClassCases = filterTestCases(testCases, "TORF-66667_Func_1");

        validateEachCaseHasSteps(firstClassCases, "@BeforeMethod: setUpMethod", "@AfterMethod: tearDownMethod");
        validateEachCaseHasSteps(secondClassCases,"@BeforeMethod: setUpMethod_C2", "@AfterMethod: tearDownMethod_C2");

        validateFirstCaseHasSteps(testCases,
                "@BeforeSuite: setUpSuite", "@BeforeSuite: setUpSuite_C2",
                "@BeforeTest: setUpTest", "@BeforeTest: setUpTest_C2");
        validateLastCaseHasSteps(testCases,
                "@AfterSuite: tearDownSuite", "@AfterSuite: tearDownSuite_C2",
                "@AfterTest: tearDownTest", "@AfterTest: tearDownTest_C2");

    }

    private void validateMultipleTestResult(TestSuiteResult suiteResult) {
        List<TestCaseResult> testCases = suiteResult.getTestCases();
        assertThat(testCases.size(), Matchers.is(6));

        List<TestCaseResult> firstClassCases = filterTestCases(testCases, "TORF-49968");
        List<TestCaseResult> secondClassCases = filterTestCases(testCases, "TORF-66667_Func_1");

        validateEachCaseHasSteps(firstClassCases, "@BeforeMethod: setUpMethod", "@AfterMethod: tearDownMethod");
        validateEachCaseHasSteps(secondClassCases,"@BeforeMethod: setUpMethod_C2", "@AfterMethod: tearDownMethod_C2");

        validateFirstCaseHasSteps(testCases,"@BeforeSuite: setUpSuite", "@BeforeSuite: setUpSuite_C2");
        validateLastCaseHasSteps(testCases, "@AfterSuite: tearDownSuite", "@AfterSuite: tearDownSuite_C2");

        validateFirstCaseHasSteps(firstClassCases,"@BeforeTest: setUpTest");
        validateLastCaseHasSteps(firstClassCases, "@AfterTest: tearDownTest");

        validateFirstCaseHasSteps(secondClassCases,"@BeforeTest: setUpTest_C2");
        validateLastCaseHasSteps(secondClassCases, "@AfterTest: tearDownTest_C2");
    }


    private void validateEachCaseHasSteps(List<TestCaseResult> testCases, String... expectedSteps) {
        for (TestCaseResult testCase : testCases) {
            validateCaseHasSteps(testCase, expectedSteps);
        }

    }

    private void validateCaseHasSteps(TestCaseResult testCaseResult, String... expectedSteps) {
        List<Step> steps = testCaseResult.getSteps();
        for (String expectedStep : expectedSteps) {
            Matcher<Step> startsWithExpectedName = Matchers.hasProperty("name", Matchers.startsWith(expectedStep));
            assertThat(steps, Matchers.hasItem(startsWithExpectedName));
        }
    }

    private void validateFirstCaseHasSteps(List<TestCaseResult> testCases, String... steps) {
        validateCaseHasSteps(testCases.get(0), steps);
    }

    private void validateLastCaseHasSteps(List<TestCaseResult> testCases, String... steps) {
        validateCaseHasSteps(testCases.get(testCases.size() - 1), steps);
    }

    private List<TestCaseResult> filterTestCases(List<TestCaseResult> testCases, final String testCaseName) {
        return Lists.newArrayList(Collections2.filter(testCases, new Predicate<TestCaseResult>() {
            @Override
            public boolean apply(TestCaseResult input) {
                return input.getName().equals(testCaseName);
            }
        }));
    }
}
