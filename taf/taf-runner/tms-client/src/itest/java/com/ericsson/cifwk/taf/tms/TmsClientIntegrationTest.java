/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.tms;
import com.ericsson.cifwk.taf.tms.dto.ReferenceDataItem;
import com.ericsson.cifwk.taf.tms.dto.RequirementInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCampaignItemInfo;
import com.ericsson.cifwk.taf.tms.dto.TestCaseInfo;
import com.ericsson.cifwk.taf.tms.dto.TestStepInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class TmsClientIntegrationTest {

    private TmsClient tmsClient;

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() {
        tmsClient = new TmsClient();
    }

    @Test
    public void testNoTmsTestPlanDefined() throws Exception {
        System.setProperty("taf.tms.api.test-case", "null");
        System.setProperty("taf.tms.web.test-plan", "null");
        System.setProperty("taf.tms.api.test-plan", "null");

        tmsClient = new TmsClient();
        assertNull(tmsClient.getTestCase("CIP-925_Perf_2"));
        assertNull(tmsClient.getTestCase("clearlyNotExistingTestCase"));
    }

    @Test
    public void testNoTmsTestCampaignDefined() throws Exception {
        System.setProperty("taf.tms.api.test-case", "null");
        System.setProperty("taf.tms.web.test-campaign", "null");
        System.setProperty("taf.tms.api.test-campaign", "null");

        tmsClient = new TmsClient();
        assertNull(tmsClient.getTestCase("CIP-925_Perf_2"));
        assertNull(tmsClient.getTestCase("clearlyNotExistingTestCase"));
    }

    @Test
    public void shouldBeCompatibleWithTmsTestCaseData() {
        // please use following URL to check test case properties
        // https://taftm.seli.wh.rnd.internal.ericsson.com/tm-server/api/test-cases/CIP-925_Perf_2?view=Detailed
        TestCaseInfo testCase = tmsClient.getTestCase("CIP-925_Perf_2");
        assertNotNull(testCase);
        assertEquals("Iterate Test Case Execution Based on 3 VUser and Context Values Updated Again", testCase.getTitle());
        assertEquals("Test if a Test Case is executed 3 times based on 3 values specified in VUsers and Context Annotations", testCase.getDescription());
        List<ReferenceDataItem> technicalComponents = testCase.getTechnicalComponents();
        assertThat(technicalComponents).hasSize(1);
        assertEquals("Other", technicalComponents.iterator().next().getTitle());

        assertEquals("Minor", testCase.getPriority().getTitle());
        assertNotNull(testCase.getRequirements());
        RequirementInfo requirement = testCase.getRequirements().iterator().next();
        assertEquals("CIP-925", requirement.getExternalId());
        assertEquals("As Developer I want TAF to iterate test case execution, based on Vusers array annotation", requirement.getSummary());
    }

    @Test
    public void shouldBeCompatibleWithTmsTestPlanData() {
        // please use following URL to check test campaign properties
        // https://taftm.seli.wh.rnd.internal.ericsson.com/tm-server/api/test-campaigns/814?view=detailedItems
        TestCampaignInfo testCampaign = tmsClient.getTestCampaign(814);

        // checking test campaign
        assertNotNull(testCampaign);
        assertEquals("TAF HopBuilder Test Campaign", testCampaign.getName());
        assertEquals("Functional tests to validate CLI Hopbuilder", testCampaign.getDescription());
        assertEquals("ENM System", testCampaign.getEnvironment());
        //assertEquals("ENM 15B", testCampaign.getRelease().getTitle());
        assertEquals("CIP", testCampaign.getProject().getExternalId());
        assertEquals("CI_FrameworkTeam_PDUOSS", testCampaign.getProject().getName());

        // checking test case execution
        List<TestCampaignItemInfo> testCampaignItems = testCampaign.getTestCampaignItems();
        assertEquals(20, testCampaignItems.size());
        TestCampaignItemInfo testPlanItem = testCampaignItems.iterator().next();
        assertEquals("Pass", testPlanItem.getResult().getTitle());
        assertEquals(0, testPlanItem.getDefectIds().size());

        // checking test case
        TestCaseInfo testCase = testPlanItem.getTestCase();
        assertEquals(14_259, testCase.getId());
        assertEquals("TAF_HopBuilder_Func_001", testCase.getTestCaseId());
        assertEquals("Verify that a test can change to a new user that exists on the current host", testCase.getTitle());
        assertEquals("The goal of this test is to verify that the CliCommandHelper Session can change user on the current host", testCase.getDescription());
        List<ReferenceDataItem> technicalComponents = testCase.getTechnicalComponents();
        assertThat(technicalComponents).hasSize(1);
        assertEquals("TAF - CLI", technicalComponents.iterator().next().getTitle());
        assertEquals("Blocker", testCase.getPriority().getTitle());
        assertEquals("Functional", testCase.getType().getTitle());
        assertEquals("Automated", testCase.getExecutionType().getTitle());
        assertEquals("CLI", testCase.getContexts().iterator().next().getTitle());
        assertEquals(null, testCase.getPackageName());
        assertEquals("Preliminary", testCase.getTestCaseStatus().getTitle());

        // checking requirements
        List<RequirementInfo> requirements = testCase.getRequirements();
        assertEquals(1, requirements.size());
        RequirementInfo requirement = requirements.iterator().next();
        assertEquals("CIP-939", requirement.getExternalId());

        assertEquals("As a developer I want to design Test Case so it can be executed using CLI", requirement.getSummary());

        // checking test steps
        List<TestStepInfo> testSteps = testCase.getTestSteps();
        assertEquals(2, testSteps.size());
        TestStepInfo testStep1 = testSteps.get(0);
        TestStepInfo testStep2 = testSteps.get(1);
        assertEquals("ssh onto host with known user", testStep1.getName());
        assertEquals(1, testStep1.getVerifies().size());
        assertEquals("\"whoami\" command returns user", testStep1.getVerifies().iterator().next().getName());
        assertEquals("hop to new user on the host", testStep2.getName());
        assertEquals(1, testStep2.getVerifies().size());
        assertEquals("\"whoami\" command returns new user", testStep2.getVerifies().iterator().next().getName());

        // All other data into attachment?
        assertThat(testCase.getPrecondition(), startsWith("Host is available"));
        assertEquals("Test execution comment", testCase.getComment());
    }

}
