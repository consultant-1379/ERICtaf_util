package com.ericsson.cifwk.taf.osgi.agent;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public final class TestUtils {

    private TestUtils() {
    }

    public static final String BUNDLE_SN = "com.ericsson.cifwk.taf.osgi.agent";

    public static Bundle getBundle(BundleContext ctx, String symbolicName) {
        for (Bundle b : ctx.getBundles()) {
            if (symbolicName.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }

    public static boolean commandExists(BundleContext ctx, String command) {
        ServiceReference[] serviceReferences;
        try {
            serviceReferences = ctx.getServiceReferences(CommandProvider.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            return false;
        }
        for (ServiceReference serviceReference : serviceReferences) {
            CommandProvider service = (CommandProvider) ctx.getService(serviceReference);
            if (service != null && service.getHelp().contains(command)) {
                return true;
            }
        }
        return false;
    }

}
