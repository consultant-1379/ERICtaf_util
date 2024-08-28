package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.UI;
import one.util.huntbugs.registry.anno.AssertWarning;

public class TestUsageOfUiPause {

    @AssertWarning("UsageOfUiPause")
    public void pause() {
        boolean asd = false;
        if (!asd) {
            UI.pause(1000);
        }
    }

}
