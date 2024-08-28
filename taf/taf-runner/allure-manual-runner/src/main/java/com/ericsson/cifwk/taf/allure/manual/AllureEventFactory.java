package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.dto.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import ru.yandex.qatools.allure.events.TestCaseCanceledEvent;
import ru.yandex.qatools.allure.events.TestCaseFailureEvent;
import ru.yandex.qatools.allure.events.TestCasePendingEvent;
import ru.yandex.qatools.allure.events.TestCaseStartedEvent;
import ru.yandex.qatools.allure.events.TestCaseStatusChangeEvent;
import ru.yandex.qatools.allure.events.TestSuiteStartedEvent;
import ru.yandex.qatools.allure.model.Description;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.taf.TafAllureModelUtils.createCommentLabel;
import static com.ericsson.cifwk.taf.TafAllureModelUtils.createManualExecutionTypeLabel;
import static com.ericsson.cifwk.taf.tms.PriorityConverter.toSeverity;
import static com.ericsson.cifwk.taf.tms.dto.TestExecutionResult.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static java.lang.String.format;
import static ru.yandex.qatools.allure.config.AllureModelUtils.*;

class AllureEventFactory {

    private AllureEventFactory() {
        // hiding constructor
    }

    public static TestSuiteStartedEvent createSuiteStartedEvent(String suiteUid, TestCampaignInfo testCampaign) {
        String suiteName = "Manual Test Campaign: " + testCampaign.getId();
        String suiteTitle = "Manual: " + testCampaign.getName();
        return new TestSuiteStartedEvent(suiteUid, suiteName)
                .withTitle(suiteTitle)
                .withDescription(getSuiteDescription(testCampaign))
                .withLabels(createTestFrameworkLabel("TAF"));
    }

    public static TestCaseStartedEvent createTestStartedEvent(String suiteUid, TestCampaignItemInfo testCampaignItem) {

        // event
        TestCaseInfo testCase = testCampaignItem.getTestCase();
        TestCaseStartedEvent event = new TestCaseStartedEvent(suiteUid, testCase.getTestCaseId())
                .withTitle(testCase.getTitle())
                .withDescription(getTestDescription(testCampaignItem));

        // link to Bug Tracker
        List<String> defectIds = checkNotNull(testCampaignItem.getDefectIds(), "Test case had null array of defect ids");
        for (String defectId : defectIds) {
            event.withLabels(createIssueLabel(defectId));
        }

        // links to Requirement Management System are disabled
        // link to Test Management System
        event.withLabels(createTestLabel(testCase.getTestCaseId()));

        // group by requirement
        List<RequirementInfo> stories = testCase.getRequirements();
        for (RequirementInfo story : stories) {
            event.withLabels(createStoryLabel(format("%s: %s", story.getExternalId(), story.getSummary())));
        }

        // group by feature
        List<ReferenceDataItem> technicalComponents = testCase.getTechnicalComponents();
        for (ReferenceDataItem technicalComponent : technicalComponents) {
            event.withLabels(createFeatureLabel(technicalComponent.getTitle()));
        }
        event.withLabels(createCommentLabel(testCase.getComment()));
        event.withLabels(createManualExecutionTypeLabel());

        // standard labels
        return event.withLabels(createSeverityLabel(toSeverity(testCase.getPriority().getTitle())));
    }

    public static TestCaseStatusChangeEvent createTestCaseFailureEvent(TestCampaignItemInfo testCampaignItem) {
        return createTestCaseFailureEvent(toEnum(testCampaignItem.getResult()));
    }

    @VisibleForTesting
    protected static TestCaseStatusChangeEvent createTestCaseFailureEvent(TestExecutionResult result) {
        Map<TestExecutionResult, TestCaseStatusChangeEvent> events = Maps.newHashMap();
        events.put(PASSED_WITH_EXCEPTION, new TestCaseFailureEvent().withThrowable(new RuntimeException("Passed with exception")));
        events.put(FAIL, new TestCaseFailureEvent().withThrowable(new AssertionError("Fail")));
        events.put(BLOCKED, new TestCaseCanceledEvent().withThrowable(new RuntimeException("Blocked")));
        addPendingEvent(events, NOT_STARTED);
        addPendingEvent(events, WIP);
        addPendingEvent(events, NOT_DEFINED);
        addPendingEvent(events, UNKNOWN);
        return events.get(result);
    }

    private static void addPendingEvent(Map<TestExecutionResult, TestCaseStatusChangeEvent> events, TestExecutionResult result) {
        events.put(result, new TestCasePendingEvent().withMessage(result.getName()));
    }

    @VisibleForTesting
    protected static Description getSuiteDescription(TestCampaignInfo testCampaign) {
        ProjectInfo project = testCampaign.getProject();
        return new DescriptionBuilder("Test Suite")
                .withTestCampaignLink(testCampaign.getId())
                .withField("Environment", testCampaign.getEnvironment())
                .withField("Project", project == null ? "" : format("%s: %s", project.getExternalId(), project.getName()))
                .withLongText(testCampaign.getDescription())
                .withSeparator()
                .build();
    }

    @VisibleForTesting
    protected static Description getTestDescription(TestCampaignItemInfo testCampaignItem) {
        TestCaseInfo testCase = testCampaignItem.getTestCase();
        ReferenceDataItem executionStatus = testCampaignItem.getResult();
        ReferenceDataItem testStatus = testCase.getTestCaseStatus();
        ReferenceDataItem type = testCase.getType();

        List<RequirementInfo> requirements = checkNotNull(testCase.getRequirements(), "Test case had null array of requirements");
        Collection<String> requirementIds = transform(requirements, new Function<RequirementInfo, String>() {
            @Override
            public String apply(RequirementInfo input) {
                return input.getExternalId();
            }
        });

        return new DescriptionBuilder("Test Case")
                .withRequirementLinks(requirementIds)
                .withField("Type", type == null ? "" : type.getTitle())
                .withField("Package", testCase.getPackageName())
                .withFields("Component", testCase.getTechnicalComponents())
                .withField("Test Status", testStatus == null ? "" : testStatus.getTitle())
                .withField("Execution Status", executionStatus == null ? "" : executionStatus.getTitle())
                .withParagraph("Description", testCase.getDescription())
                .withParagraph("Comment", testCase.getComment())
                .withParagraph("Precondition", testCase.getPrecondition())
                .build();
    }

}
