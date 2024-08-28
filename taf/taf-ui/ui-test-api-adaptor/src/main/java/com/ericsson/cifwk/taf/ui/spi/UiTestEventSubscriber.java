package com.ericsson.cifwk.taf.ui.spi;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.InvokedMethod;
import com.ericsson.cifwk.taf.testapi.events.AfterMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.BeforeMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionEvent;
import com.ericsson.cifwk.taf.ui.UI;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Listener for the tests that are using TAF UI. Assists automatic browser close feature.
 * In general TAF UI framework allows to set automatic close browsers by {@link com.ericsson.cifwk.taf.execution.TestExecutionEvent}.
 * <p>
 * User is required to set TestExecutionEvent in @BeforeSuite method.
 * Depending on event type {@link UI} registry container will be reinitialized.
 * Rules for reinitialization apply:
 * <p><ul>
 * <li>ON_EXECUTION_FINISH doesn't require reinitialization.
 * Closing of all browsers happens in {@link #onTestSessionEvent(TestSessionEvent)}
 * <li>ON_SUITE_FINISH require reinitialization after <b>first</b> <code>@BeforeSuite</code> annotated method.
 * Closing of all browsers happens in {@link #onTestSuiteFinishEvent(TestGroupFinishedEvent)}.
 * <li>ON_TEST_FINISH require reinitialization before <b>first</b> <code>@BeforeTest</code> annotated method, but this method may not exist.
 * Additional reinitialization attempt is made before test start, see {@link #onTestCaseEvent(TestCaseEvent)} if it's a test case start.
 * </ul>
 * Closing of all browsers happens in {@link #onTestCaseEvent(TestCaseEvent)}.
 */
@API(Internal)
public class UiTestEventSubscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiTestEventSubscriber.class);

    @Subscribe
    public void beforeInvocation(BeforeMethodInvocationEvent event) {
        InvokedMethod method = event.getMethod();
        // before @BeforeTest
        if (isBeforeTestMethod(method) && UI.isCloseOnTest() && UI.isReInitializationPossible()) {
            UI.resetRegistryContainer();
        }
    }

    @Subscribe
    public void afterInvocation(AfterMethodInvocationEvent event) {
        InvokedMethod method = event.getMethod();
        // after @BeforeSuite
        if (isBeforeSuiteMethod(method) && UI.isCloseOnSuite() && UI.isReInitializationPossible()) {
            UI.resetRegistryContainer();
        }
    }

    private boolean isBeforeTestMethod(InvokedMethod method) {
        return isConfigurationMethod(method) && method.getTestMethod().isBeforeMethodConfiguration();
    }

    private boolean isBeforeSuiteMethod(InvokedMethod method) {
        return isConfigurationMethod(method) && method.getTestMethod().isBeforeTestGroupConfiguration();
    }

    private boolean isConfigurationMethod(InvokedMethod method) {
        return method.isConfigurationMethod();
    }

    @Subscribe
    public void onTestCaseEvent(TestCaseEvent event) {
        boolean closeOnTest = UI.isCloseOnTest();
        switch (event.getExecutionState()) {
            case STARTED:
                if (closeOnTest && UI.isReInitializationPossible()) {
                    UI.resetRegistryContainer();
                }
                break;
            case SUCCEEDED:
            case FAILED:
            case SKIPPED:
                if (closeOnTest) {
                    closeAllUiWindows();
                }
                break;
        }
    }

    @Subscribe
    public void onTestSessionEvent(TestSessionEvent event) {
        switch (event.getExecutionPhase()) {
            case START:
                UI.init();
                break;
            case FINISH: //after all suites
                if (UI.isCloseOnExecution()) {
                    closeAllUiWindows();
                }
                break;
        }
    }

    @Subscribe //after suite
    public void onTestSuiteFinishEvent(TestGroupFinishedEvent event) {
        if (UI.isCloseOnSuite()) {
            closeAllUiWindows();
        }
    }

    @VisibleForTesting
    void closeAllUiWindows() {
        LOGGER.trace("closing UI windows that are still open");
        try {
            UI.closeAllWindows();
            LOGGER.trace("all UI windows closed");
        } catch (Exception e) {
            LOGGER.error("Failed to close windows successfully", e);
        }
    }
}
