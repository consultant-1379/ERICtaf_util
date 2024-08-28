package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.VUserScoped;

/**
 * Shared annotation is used which is not required anymore.
 */
@VUserScoped
public class VUserScopedAnnotationUsed {

    public void doNothing() throws InterruptedException {
        System.out.println("Do nothing");
    }

}
