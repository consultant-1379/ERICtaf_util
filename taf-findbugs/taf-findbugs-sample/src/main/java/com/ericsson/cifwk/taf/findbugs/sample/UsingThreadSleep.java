package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.ui.UI;

public class UsingThreadSleep {

    public void sleep() throws InterruptedException {
        boolean a = false;
        if (!a) {
            Thread.sleep(1000);
        }
    }

}
