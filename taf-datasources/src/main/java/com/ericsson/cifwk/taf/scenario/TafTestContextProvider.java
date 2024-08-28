package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.spi.TestContextProvider;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/03/2016
 */
@API(Internal)
public class TafTestContextProvider implements TestContextProvider {

    @Override
    public TestContext get() {
        return TafTestContext.getContext();
    }

    @Override
    public void initialize(TestContext testContext) {
        TafTestContext.initialize(testContext);
    }

    @Override
    public boolean isContextInitialized() {
        return TafTestContext.isInitialized();
    }

    @Override
    public void removeContext() {
        TafTestContext.remove();
    }

    @Override
    public void initialize(int vUser) {
        TafTestContext.initialize(vUser);
    }
}
