package com.ericsson.cifwk.taf.huntbugs;

import one.util.huntbugs.spi.HuntBugsPlugin;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 23.08.2016
 */
public class TafHuntBugsPlugin implements HuntBugsPlugin {

    @Override
    public String name() {
        return "TAF Detectors";
    }

    @Override
    public String detectorPackage() {
        return "com.ericsson.cifwk.taf.huntbugs.detect";
    }

}
