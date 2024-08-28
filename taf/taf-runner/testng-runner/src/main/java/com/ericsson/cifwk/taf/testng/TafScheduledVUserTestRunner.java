package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.TafTestContext;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TafScheduledVUserTestRunner extends com.ericsson.cifwk.taf.testng.TafTestRunner {

    private final CountDownLatch latch;

    public TafScheduledVUserTestRunner(
            IConfiguration configuration,
            ISuite suite,
            XmlTest test,
            boolean skipFailedInvocationCounts,
            Collection<IInvokedMethodListener> invokedMethodListeners,
            List<IClassListener> classListeners,
            CountDownLatch latch) {
        super(configuration, suite, test, skipFailedInvocationCounts, invokedMethodListeners, classListeners);
        for (IInvokedMethodListener listener : invokedMethodListeners) {
            addListener(listener);
        }
        for (IClassListener listener : classListeners) {
            addListener(listener);
        }
        this.latch = latch;
    }

    @Override
    public void run() {
        TafTestContext.initialize(vUser);
        try {
            setRepeatCountAndRepeatUntil();
            invokePrivateRun();
            if (!shouldRepeat(--repeatCount, repeatUntil)) {
                latch.countDown();
            }
        } finally {
            TafTestContext.remove();
        }
    }

}
