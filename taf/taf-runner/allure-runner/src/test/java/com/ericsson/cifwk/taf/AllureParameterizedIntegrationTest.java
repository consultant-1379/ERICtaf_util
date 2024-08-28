package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import static com.ericsson.cifwk.taf.AllureTestUtils.*;
import static com.ericsson.cifwk.taf.ReadableIsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;


public class AllureParameterizedIntegrationTest {

    public static final String TEST_SUITE = "Command line suite";

    public static final String TEST_CASE_ID = "Test Case ID";

    private static final String TEST_STEP = "testDtep1";

    @org.junit.Test
    public void allureReport() throws Exception {
        runTestNg(ParameterizedTest.class);
        testXmlReports();
    }

    private void testXmlReports() throws Exception {
        TestSuiteResult testSuite1 = getLatestTestSuite(TEST_SUITE);
        assertThat(getTestCaseNames(testSuite1), containsInAnyOrder(TEST_CASE_ID, TEST_CASE_ID));
        assertThat(getTestCaseTitles(testSuite1), containsInAnyOrder(TEST_CASE_ID + "[Ireland]", TEST_CASE_ID + "[Latvia]"));
    }

    public static class ParameterizedTest {

        @Test(dataProvider = "bestCountries")
        @TestId(id = TEST_CASE_ID)
        public void methodParameters(String value) {
            testStep1(value);
        }

        @org.testng.annotations.DataProvider(name = "bestCountries")
        public Object[][] createData1() {
            return new Object[][]{
                    {"Ireland"},
                    {"Latvia"},
            };
        }

        @TestStep(id = TEST_STEP)
        public void testStep1(@Input("string") String string) {
        }

    }


}

