package com.ericsson.cifwk.taf.ui.selenium;

import com.ericsson.cifwk.taf.ui.BrowserTestUtils;


public abstract class AbstractSeleniumITest {

    protected String findHtmlPage(String fileName) {
        return BrowserTestUtils.findHtmlPage(fileName);
    }

}
