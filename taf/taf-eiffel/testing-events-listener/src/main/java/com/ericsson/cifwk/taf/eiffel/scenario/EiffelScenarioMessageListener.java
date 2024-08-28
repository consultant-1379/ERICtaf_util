package com.ericsson.cifwk.taf.eiffel.scenario;

import com.ericsson.cifwk.taf.ParametersManager;
import com.ericsson.cifwk.taf.TestCaseBean;
import com.ericsson.cifwk.taf.eiffel.EiffelAdapter;
import com.ericsson.cifwk.taf.eiffel.exception.UnknownParentEventException;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioMessageListener;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.getTestSuiteEvent;

public class EiffelScenarioMessageListener implements ScenarioMessageListener {

    private EiffelAdapter adapter;
    private static Logger logger = LoggerFactory.getLogger(EiffelScenarioMessageListener.class);

    public EiffelScenarioMessageListener() {
        adapter = new EiffelAdapter();
    }

    @Subscribe
    public void onScenarioFlowStarted(FlowStartedMessage.ScenarioFlow message) {
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            String testCaseId = message.getTestCaseId();
            if (testCaseId != null) {
                TestCaseBean testCaseBean = new TestCaseBean(testCaseId, message.getDataSourcesRecords());
                String testCaseTitle = ParametersManager.getParametrizedName(
                        testCaseId,
                        testCaseBean.getParameters().values().toArray());
                try {
                    adapter.fireTestCaseStarted(
                            testCaseId,
                            testCaseTitle,
                            testCaseBean.getParameters(),
                            getTestSuiteEvent());
                } catch (EiffelMessageSenderException | UnknownParentEventException e) {
                    logger.info("Cannot send test case start message to TMS.");
                }
            }
        }
    }

    @Subscribe
    public void onScenarioFlowSuccess(FlowFinishedMessage.ScenarioFlowSuccess message) {
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            fireTestCaseFinished(ResultCode.SUCCESS);
        }

    }

    @Subscribe
    public void onScenarioFlowFailed(FlowFinishedMessage.ScenarioFlowFailed message) {
        if (ScenarioType.DATA_DRIVEN.equals(message.getScenarioType())) {
            fireTestCaseFinished(ResultCode.FAILURE);
        }
    }

    private void fireTestCaseFinished(ResultCode resultCode) {
        try {
            adapter.fireTestCaseFinished(resultCode, new HashMap<String, Object>());
        } catch (UnknownParentEventException | EiffelMessageSenderException e) {
            logger.info("Cannot send test case finished message to TMS.");
        }
    }
}
