package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Shared;

/**
 * Shared annotation is used which is not required anymore.
 */
@Shared
public class SharedAnnotationUsed {

    public void doNothing() throws InterruptedException {
        System.out.println("Do nothing");
    }

}
