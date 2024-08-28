package com.ericsson.cifwk.taf.swtsample;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private SwtWindow swtWindow;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        swtWindow = new SwtWindow();
        swtWindow.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        swtWindow.close();
    }

}
