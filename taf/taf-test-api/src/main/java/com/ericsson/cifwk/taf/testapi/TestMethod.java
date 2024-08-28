package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Test class method representation. It stands for both configuration (e.g. <code>@Before</code>)
 * and ordinary (<code>@Test</code>) methods.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 20/06/2016
 */
@API(Internal)
public interface TestMethod extends Serializable {

    /**
     * @return true if this method is a config method to be invoked before test
     */
    boolean isBeforeMethodConfiguration();

    /**
     * @return true if this method is a config method to be invoked before test group
     */
    boolean isBeforeTestGroupConfiguration();

    /**
     * @return true if this method is a config method to be invoked before test method (annotated as @Test)
     */
    boolean isBeforeTestMethodConfiguration();

    /**
     * @return true if this method is a config method to be invoked before test class execution
     */
    boolean isBeforeTestClassConfiguration();

    /**
     * @return true if this method is a config method to be invoked after test group
     */
    boolean isAfterTestGroupConfiguration();

    /**
     * @return true if this method is a config method to be invoked after test method (annotated as @Test)
     */
    boolean isAfterTestMethodConfiguration();

    /**
     * @return true if this method is a config method to be invoked after test class execution
     */
    boolean isAfterTestClassConfiguration();

    /**
     * @return the method name.
     */
    String getName();

    /**
     * @return test class which this test method belongs to
     */
    TestClass getTestClass();

    /**
     * @return the actual Java method (annotated as test) being invoked
     */
    Method getJavaMethod();

    /**
     * @return parameters this method was called with
     */
    Object[] getParameters();

    /**
     * @return the names of the groups that test method belongs to
     */
    Set<String> getGroupNames();

    /**
     * @return current number of invocations
     */
    int getCurrentInvocationCount();

    /**
     * @return The timeout in milliseconds.
     */
    long getTimeOut();

    /**
     * Set timeout for method execution, in milliseconds
     * @param timeOutInMillis
     */
    void setTimeOut(long timeOutInMillis);

}
