package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;

public class SelectorHell {

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "id=1")
    Button good;

    @UiComponentMapping(selectorType = SelectorType.XPATH, selector = "/html/body/div")
    Button button;

}
