/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl.messages;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_TITLE;

import static java.lang.String.format;


@API(Internal)
public abstract class FlowStartedMessage implements ScenarioMessage {
    private final TestStepFlow flow;
    private final LinkedHashMap<String, DataRecord> dataSourcesRecords;
    private final int vuserId;
    private final ScenarioType scenarioType;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowStartedMessage.class);

    protected FlowStartedMessage(TestStepFlow flow, int vuserId, LinkedHashMap<String, DataRecord> dataSourcesRecords, ScenarioType scenarioType) {
        this.flow = flow;
        this.dataSourcesRecords = dataSourcesRecords;
        this.vuserId = vuserId;
        this.scenarioType = scenarioType;
    }

    public TestStepFlow getFlow() {
        return flow;
    }

    public LinkedHashMap<String, DataRecord> getDataSourcesRecords() {
        return dataSourcesRecords;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public int getVuserId() {
        return vuserId;
    }

    /**
     * Gets testCaseId field from current data source record. This field is mandatory for Data Driven Scenarios.
     * Multiple data sources can be used in one test, thus testCaseId is looked up in all the data sources.
     *
     * @return testCaseId field value
     */
    public String getTestCaseId() {
        for (DataRecord dataRecord : dataSourcesRecords.values()) {
            Object testCaseId = dataRecord.getFieldValue(TEST_CASE_ID);
            if (testCaseId != null) {
                return String.valueOf(testCaseId);
            }
        }
        LOGGER.warn("None of the DataSources {} contains field {}", dataSourcesRecords.keySet(), TEST_CASE_ID);
        return null;
    }

    /**
     * Gets testTitle field from current data source record. This field is mandatory for Data Driven Scenarios
     * in which you have no TMS and you don't want testId name (AGAT scope).
     * Multiple data sources can be used in one test, thus testCaseId is looked up in all the data sources.
     *
     * @return testTitle field value
     */
    public String getTestTitle() {
        for (final DataRecord dataRecord : dataSourcesRecords.values()) {
            final Object testCaseTitle = dataRecord.getFieldValue(TEST_CASE_TITLE);
            if (testCaseTitle != null) {
                return String.valueOf(testCaseTitle);
            }
        }
        LOGGER.warn("None of the DataSources {} contains field {}", dataSourcesRecords.keySet(), TEST_CASE_TITLE);
        return null;
    }

    public static FlowStartedMessage newInstance(ScenarioExecutionContext scenarioContext, TestStepFlow flow, int vuserId, LinkedHashMap<String, DataRecord> dataSourcesRecords) {
        if (scenarioContext.getScenarioFlowId().equals(flow.getId())) {
            return new ScenarioFlow(flow, vuserId, dataSourcesRecords, scenarioContext.getScenarioType());
        } else {
            return new UserFlow(flow, vuserId, dataSourcesRecords, scenarioContext.getScenarioType());
        }
    }

    public static class UserFlow extends FlowStartedMessage {
        protected UserFlow(TestStepFlow flow, int vuserId, LinkedHashMap<String, DataRecord> dataSourcesRecords, ScenarioType scenarioType) {
            super(flow, vuserId, dataSourcesRecords, scenarioType);
        }
    }

    public static class ScenarioFlow extends FlowStartedMessage {
        protected ScenarioFlow(TestStepFlow flow, int vuserId, LinkedHashMap<String, DataRecord> dataSourcesRecords, ScenarioType scenarioType) {
            super(flow, vuserId, dataSourcesRecords, scenarioType);
        }
    }
}
