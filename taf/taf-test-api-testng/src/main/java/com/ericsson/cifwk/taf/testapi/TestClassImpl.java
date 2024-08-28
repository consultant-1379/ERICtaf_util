package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import org.testng.IClass;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/09/2016
 */
@API(Internal)
public class TestClassImpl implements TestClass {

    private final IClass testClass;

    public TestClassImpl(IClass testClass) {
        this.testClass = testClass;
    }

    @Override
    public String getName() {
        return testClass.getName();
    }

}
