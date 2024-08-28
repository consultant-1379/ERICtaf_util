package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import ru.yandex.qatools.allure.model.Attachment;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.taf.AllureTestUtils.getLatestTestSuite;
import static com.ericsson.cifwk.taf.AllureTestUtils.runTestNg;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestStepLoggingIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TestStepLoggingIntegrationTest.class);

    private static final String SUITE_NAME = TestStepLoggingIntegrationTest.class.getName();

    public static final int VUSERS = 10;

    @org.junit.Test
    public void allureReport() {
        runTestNg(SUITE_NAME, TestClass.class);
        testAttachments();
    }

    public void testAttachments() {
        List<Attachment> stepAttachments = Lists.newArrayList();
        TestSuiteResult suite = getLatestTestSuite(SUITE_NAME);
        List<TestCaseResult> testCases = suite.getTestCases();

        assertEquals(1, testCases.size());
        List<Step> steps = testCases.iterator().next().getSteps();
        assertEquals(VUSERS * 3, steps.size());

        for (Step step : steps) {
            stepAttachments.addAll(step.getAttachments());
            assertEquals(1, step.getAttachments().size());
        }

        assertEquals(VUSERS * 3, stepAttachments.size());

        Map<Integer, List<Integer>> vUsers = getStatistics(stepAttachments);
        assertEquals(VUSERS, vUsers.keySet().size());
        for (Integer vUser : vUsers.keySet()) {
            List<Integer> records = vUsers.get(vUser);
            assertEquals(3, records.size());
            assertThat(records, hasItems(1, 2, 3));
        }
    }

    private Map<Integer, List<Integer>> getStatistics(List<Attachment> attachments) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Map<Integer, List<Integer>> vUsers = Maps.newHashMap();
        for (Attachment attachment : attachments) {
            String content = AllureTestUtils.getAttachmentContent(attachment);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                int vUser = Integer.parseInt(matcher.group(1));
                int recordId = Integer.parseInt(matcher.group(2));
                List<Integer> records = vUsers.get(vUser);
                if (records == null) {
                    records = Lists.newArrayList();
                    vUsers.put(vUser, records);
                }
                records.add(recordId);
            }
        }
        return vUsers;
    }

    public static class TestClass {

        @Test
        @TestId(id = "TAF_Scenarios_0080")
        public void parallelExecution() {
            TestDataSource<DataRecord> dataSource = fromClass(JavaDataSourceOfSize3.class);
            TafTestContext.getContext().addDataSource("records", dataSource);
            TestStepFlow flow = flow("parallelExecution")
                    .addTestStep(annotatedMethod(this, "whatever"))
                    .withDataSources(dataSource("records"))
                    .build();
            TestScenario scenario = scenario()
                    .addFlow(flow)
                    .withDefaultVusers(VUSERS)
                    .build();
            runner().build().start(scenario);
        }

        @TestStep(id = "whatever")
        public void testStepWithLogMessage(@Input("recordId") int recordId) {
            int vUser = TafTestContext.getContext().getVUser();
            try {
                Thread.sleep(new Random().nextInt(100)); // NOSONAR
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info(String.format("(%s,%s)", vUser, recordId));
        }

    }

    public class FileSizeComparator implements Comparator<File> {
        public int compare(File a, File b) {
            return (new Long(a.length()).compareTo(b.length()));
        }
    }

    public static class JavaDataSourceOfSize3 {
        @DataSource
        public List<Map<String, Object>> records() {
            List<Map<String, Object>> records = Lists.newArrayList();
            Map<String, Object> record = Maps.newHashMap();
            record.put("recordId", "1");
            records.add(record);
            record = Maps.newHashMap();
            record.put("recordId", "2");
            records.add(record);
            record = Maps.newHashMap();
            record.put("recordId", "3");
            records.add(record);
            return records;
        }
    }

}
