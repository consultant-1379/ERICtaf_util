package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.dto.*;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.allure.events.TestCaseCanceledEvent;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCasePendingEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.events.TestCaseStatusChangeEvent;
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.Label;

import java.util.List;

import static com.ericsson.cifwk.taf.allure.manual.MockTestCampaigns.mockTestCampaign;
import static com.ericsson.cifwk.taf.allure.manual.MockTestCampaigns.mockTestCampaignItem;
import static com.ericsson.cifwk.taf.tms.dto.TestExecutionResult.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.yandex.qatools.allure.model.DescriptionType.MARKDOWN;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 17/07/2015
 */
public class AllureEventFactoryTest {

    private TestCampaignInfo emptyTestCampaign;

    private TestCampaignItemInfo emptyTestCampaignItem;

    private TestCampaignInfo testCampaign;

    private TestCampaignItemInfo testCampaignItem;

    @Before
    public void setUp() {

        // empty test campaign
        emptyTestCampaign = mock(TestCampaignInfo.class);
        when(emptyTestCampaign.getId()).thenReturn(123L);

        // empty test campaign
        TestCaseInfo emptyTestCase = mock(TestCaseInfo.class);
        emptyTestCampaignItem = mock(TestCampaignItemInfo.class);
        when(emptyTestCampaignItem.getTestCase()).thenReturn(emptyTestCase);

        // full test campaign
        testCampaignItem = mockTestCampaignItem();
        testCampaign = mockTestCampaign(testCampaignItem);
    }

    @Test
    public void createSuiteStartedEvent() {

        // checking basic properties
        TestSuiteStartedEvent event = AllureEventFactory.createSuiteStartedEvent("suiteUid", testCampaign);
        assertEquals("Manual Test Campaign: 456", event.getName());
        assertEquals("Manual: planName", event.getTitle());
        assertEquals("suiteUid", event.getUid());
        assertTrue(event.getDescription().getValue().length() > 0);

        // checking labels
        List<Label> labels = event.getLabels();
        assertEquals(1, labels.size());
        assertLabel("framework", "TAF", labels.iterator().next());
    }

    @Test
    public void createTestStartedEvent() {
        TestCaseStartedEvent event = AllureEventFactory.createTestStartedEvent("suiteUid", testCampaignItem);
        assertEquals("TEST-123", event.getName());
        assertEquals("testTitle", event.getTitle());
        assertTrue(event.getDescription().getValue().length() > 0);
        assertEquals("suiteUid", event.getSuiteUid());

        // checking labels
        int i = 0;
        List<Label> labels = event.getLabels();
        assertEquals(10, labels.size());
        assertLabel("issue", "BUG-123", labels.get(i++));
        assertLabel("issue", "BUG-456", labels.get(i++));
        assertLabel("testId", "TEST-123", labels.get(i++));
        assertLabel("story", "REQ-123: Requirement summary", labels.get(i++));
        assertLabel("story", "REQ-456: Requirement summary", labels.get(i++));
        assertLabel("feature", "component1", labels.get(i++));
        assertLabel("feature", "component2", labels.get(i++));
        assertLabel("comment", "comment", labels.get(i++));
        assertLabel("execution_type", "manual", labels.get(i++));
        assertLabel("severity", "blocker", labels.get(i++));
    }

    @Test
    public void createTestCaseEvent() {
        assertEvent(PASSED_WITH_EXCEPTION, TestCaseFailureEvent.class, RuntimeException.class);
        assertEvent(FAIL, TestCaseFailureEvent.class, AssertionError.class);
        assertEvent(BLOCKED, TestCaseCanceledEvent.class, RuntimeException.class);
        assertEvent(NOT_STARTED, TestCasePendingEvent.class, null);
        assertEvent(WIP, TestCasePendingEvent.class, null);
        assertEvent(NOT_DEFINED, TestCasePendingEvent.class, null);
        assertEvent(UNKNOWN, TestCasePendingEvent.class, null);
        assertNull(AllureEventFactory.createTestCaseFailureEvent(PASS));
    }

    @Test
    public void getEmptySuiteDescription() {
        Description description = AllureEventFactory.getSuiteDescription(emptyTestCampaign);
        assertEquals(MARKDOWN, description.getType());
        String actual = description.getValue();
        String expected = "## Test Suite\nTest Campaign: [123](https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTestCampaign/123) \n___\n";
        assertEquals(expected, actual);
    }

    @Test
    public void getSuiteDescription() {
        String description = AllureEventFactory.getSuiteDescription(testCampaign).getValue();
        assertThat(description, containsString("## Test Suite"));
        assertThat(description, containsString("Test Campaign: [456]"));
        assertThat(description, containsString("Environment: **env**"));
        assertThat(description, containsString("Project: **PROJ: project**"));
        assertThat(description, containsString("> planDescription"));
        assertThat(description, containsString("___"));
    }

    @Test
    public void getEmptyTestDescription() {
        Description description = AllureEventFactory.getTestDescription(emptyTestCampaignItem);
        assertEquals(MARKDOWN, description.getType());
        String actual = description.getValue();
        String expected = "## Test Case\n";
        assertEquals(expected, actual);
    }

    @Test
    public void getTestDescription() {
        String description = AllureEventFactory.getTestDescription(testCampaignItem).getValue();
        assertThat(description, containsString("## Test Case"));
        assertThat(description, containsString("Requirements: [REQ-123](http://jira-oss.lmera.ericsson.se/browse/REQ-123) [REQ-456](http://jira-oss.lmera.ericsson.se/browse/REQ-456)"));
        assertThat(description, containsString("Type: **Functional**"));
        assertThat(description, containsString("Package: **package**"));
        assertThat(description, containsString("Component: **component1**"));
        assertThat(description, containsString("Component: **component2**"));
        assertThat(description, containsString("Test Status: **Preliminary**"));
        assertThat(description, containsString("Execution Status: **Pass**"));
        assertThat(description, containsString("### Description\n> testDescription"));
        assertThat(description, containsString("### Comment\n> comment"));
        assertThat(description, containsString("### Precondition\n> precondition"));
    }

    private void assertLabel(String expectedKey, String expectedValue, Label actualLabel) {
        assertEquals(expectedKey, actualLabel.getName());
        assertEquals(expectedValue, actualLabel.getValue());
    }

    private void assertEvent(TestExecutionResult status, Class<?> expectedEventClass, Class<?> expectedExceptionClass) {
        TestCaseStatusChangeEvent event = AllureEventFactory.createTestCaseFailureEvent(status);
        assertNotNull(event);
        assertEquals(expectedEventClass, event.getClass());
        if (expectedExceptionClass != null) {
            Throwable throwable = event.getThrowable();
            assertEquals(expectedExceptionClass, throwable.getClass());
            assertEquals(status.getName(), throwable.getMessage());
        }
    }

}
