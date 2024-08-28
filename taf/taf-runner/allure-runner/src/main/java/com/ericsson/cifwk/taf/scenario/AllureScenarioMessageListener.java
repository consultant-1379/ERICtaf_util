package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.AllureFacade;
import com.ericsson.cifwk.taf.TestCaseBean;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.scenario.ext.exporter.SvgExporter;
import com.ericsson.cifwk.taf.scenario.ext.exporter.messages.ScenarioGraphGeneratedMessage;
import com.ericsson.cifwk.taf.scenario.impl.FlowExecutionRecorder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import com.ericsson.cifwk.taf.scenario.impl.exception.EmptyDataSourceException;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioMessageListener;
import com.google.common.eventbus.Subscribe;

import java.util.LinkedHashMap;

public class AllureScenarioMessageListener implements ScenarioMessageListener {

    private FlowExecutionRecorder flowExecutionRecorder = new FlowExecutionRecorder();

    @Subscribe
    public void onScenarioStarted(ScenarioStartedMessage message){
        flowExecutionRecorder.populate(message.getGraph());
    }

    @Subscribe
    public void onScenarioFlowStarted(FlowStartedMessage.ScenarioFlow message) {
        flowExecutionRecorder.recordScenarioFlowId(message.getFlow());
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            String testCaseId = message.getTestCaseId();
            final String testTitle = message.getTestTitle();
            if (testCaseId != null) {
                startTestCase(message, testCaseId, testTitle);
            } else {
                testCaseId = "Missing testCaseId field";
                startTestCase(message, testCaseId, testTitle);
                final String missingTestIdMessage = "Field 'testCaseId' could not be found in Data Record\n" +
                        "You must provide 'testCaseId' field when using Data Driven Scenarios";
                final NullPointerException nullPointerException = new NullPointerException(missingTestIdMessage);
                AllureFacade.failTestCase(nullPointerException);
                throw nullPointerException;
            }
        }
    }

    private void startTestCase(FlowStartedMessage.ScenarioFlow message, String testCaseId, String testTitle) {
        TestCaseBean testCaseBean = new TestCaseBean(testCaseId, testTitle, message.getDataSourcesRecords());
        String testName = AllureFacade.getTestName(testCaseId, testCaseBean.getParameters().values().toArray());
        String suiteName = AllureFacade.getCurrentSuiteName();
        AllureFacade.startTestCase(suiteName, testName, testCaseId, testCaseBean);
    }

    @Subscribe
    public void onScenarioFlowFailed(FlowFinishedMessage.ScenarioFlowFailed message) {
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            if (message.getFullException() instanceof InvalidDataSourceException) {
                startDummyTestCase();
            }
            AllureFacade.failTestCase(message.getTestStepError());
            AllureFacade.finishTestCase();
        }
    }

    @Subscribe
    public void onFlowFinishedAllIterationsMessage(FlowFinishedAllIterationsMessage message) {
        flowExecutionRecorder.recordExecution(message);
    }

    @Subscribe
    public void onScenarioFlowSuccess(FlowFinishedMessage.ScenarioFlowSuccess message) {
        if(flowExecutionRecorder.emptyDataSourcesExist()){
            AllureFacade.failTestCase(new EmptyDataSourceException(flowExecutionRecorder.getErrorMessage()));
        }
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            AllureFacade.finishTestCase();
        }
    }

    @Subscribe
    public void onScenarioGraphGenerated(ScenarioGraphGeneratedMessage message) {
        if (message.getName().endsWith(SvgExporter.SVG_EXT)) {
            AllureFacade.attachToLatestTestCase(message.getName(), message.getGraph(), SvgExporter.SVG_MIME);
        }
    }

    private void startDummyTestCase() {
        String testCaseId = "TestId not found as test case didn't start successfully";
        AllureFacade.startTestCase(AllureFacade.getCurrentSuiteName(), "Dummy Test Case", testCaseId, new TestCaseBean(testCaseId, new LinkedHashMap<String, DataRecord>()));
    }
}
