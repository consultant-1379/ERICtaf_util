package com.ericsson.cifwk.taf.ui.selenium;

import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.BrowserType;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.ui.sdk.BasicComponentsITestView;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBrowserAwareITest extends AbstractSeleniumITest {
    private static final Logger log = LoggerFactory.getLogger(AbstractBrowserAwareITest.class);
    protected Browser browser;
    protected BrowserTab browserTab;
    protected ViewModel genericView;

    @AfterClass
    public static void tearClassDown() {
        log.info("Closing UI windows that are still open");
        UI.closeAllWindows();
    }

    protected BasicComponentsITestView openComponentsView() {
        openDefaultBrowserTab();
        return browserTab.getView(BasicComponentsITestView.class);
    }

    protected ViewModel openGenericView() {
        openDefaultBrowserTab();
        return browserTab.getGenericView();
    }

    private void openDefaultBrowserTab() {
        openBrowserTab("basic_ui_components.htm");
    }

    protected void openBrowserTab(String htmlPage) {
        String testPage = findHtmlPage(htmlPage);
        this.browser = UI.newBrowser(getBrowserType());
        this.browserTab = browser.open(testPage);
        this.genericView = browserTab.getGenericView();
    }

    protected BrowserType getBrowserType() {
        return BrowserType.HEADLESS;
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
