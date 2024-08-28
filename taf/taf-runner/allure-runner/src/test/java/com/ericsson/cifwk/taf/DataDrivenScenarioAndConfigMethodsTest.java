package com.ericsson.cifwk.taf;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static com.ericsson.cifwk.taf.testdata.DataDrivenScenarioWithConfigMethods.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class DataDrivenScenarioAndConfigMethodsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataDrivenScenarioAndConfigMethodsTest.class);

    @Test
    public void testSequentially() throws IOException {
        runTestNg("CIP-9057_Sequential_suite.xml");
        validateSuiteResults();
    }

    @Test
    public void testParallel() throws IOException {
        runTestNg("CIP-9057_Parallel_suite.xml");
        validateSuiteResults();
    }

    private void validateSuiteResults() throws IOException {
        TestSuiteResult suite1 = getLatestTestSuite(SUITE_1);
        TestSuiteResult suite2 = getLatestTestSuite(SUITE_2);
        TestSuiteResult suite3 = getLatestTestSuite(SUITE_3);
        List<TestSuiteResult> suites = Arrays.asList(suite1, suite2, suite3);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(suite1, out);
        JAXB.marshal(suite2, out);
        JAXB.marshal(suite3, out);
        LOGGER.info("Latest suites after execution: \r\n" + new String(out.toByteArray()));

        for (TestSuiteResult suite : suites) {
            validateSuite(suite);
        }

        Collections.sort(suites, new Comparator<TestSuiteResult>() {
            @Override
            public int compare(TestSuiteResult s1, TestSuiteResult s2) {
                return new Long(s1.getStop()).compareTo(s2.getStop());
            }
        });

        validateFirstSuite(suites.iterator().next());
        validateAnySuiteHasTearDownSteps(suites, "@AfterSuite", "@AfterTest");
    }

    private void validateFirstSuite(TestSuiteResult result) {
        validateFirstCaseHasSteps(result.getTestCases(), "@BeforeSuite", "@BeforeTest");
    }

    private void validateSuite(TestSuiteResult suite) {
        LOGGER.info(String.format("Validating suite '%s'", suite.getName()));
        assertThat(suite.getTestCases().size(), is(3));
        validateFirstCaseHasSteps(suite.getTestCases(), "@BeforeMethod");
        validateLastCaseHasSteps(suite.getTestCases(), "@AfterMethod");
    }


    private boolean validateCaseHasSteps(TestCaseResult testCaseResult, String... expectedSteps) {
        List<Step> steps = testCaseResult.getSteps();
        for (String expectedStep : expectedSteps) {
            boolean contains = false;
            for (Step step : steps) {
                if (step.getName().startsWith(expectedStep)) {
                    if (contains) {
                        fail(String.format("Only one %s step expected in test case %s",
                                expectedStep,
                                testCaseResult.getName()));
                    }
                    contains = true;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;
    }

    private void validateFirstCaseHasSteps(List<TestCaseResult> testCases, String... steps) {
        TestCaseResult testCase = testCases.get(0);
        assertTrue(String.format("%s steps expected in test case %s", Arrays.toString(steps), testCase.getName()),
                validateCaseHasSteps(testCase, steps));
    }

    private void validateLastCaseHasSteps(List<TestCaseResult> testCases, String... steps) {
        TestCaseResult testCase = testCases.get(testCases.size() - 1);
        assertTrue(String.format("%s steps expected in test case %s", Arrays.toString(steps), testCase.getName()),
                validateCaseHasSteps(testCase, steps));
    }

    private void validateAnySuiteHasTearDownSteps(List<TestSuiteResult> suites, String... steps) {
        for (TestSuiteResult suite : suites) {
            List<TestCaseResult> testCases = suite.getTestCases();
            TestCaseResult testCase = testCases.get(testCases.size() - 1);
            if (validateCaseHasSteps(testCase, steps)) {
                return;
            }
        }
        fail(String.format("%s steps expected in one of the test suites", Arrays.toString(steps)));
    }

}
