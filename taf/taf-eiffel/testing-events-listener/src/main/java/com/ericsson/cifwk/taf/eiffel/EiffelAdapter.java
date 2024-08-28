package com.ericsson.cifwk.taf.eiffel;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.eiffel.exception.UnknownParentEventException;
import com.ericsson.duraci.configuration.EiffelConfiguration;
import com.ericsson.duraci.datawrappers.Environment;
import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;
import com.ericsson.duraci.datawrappers.LogReference;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.messages.EiffelEvent;
import com.ericsson.duraci.eiffelmessage.messages.EiffelMessage;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestCaseStartedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteFinishedEvent;
import com.ericsson.duraci.eiffelmessage.messages.events.EiffelTestSuiteStartedEvent;
import com.ericsson.duraci.eiffelmessage.mmparser.clitool.EiffelConfig;
import com.ericsson.duraci.eiffelmessage.sending.MessageSender;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.getTestCaseEvent;
import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.removeTestCaseEvent;
import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.setTestCaseEvent;
import static com.ericsson.cifwk.taf.eiffel.testng.JenkinsEnvironment.BUILD_URL;
import static com.ericsson.cifwk.taf.eiffel.testng.JenkinsEnvironment.getStringVariable;

/**
 * Adapter for Eiffel test lifecycle event sending
 */
@API(Internal)
public class EiffelAdapter {

    static final String PARENT_EVENT_ID = "ER_REPORTING_PARENT_EVENT_ID";
    static final String PARENT_EXECUTION_ID = "ER_REPORTING_PARENT_EXECUTION_ID";
    private static final String TEST_EXECUTION_ID = "ER_REPORTING_TEST_EXECUTION_ID";
    public static final String MB_HOST = "ER_REPORTING_MB_HOST";

    static final String MB_EXCHANGE = "ER_REPORTING_MB_EXCHANGE";
    static final String MB_DOMAIN = "ER_REPORTING_MB_DOMAIN";

    private static final String DEFAULT_TEST_TYPE = "Functional";
    static final String DEFAULT_LOG_MAPPING = "default_log";

    private EiffelConfiguration configuration;
    private MessageSender sender;

    @VisibleForTesting
    static MessageSender senderForTests;

    private static final Logger LOGGER = LoggerFactory.getLogger(EiffelAdapter.class);

    public EiffelAdapter() {
        this.configuration = getEiffelConfiguration();
        this.sender = getSenderFactory(configuration).create();
    }

    @VisibleForTesting
    EiffelAdapter(EiffelConfiguration configuration, MessageSender sender) {
        this.configuration = configuration;
        this.sender = sender;
    }

    public ExecutionEvent fireSuiteStarted(String suiteName) throws EiffelMessageSenderException {
        EventId jobStepEventId = getJobStepEventId();
        ExecutionId jobStepExecutionId = getJobStepExecutionId();

        EiffelTestSuiteStartedEvent event =
                EiffelTestSuiteStartedEvent.Factory.create(jobStepExecutionId, DEFAULT_TEST_TYPE, suiteName);
        event.setTestExecutionId(getTestExecutionId());

        EiffelMessage message = sendMessage(toMessage(event, jobStepEventId));

        return new ExecutionEvent(message.getEventId(), event.getTestSuiteExecutionId());
    }


    public void fireTestCaseStarted(String testId, String title, Map<String, Object> parameters, ExecutionEvent suiteEvent)
            throws EiffelMessageSenderException {

        EiffelTestCaseStartedEvent event = EiffelTestCaseStartedEvent.Factory.create(
                suiteEvent.getExecutionId(),
                testId,
                title,
                mapEnvironmentParameters(parameters)
        );
        event.setTestExecutionId(getTestExecutionId());
        EiffelMessage message = sendMessage(toMessage(event, suiteEvent.getEventId()));
        setTestCaseEvent(new ExecutionEvent(message.getEventId(), event.getTestCaseExecutionId()));
    }

    @VisibleForTesting
    List<Environment> mapEnvironmentParameters(Map<String, Object> parameters) {
        List<Environment> envList = new LinkedList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Environment env = new Environment(entry.getKey(), toStringSafely(entry.getValue()));
            envList.add(env);
        }
        return envList;
    }

    public void fireTestCaseFinished(ResultCode resultCode, Map<String, Object> eventParameters)
            throws UnknownParentEventException, EiffelMessageSenderException {

        EiffelTestCaseFinishedEvent event = EiffelTestCaseFinishedEvent.Factory.create(
                resultCode,
                getLogReferenceMap(),
                getTestCaseEvent().getExecutionId()
        );
        event.setTestExecutionId(getTestExecutionId());
        mapEventParameters(eventParameters, event);
        try {
            sendMessage(toMessage(event, getTestCaseEvent().getEventId()));
        } finally {
            removeTestCaseEvent();
        }
    }

    private void mapEventParameters(Map<String, Object> eventParameters, EiffelEvent event) {
        for (Map.Entry<String, Object> entry : eventParameters.entrySet()) {
            event.setOptionalParameter(
                    entry.getKey(),
                    toStringSafely(entry.getValue()));
        }
    }

    String toStringSafely(Object value){
        return value== null ? "null" : value.toString();
    }

    public void fireSuiteFinished(ResultCode resultCode, ExecutionEvent suiteStartedEvent) throws EiffelMessageSenderException {
        EiffelTestSuiteFinishedEvent finishedEvent = EiffelTestSuiteFinishedEvent.Factory.create(
                resultCode,
                getBuildUrl(),
                suiteStartedEvent.getExecutionId()
        );
        finishedEvent.setTestExecutionId(getTestExecutionId());
        sendMessage(toMessage(finishedEvent, suiteStartedEvent.getEventId()));
    }

    private static ExecutionId getTestExecutionId() {
        String testExecutionId = getTafConfigProperty(TEST_EXECUTION_ID);
        if (testExecutionId != null) {
            return new ExecutionId(testExecutionId);
        } else {
            ExecutionId assignedTestExecutionId = new ExecutionId();
            LOGGER.warn("Test execution ID not available. Generated new test execution id - {}", assignedTestExecutionId.toString());
            TafConfiguration config = getTafConfiguration();
            config.setProperty(TEST_EXECUTION_ID, assignedTestExecutionId.toString());
            return assignedTestExecutionId;
        }
    }

    private static EiffelMessage toMessage(EiffelEvent event, EventId parentEventId) {
        String domainId = getEiffelConfiguration().getDomainId();
        return EiffelMessage.Factory.configure(domainId, event)
                .addInputEventIds(parentEventId)
                .create();
    }

    @VisibleForTesting
    synchronized EiffelMessage sendMessage(EiffelMessage message) throws EiffelMessageSenderException {
        return getSender().send(message);
    }

    private MessageSender getSender() {
        if (senderForTests == null) {
            return sender;
        }
        return senderForTests;
    }

    private MessageSender.Factory getSenderFactory(EiffelConfiguration configuration) {
        return new MessageSender.Factory(configuration);
    }

    private static EiffelConfiguration getEiffelConfiguration() {
        TafConfiguration config = getTafConfiguration();
        String mbHost = config.getString(MB_HOST);
        String mbExchange = config.getString(MB_EXCHANGE);
        String mbDomain = config.getString(MB_DOMAIN);
        return new EiffelConfig(mbDomain, mbExchange, mbHost);
    }

    private EventId getJobStepEventId() {
        String parentEventId = getTafConfigProperty(PARENT_EVENT_ID);
        if (parentEventId != null) {
            return new EventId(parentEventId);
        } else {
            LOGGER.warn("Job step event id not set.");
            return new EventId();
        }
    }

    private ExecutionId getJobStepExecutionId() {
        String parentExecutionId = getTafConfigProperty(PARENT_EXECUTION_ID);
        if (parentExecutionId != null) {
            return new ExecutionId(parentExecutionId);
        } else {
            LOGGER.warn("Job step execution id not set.");
            return new ExecutionId();
        }
    }

    private Map<String, LogReference> getBuildUrl() {
        return Collections.singletonMap("build", new LogReference(getStringVariable(BUILD_URL)));
    }

    String getLogUrl() {
        // Currently we just return a reference to Jenkins log for any event type
        String buildUrl = getStringVariable(BUILD_URL);
        if (StringUtils.isEmpty(buildUrl)) {
            return null;
        } else {
            return buildUrl + "consoleFull";
        }
    }

    Map<String, LogReference> getLogReferenceMap() {
        Map<String, LogReference> logRefMap = Maps.newHashMap();
        String logUrl = getLogUrl();
        if (logUrl != null) {
            LogReference value = new LogReference(logUrl);
            logRefMap.put(DEFAULT_LOG_MAPPING, value);
        }
        return logRefMap;
    }

    private static TafConfiguration getTafConfiguration() {
        return TafConfigurationProvider.provide();
    }

    private static String getTafConfigProperty(String key) {
        return getTafConfiguration().getString(key);
    }

    public void shutdown() {
        sender.dispose();
    }
}
