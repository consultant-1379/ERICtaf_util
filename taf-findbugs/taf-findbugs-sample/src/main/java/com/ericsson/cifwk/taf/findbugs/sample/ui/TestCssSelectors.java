package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.09.2016
 */
public class TestCssSelectors {

    public static final String LONG_VERSION_OF_CSS_SELECTOR = "LongVersionOfCssSelector";

    public static final String EMPTY_SELECTOR = "EmptySelector";

    public static final String TOO_MANY_SELECTORS_DEFINED = "TooManySelectorsDefined";

    @AssertWarning(LONG_VERSION_OF_CSS_SELECTOR)
    @UiComponentMapping(selector = ".eaLauncher-ListItem", selectorType = SelectorType.CSS)
    private UiComponent longOne;

    @AssertWarning(LONG_VERSION_OF_CSS_SELECTOR)
    @UiComponentMapping(selector = ".eaLauncher-ListItem")
    private UiComponent shorterOne;

    @AssertNoWarning(LONG_VERSION_OF_CSS_SELECTOR)
    @UiComponentMapping(".eaLauncher-ListItem")
    private UiComponent shortOne;

    @AssertWarning(EMPTY_SELECTOR)
    @UiComponentMapping()
    private UiComponent emptySelector;

    @AssertWarning(EMPTY_SELECTOR)
    @UiComponentMapping(selectorType = SelectorType.CSS)
    private UiComponent anotherEmptySelector;

    @AssertWarning(TOO_MANY_SELECTORS_DEFINED)
    @UiComponentMapping(value = ".class", selector = ".class")
    private UiComponent tooManySelectorsDefined;

}
