package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.TafTestContext;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.List;

public class TafVUserTestRunner extends com.ericsson.cifwk.taf.testng.TafTestRunner {

    public TafVUserTestRunner(
            IConfiguration configuration,
            ISuite suite,
            XmlTest test,
            boolean skipFailedInvocationCounts,
            Collection<IInvokedMethodListener> invokedMethodListeners,
            List<IClassListener> classListeners) {
        super(configuration, suite, test, skipFailedInvocationCounts, invokedMethodListeners, classListeners);
        for (IInvokedMethodListener listener : invokedMethodListeners) {
            addListener(listener);
        }
        for (IClassListener listener : classListeners) {
            addListener(listener);
        }
    }

    @Override
    public void run() {
        TafTestContext.initialize(vUser);
        try {
            setRepeatCountAndRepeatUntil();
            do {
                invokePrivateRun();
                repeatCount--;
            } while (shouldRepeat(repeatCount, repeatUntil));
        } finally {
            TafTestContext.remove();
        }
    }

}
