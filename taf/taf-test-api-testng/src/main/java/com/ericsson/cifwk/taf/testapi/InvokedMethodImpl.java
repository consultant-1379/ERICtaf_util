package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import org.testng.IInvokedMethod;
import org.testng.internal.ConfigurationMethod;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/07/2016
 */
@API(Internal)
public class InvokedMethodImpl implements InvokedMethod {

    private final IInvokedMethod invokedMethod;

    public InvokedMethodImpl(IInvokedMethod invokedMethod) {
        this.invokedMethod = invokedMethod;
    }

    @Override
    public boolean isTestMethod() {
        return invokedMethod.isTestMethod();
    }

    @Override
    public boolean isConfigurationMethod() {
        return invokedMethod.isConfigurationMethod() ||
                (invokedMethod.getTestMethod() != null && invokedMethod.getTestMethod() instanceof ConfigurationMethod);
    }

    @Override
    public TestMethod getTestMethod() {
        return new TestMethodImpl(invokedMethod.getTestMethod(), invokedMethod.getTestResult());
    }

    @Override
    public TestMethodExecutionResult getTestMethodExecutionResult() {
        return new TestMethodExecutionResultImpl(invokedMethod.getTestResult());
    }

    @Override
    public long getInvocationTimestamp() {
        return invokedMethod.getDate();
    }
}
