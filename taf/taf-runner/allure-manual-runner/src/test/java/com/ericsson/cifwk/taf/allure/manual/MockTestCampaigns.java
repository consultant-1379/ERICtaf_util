package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.dto.ProjectInfo;
import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import com.ericsson.cifwk.taf.tms.dto.RequirementInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignItemInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCaseInfo;
import com.ericsson.cifwk.taf.tms.dto.TestStepInfo;
import com.google.common.collect.Lists;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Mihails Volkovs <mihails.volkovs@ericsson.com>
 * 20/07/2015
 */
public class MockTestCampaigns {

    protected static TestCampaignInfo mockTestCampaign(TestCampaignItemInfo testCampaignItem) {
        ProjectInfo project = mockProject();

        TestCampaignInfo testCampaign = mock(TestCampaignInfo.class);
        when(testCampaign.getId()).thenReturn(456L);
        when(testCampaign.getName()).thenReturn("planName");
        when(testCampaign.getDescription()).thenReturn("planDescription");
        when(testCampaign.getEnvironment()).thenReturn("env");
        when(testCampaign.getProject()).thenReturn(project);
        when(testCampaign.getTestCampaignItems()).thenReturn(singletonList(testCampaignItem));
        return testCampaign;
    }

    protected static TestCampaignItemInfo mockTestCampaignItem() {
        TestCaseInfo testCase = mockTestCase();
        ReferenceDataItem executionResult = mockReferenceData("Pass");
        when(executionResult.getId()).thenReturn("2");
        TestCampaignItemInfo testCampaignItem = mock(TestCampaignItemInfo.class);
        when(testCampaignItem.getTestCase()).thenReturn(testCase);
        when(testCampaignItem.getResult()).thenReturn(executionResult);
        when(testCampaignItem.getDefectIds()).thenReturn(asList("BUG-123", "BUG-456"));
        return testCampaignItem;
    }

    private static ProjectInfo mockProject() {
        ProjectInfo project = mock(ProjectInfo.class);
        when(project.getExternalId()).thenReturn("PROJ");
        when(project.getName()).thenReturn("project");
        return project;
    }

    private static TestCaseInfo mockTestCase() {
        ReferenceDataItem context1 = mockReferenceData("REST");
        ReferenceDataItem context2 = mockReferenceData("UI");
        ReferenceDataItem executionType = mockReferenceData("Manual");
        ReferenceDataItem testCaseStatus = mockReferenceData("Preliminary");
        ReferenceDataItem priority = mockReferenceData("High");
        ReferenceDataItem type = mockReferenceData("Functional");
        TestStepInfo testStep1 = mockTestStep(1);
        TestStepInfo testStep2 = mockTestStep(2);
        ReferenceDataItem component1 = mockComponent("component1");
        ReferenceDataItem component2 = mockComponent("component2");
        List<RequirementInfo> requirements = mockRequirements("REQ-123", "REQ-456");

        TestCaseInfo testCase = mock(TestCaseInfo.class);
        when(testCase.getTechnicalComponents()).thenReturn(asList(component1, component2));
        when(testCase.getComment()).thenReturn("comment");
        when(testCase.getContexts()).thenReturn(asList(context1, context2));
        when(testCase.getDescription()).thenReturn("testDescription");
        when(testCase.getExecutionType()).thenReturn(executionType);
        when(testCase.getPackageName()).thenReturn("package");
        when(testCase.getPrecondition()).thenReturn("precondition");
        when(testCase.getPriority()).thenReturn(priority);
        when(testCase.getRequirements()).thenReturn(requirements);
        when(testCase.getTestCaseId()).thenReturn("TEST-123");
        when(testCase.getTestCaseStatus()).thenReturn(testCaseStatus);
        when(testCase.getTitle()).thenReturn("testTitle");
        when(testCase.getType()).thenReturn(type);
        when(testCase.getTestSteps()).thenReturn(asList(testStep1, testStep2));
        return testCase;
    }

    private static TestStepInfo mockTestStep(int i) {
        TestStepInfo testStep = mock(TestStepInfo.class);
        when(testStep.getName()).thenReturn("stepName" + i);
        if (i < 10) {
            TestStepInfo verifyStep = mockTestStep(i + 10);
            when(testStep.getVerifies()).thenReturn(singletonList(verifyStep));
        }
        return testStep;
    }

    private static ReferenceDataItem mockComponent(String name) {
        ReferenceDataItem result = mock(ReferenceDataItem.class);
        when(result.getTitle()).thenReturn(name);
        return result;
    }

    private static ReferenceDataItem mockReferenceData(String title) {
        ReferenceDataItem release = mock(ReferenceDataItem.class);
        when(release.getTitle()).thenReturn(title);
        return release;
    }

    private static List<RequirementInfo> mockRequirements(String... externalIds) {
        List<RequirementInfo> requirements = Lists.newArrayList();
        for (String externalId : externalIds) {
            RequirementInfo requirement = mock(RequirementInfo.class);
            when(requirement.getExternalId()).thenReturn(externalId);
            when(requirement.getSummary()).thenReturn("Requirement summary");
            requirements.add(requirement);
        }
        return requirements;
    }

}
