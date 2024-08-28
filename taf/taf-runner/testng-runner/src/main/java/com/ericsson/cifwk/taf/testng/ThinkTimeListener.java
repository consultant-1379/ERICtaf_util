package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.events.AfterMethodInvocationEvent;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Think-time feature, which will hold execution of current thread between test case execution.
 */
public class ThinkTimeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ThinkTimeListener.class);
    static final String PARAMETER_THINK_TIME = "taf.think-time";

    @Subscribe
    @Priority(51)
    public void afterInvocation(AfterMethodInvocationEvent event) {
        TestExecutionContext executionContext = event.getTestExecutionContext();
        TestGroup testGroup = executionContext.getTestGroup();
        processTestGroup(testGroup);
    }

    @VisibleForTesting
    void processTestGroup(TestGroup testGroup) {
        String thinkTimeString = testGroup.getDefinitionParameter(PARAMETER_THINK_TIME);
        if (thinkTimeString != null && thinkTimeString.length() > 0) {
            int thinkTime = Integer.parseInt(thinkTimeString);
            sleep(thinkTime);
        }
    }

    void sleep(int thinkTime) {
        try {
            LOG.debug("Thinking for " + thinkTime + " millis");
            Thread.sleep(thinkTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
