package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.ext.exporter.SvgExporter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.util.List;

import static com.ericsson.cifwk.taf.AllureTestUtils.*;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_ENABLED;
import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScenarioSvgAttachmentTest {

    public static final String SUITE_NAME_1 = "Simple Scenario SVG attachment";
    public static final String SUITE_NAME_2 = "Two simple Scenarios SVG attachment";
    public static final String SCENARIO_NAME_1 = "testAttachments1";
    public static final String SCENARIO_NAME_2 = "testAttachments2";

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void prepareGlobalDataSource() {
        System.setProperty(TAF_SCENARIO_DEBUG_ENABLED, "true");
    }

    @org.junit.Test
    public void simpleScenario() {
        runTestNg(SUITE_NAME_1, SimpleScenarioAttachmentTest.class);
        TestSuiteResult testSuite = getLatestTestSuite(SUITE_NAME_1);
        TestCaseResult testCase = getLast(testSuite.getTestCases());
        List<Attachment> attachments = getAttachmentsByType(testCase, SvgExporter.SVG_MIME);
        assertThat(attachments, hasSize(1));
        assertThat(attachments.get(0).getType(), equalTo(SvgExporter.SVG_MIME));
        assertThat(attachments.get(0).getTitle(), containsString(SCENARIO_NAME_1));
    }

    @org.junit.Test
    public void twoSimpleScenarios() {
        runTestNg(SUITE_NAME_2, TwoSimpleScenariosAttachmentTest.class);
        TestSuiteResult testSuite = getLatestTestSuite(SUITE_NAME_2);
        TestCaseResult testCase = getLast(testSuite.getTestCases());
        List<Attachment> attachments = getAttachmentsByType(testCase, SvgExporter.SVG_MIME);
        assertThat(attachments, hasSize(2));
        assertThat(attachments.get(0).getTitle(), containsString(SCENARIO_NAME_1));
        assertThat(attachments.get(1).getTitle(), containsString(SCENARIO_NAME_2));
    }

    public static class SimpleScenarioAttachmentTest {
        @Test
        public void simpleScenario() {
            TestScenario scenario = scenario(SCENARIO_NAME_1).addFlow(flow("A")).build();
            runner().build().start(scenario);
        }
    }

    public static class TwoSimpleScenariosAttachmentTest {
        @Test
        public void twoSimpleScenarios() {
            TestScenario scenario1 = scenario(SCENARIO_NAME_1).addFlow(flow("A")).build();
            TestScenario scenario2 = scenario(SCENARIO_NAME_2).addFlow(flow("B")).build();
            runner().build().start(scenario1);
            runner().build().start(scenario2);
        }
    }
}


