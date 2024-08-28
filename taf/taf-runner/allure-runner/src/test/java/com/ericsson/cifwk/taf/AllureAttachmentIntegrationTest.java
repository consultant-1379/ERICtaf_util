package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Attachment;
import com.ericsson.cifwk.taf.annotations.TestId;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCase;
import static com.ericsson.cifwk.taf.AllureTestUtils.getTestCaseNames;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static com.ericsson.cifwk.taf.ReadableIsIterableContainingInAnyOrder.containsInAnyOrder;
import static java.nio.charset.Charset.forName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Mihails Volkovs (mihails.volkovs@ericsson.com)
 *         11/08/2015
 */

public class AllureAttachmentIntegrationTest {

    public static final String ROOT_SUITE = AllureAttachmentIntegrationTest.class.getSimpleName();

    public static final String ATTACHMENT_TEST = "emptyAttachmentTest";

    public static final String EMPTY_ATTACHMENT_NAME = "emptyAttachment";

    public static final String SIMPLE_ATTACHMENT_NAME = "simpleAttachment";

    public static final String SIMPLE_ATTACHMENT_CONTENT = "content";

    private static TestSuiteResult testSuite1;

    @BeforeClass
    public static void generateReport() {
        runTestNg(ROOT_SUITE, AllureAttachmentsTest.class);

        testSuite1 = getLatestTestSuite(ROOT_SUITE);
        assertNotNull(testSuite1);
    }

    @Test
    public void allTestCasesFound() {
        assertThat(getTestCaseNames(testSuite1), containsInAnyOrder(ATTACHMENT_TEST));
    }

    @Test
    public void testEmptyAttachment() {

        // getting all test attachments
        TestCaseResult testCase = getTestCase(testSuite1, ATTACHMENT_TEST);
        assertEquals(ATTACHMENT_TEST, testCase.getTitle());
        List<ru.yandex.qatools.allure.model.Attachment> attachments = testCase.getAttachments();

        // checking there are no empty attachments
        assertThat(attachments).hasSize(1);
        ru.yandex.qatools.allure.model.Attachment attachment = attachments.iterator().next();
        assertThat(attachment.getTitle()).isEqualTo(SIMPLE_ATTACHMENT_NAME);
        assertThat(attachment.getTitle()).isNotEqualTo(EMPTY_ATTACHMENT_NAME);
    }

    public static class AllureAttachmentsTest {

        @org.testng.annotations.Test
        @TestId(id = ATTACHMENT_TEST)
        public void emptyAttachmentsShouldNotBeAdded() {
            createAttachment();
            createEmptyAttachment();
        }

        @Attachment(type = "text/plain", value = SIMPLE_ATTACHMENT_NAME)
        public byte[] createAttachment() {
            return SIMPLE_ATTACHMENT_CONTENT.getBytes(forName("utf-8"));
        }

        @Attachment(type = "text/plain", value = EMPTY_ATTACHMENT_NAME)
        public byte[] createEmptyAttachment() {
            return new byte[]{};
        }
    }

}
