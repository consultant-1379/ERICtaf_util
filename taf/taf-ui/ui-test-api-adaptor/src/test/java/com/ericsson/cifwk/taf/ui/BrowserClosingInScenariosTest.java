/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BrowserClosingInScenariosTest {

    @Test
    public void verifyAllBrowsersClose() {
        UI.init();
        TestStepFlow openBrowsers = TestScenarios.flow("openBrowsers")
                .addTestStep(TestScenarios.annotatedMethod(this, "createBrowsers")).withVusers(2).build();
        TestScenario scenarioOpen = TestScenarios.scenario("Result").addFlow(openBrowsers).build();
        TestScenarios.runner().withDefaultExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build().start(scenarioOpen);

        assertEquals(6, UI.getActiveBrowserCount());
        UI.closeAllWindows();
        assertEquals(0, UI.getActiveBrowserCount());
    }

    @TestStep(id = "createBrowsers")
    public void createBrowsers() {
        Browser browser, browser1, browser2;
        browser = UI.newBrowser(BrowserType.HEADLESS);
        browser1 = UI.newBrowser(BrowserType.HEADLESS);
        browser2 = UI.newBrowser(BrowserType.HEADLESS);
        assertFalse(browser.isClosed());
        assertFalse(browser1.isClosed());
        assertFalse(browser2.isClosed());
    }
}
