package com.ericsson.cifwk.taf.allure.manual;

import com.ericsson.cifwk.taf.tms.TmsClient;
import com.ericsson.cifwk.taf.tms.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;
import ru.yandex.qatools.allure.events.TestCaseFinishedEvent;
import ru.yandex.qatools.allure.events.TestCaseStatusChangeEvent;
import ru.yandex.qatools.allure.events.TestSuiteFinishedEvent;

import java.util.List;
import java.util.UUID;

import static com.ericsson.cifwk.taf.allure.manual.AllureEventFactory.*;
import static com.google.common.collect.Collections2.filter;

public class AllureManualAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureManualAdaptor.class);

    private TmsClient tmsClient = new TmsClient();

    public void generateXmlReport(long testCampaignId) {

        // retrieving test campaign
        TestCampaignInfo testCampaign = tmsClient.getTestCampaign(testCampaignId);
        if (testCampaign == null) {
            LOGGER.error("Couldn't get TestCampaign {} from TMS", testCampaignId);
            return;
        }

        generateSuite(testCampaign);
    }

    private void generateSuite(TestCampaignInfo testCampaign) {
        String suiteUid = UUID.randomUUID().toString();
        Allure.LIFECYCLE.fire(createSuiteStartedEvent(suiteUid, testCampaign));
        try {
            generateTests(testCampaign.getTestCampaignItems(), suiteUid);
        } finally {
            Allure.LIFECYCLE.fire(new TestSuiteFinishedEvent(suiteUid));
        }
    }

    private void generateTests(List<TestCampaignItemInfo> testCampaignItems, String suiteUid) {
        for (TestCampaignItemInfo manualTestCampaignItem : filter(testCampaignItems, new ManualTestPredicate())) {
            generateTest(suiteUid, manualTestCampaignItem);
        }
    }

    private void generateTest(String suiteUid, TestCampaignItemInfo testCampaignItem) {

        // test case started
        Allure.LIFECYCLE.fire(createTestStartedEvent(suiteUid, testCampaignItem));

        // test steps
        List<TestStepInfo> testSteps = testCampaignItem.getTestCase().getTestSteps();
        for (TestStepInfo testStep : testSteps) {
            generateTestStep(testStep);
        }

        // test execution results
        TestCaseStatusChangeEvent event = createTestCaseFailureEvent(testCampaignItem);
        if (event != null) {
            Allure.LIFECYCLE.fire(event);
        }

        // finishing test case
        Allure.LIFECYCLE.fire(new TestCaseFinishedEvent());
    }

    private void generateTestStep(TestStepInfo testStep) {
        Allure.LIFECYCLE.fire(new StepStartedEvent(testStep.getName()));
        List<TestStepInfo> verifySteps = testStep.getVerifies();
        for (TestStepInfo verifyStep : verifySteps) {
            Allure.LIFECYCLE.fire(new StepStartedEvent(verifyStep.getName()));
            Allure.LIFECYCLE.fire(new StepFinishedEvent());
        }
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

}
