package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Sets;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/07/2016
 */
@API(Internal)
public class TestMethodImpl implements TestMethod {

    private final ITestNGMethod method;
    private final ITestResult testResult;

    public TestMethodImpl(ITestNGMethod method, ITestResult testResult) {
        this.method = method;
        this.testResult = testResult;
    }

    @Override
    public boolean isBeforeMethodConfiguration() {
        return method.isBeforeMethodConfiguration();
    }

    @Override
    public boolean isBeforeTestGroupConfiguration() {
        // TestNG suites, groups and <test>s (group of tests in XML) are treated as TAF Test API test groups
        return method.isBeforeSuiteConfiguration() || method.isBeforeGroupsConfiguration() || method.isBeforeTestConfiguration();
    }

    @Override
    public boolean isBeforeTestMethodConfiguration() {
        return method.isBeforeMethodConfiguration();
    }

    @Override
    public boolean isBeforeTestClassConfiguration() {
        return method.isBeforeClassConfiguration();
    }

    @Override
    public boolean isAfterTestGroupConfiguration() {
        return method.isAfterSuiteConfiguration() || method.isAfterGroupsConfiguration() || method.isAfterTestConfiguration();
    }

    @Override
    public boolean isAfterTestMethodConfiguration() {
        return method.isAfterMethodConfiguration();
    }

    @Override
    public boolean isAfterTestClassConfiguration() {
        return method.isAfterClassConfiguration();
    }

    @Override
    public String getName() {
        return method.getMethodName();
    }

    @Override
    public TestClass getTestClass() {
        return new TestClassImpl(method.getTestClass());
    }

    @Override
    public Method getJavaMethod() {
        return method.getConstructorOrMethod().getMethod();
    }

    @Override
    public Object[] getParameters() {
        return testResult.getParameters();
    }

    @Override
    public Set<String> getGroupNames() {
        return Sets.newHashSet(method.getGroups());
    }

    @Override
    public int getCurrentInvocationCount() {
        return method.getCurrentInvocationCount();
    }

    @Override
    public long getTimeOut() {
        return method.getTimeOut();
    }

    @Override
    public void setTimeOut(long timeOutInMillis) {
        method.setTimeOut(timeOutInMillis);
    }

    @Override
    public String toString() {
        return "TestMethodImpl{" +
                "method=" + method +
                ", testResult=" + testResult +
                '}';
    }
}
