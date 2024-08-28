package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.AllureTestUtils.VerySpecialException;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.assertLabelNames;
import static com.ericsson.cifwk.taf.AllureTestUtils.assertParameter;
import static com.ericsson.cifwk.taf.AllureTestUtils.getAttachmentContent;
import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCase;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class AllureTafTestListenerIntegrationTest {

    private static String SUITE_NAME = AllureTafTestListenerIntegrationTest.class.getName();

    @org.junit.Test
    public void allureReport() {
        runTestNg(SUITE_NAME, TestClass.class);
        testXmlReport();
        testAttachments();
    }

    public void testXmlReport() {
        TestSuiteResult suite = getLatestTestSuite(SUITE_NAME);
        TestCaseResult testCase = getTestCase(suite, "CIP-6328_Func_NON_EXISTING");
        assertEquals("Testing the configuration form", testCase.getTitle());

        testCase = getTestCase(suite, "OSS-33200_Func_NON_EXISTING");
        assertEquals("OSS-33200_Func_NON_EXISTING", testCase.getTitle());
        assertLabelNames(testCase, "host", "thread");

        testCase = getTestCase(suite, "testIdFromDataSource1");
        assertEquals("testIdFromDataSource1[expectedValue]", testCase.getTitle());
        assertParameter(testCase, "testId", "testIdFromDataSource1");
        assertParameter(testCase, "expected", "expectedValue");

        testCase = getTestCase(suite, "CIS-24167_Func");
        assertThat(testCase.getStatus(), is(Status.PASSED));

        testCase = getTestCase(suite, "CIS-24167_Func_1");
        assertThat(testCase.getStatus(), is(Status.BROKEN));
        assertThat(testCase.getFailure().getMessage(), containsString("VerySpecialException"));

        testCase = getTestCase(suite, "CIP-13150_PASSED");
        assertThat(testCase.getStatus(), is(Status.PASSED));

        testCase = getTestCase(suite, "CIP-13150_FAILED");
        assertThat(testCase.getStatus(), is(Status.FAILED));
    }

    public void testAttachments() {
        TestSuiteResult suite = getLatestTestSuite(SUITE_NAME);
        TestCaseResult testCase = getTestCase(suite, "OSS-33200_Func_NON_EXISTING");
        List<Attachment> attachments = testCase.getAttachments();
        Attachment testAttachment = attachments.iterator().next();

        List<Step> testSteps = testCase.getSteps();
        assertEquals(2, testSteps.size());
        List<Attachment> step1Attachments = testSteps.iterator().next().getAttachments();
        assertEquals(1, step1Attachments.size());
        Attachment step1Attachment = step1Attachments.iterator().next();

        final String TEST_STEP_LOG = "inside test step|";
        String TEST = "inside test method|";
        assertThat(getAttachmentContent(testAttachment), containsString(TEST + TEST_STEP_LOG));
        assertEquals(TEST_STEP_LOG, getAttachmentContent(step1Attachment));
    }

    @Test(enabled = false, groups = {"mock"})
    public static class TestClass extends TafAsserts {

        @Test
        @TestId(id = "CIP-6328_Func_NON_EXISTING", title = "Testing the configuration form")
        public void testTitleIsAnnotated() {
            assertTrue(true);
            // no log messages in this test - should not create attachment
        }

        @Test(dataProvider = "testIds")
        public void testIdFromDataSource(@TestId @Input("testId") String testId, @Output("expected") String whatever) {
            assertTrue(true);
        }

        @DataProvider(name = "testIds")
        public Object[][] getParameters() {
            return new Object[][]{new Object[]{"testIdFromDataSource1", "expectedValue"}};
        }

        @Test
        @TestId(id = "OSS-33200_Func_NON_EXISTING")
        public void testTitleNotAnnotated() throws InterruptedException {
            System.out.print("inside test method|");
            testStepWithLogMessage();
            testStepWithoutLogMessage();
            LoggingThread loggingThread = new LoggingThread();
            loggingThread.start();
            loggingThread.join();
        }

        @TestId(id = "CIS-24167_Func")
        @Test(expectedExceptions = VerySpecialException.class)
        public void testExpectedException(){
            throw new VerySpecialException();
        }

        @TestId(id = "CIS-24167_Func_1")
        @Test
        public void testException(){
            throw new VerySpecialException();
        }

        @TestId(id = "CIP-13150_PASSED")
        @Test
        public void testSaveAssertPassed() {
            saveAssertTrue("Always passed", true);
        }

        @TestId(id = "CIP-13150_FAILED")
        @Test
        public void testSaveAssertFailure() {
            saveAssertTrue("Failure by design", false);
        }

        @TestStep(id = "whatever")
        public void testStepWithLogMessage() {
            System.out.print("inside test step|");
        }

        @TestStep(id = "whatever")
        public void testStepWithoutLogMessage() {
            // no log messages in this test step - should not create attachment
        }

    }

    private static class LoggingThread extends Thread {

        @Override
        public void run() {
            System.out.print("inside child thread");
        }
    }

    public class FileSizeComparator implements Comparator<File> {
        public int compare(File a, File b) {
            return (new Long(a.length()).compareTo(b.length()));
        }
    }

}
