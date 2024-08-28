/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.AllureFacade.ConfigMethodType;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.InvokedMethod;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethod;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.BeforeMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestConfigurationEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.exceptions.TestSkipException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class AllureTafTestListener {

    // TODO: single constant is wrong, fine-tune the priorities for individual method executions instead
    protected static final int PRIORITY = 100;

    private ThreadLocal<Throwable> latestThrowable = new InheritableThreadLocal<>();

    @Subscribe
    @Priority(PRIORITY)
    public void onConfigurationEvent(TestConfigurationEvent configurationEvent) {
        TestMethodExecutionResult testExecutionResult = configurationEvent.getTestExecutionResult();
        TafAnnotationManager annotationManager = getTafAnnotationManager(testExecutionResult);
        switch(configurationEvent.getExecutionState()) {
            case SUCCEEDED:
                AllureFacade.passConfig(annotationManager.getFixtureName(), getConfigMethodType(testExecutionResult));
                break;
            case FAILED:
                Throwable throwable = testExecutionResult.getThrowable();
                String fixtureName = annotationManager.getFixtureName();
                if (annotationManager.isSetUpMethod()) {
                    latestThrowable.set(throwable);
                }
                AllureFacade.failConfig(throwable, fixtureName, getConfigMethodType(testExecutionResult));
                break;
            case SKIPPED:
                AllureFacade.skipConfig(annotationManager.getFixtureName(), getConfigMethodType(testExecutionResult));
                break;
        }
    }

    @Subscribe
    @Priority(PRIORITY)
    public void beforeInvocation(BeforeMethodInvocationEvent event) {
        InvokedMethod method = event.getMethod();
        if (method.isConfigurationMethod()) {
            TestMethodExecutionResult testExecutionResult = event.getMethod().getTestMethodExecutionResult();
            TafAnnotationManager annotationManager = getTafAnnotationManager(testExecutionResult);
            ConfigMethodType configMethodType = getConfigMethodType(testExecutionResult);
            AllureFacade.startConfig(annotationManager.getFixtureName(), configMethodType);
        }
    }

    @Subscribe
    @Priority(PRIORITY)
    public void onTestCaseEvent(TestCaseEvent event) {
        TestMethodExecutionResult testExecutionResult = event.getTestExecutionResult();
        switch (event.getExecutionState()) {
            case STARTED:
                TestSuite suiteAnnotation = getSuiteAnnotation(testExecutionResult);
                if (suiteAnnotation == null) {
                    // regular test case
                    startTestCase(testExecutionResult, event.getTestExecutionContext());
                } else {
                    if (hasSuiteName(suiteAnnotation)) {
                        // test suite

                        AllureFacade.startSuite(suiteAnnotation.value());
                    } else {
                        // no suite name - skip test case and keep on current suite
                    }
                }
                break;
            case SUCCEEDED:
                fireFinishTest(testExecutionResult, true);
                break;
            case FAILED:
                fireFinishTest(testExecutionResult, false);
                break;
            case SKIPPED:
                if (!AllureFacade.isTestStarted(getTestName(testExecutionResult))) {
                    startTestCase(testExecutionResult, event.getTestExecutionContext());
                }

                AllureFacade.fireConfigEvents();

                Throwable throwable = testExecutionResult.getThrowable();
                if (throwable == null) {
                    throwable = latestThrowable.get();
                    latestThrowable.remove();
                }

                if (throwable == null) {
                    AllureFacade.skipTestCase(new TestSkipException("The test was skipped for some reason"));
                } else {
                    AllureFacade.failTestCase(throwable);
                }
                AllureFacade.finishTestCase();
                break;
        }
    }

    @Subscribe
    @Priority(PRIORITY)
    public void onTestGroupEvent(TestGroupEvent testGroupEvent) {
        String suiteName = testGroupEvent.getTestGroup().getId();
        switch (testGroupEvent.getExecutionPhase()) {
            case START:
                AllureFacade.startSuite(suiteName);
                TestExecutionHelper.setCurrentSuiteName(suiteName);
                break;
            case FINISH:
                AllureFacade.finishSuite(suiteName);
                TestExecutionHelper.setCurrentSuiteName(null);
                break;
        }
    }

    /**
     * For regular TestNG test
     */
    private void startTestCase(TestMethodExecutionResult testResult, TestExecutionContext testExecutionContext) {
        String suiteName = testExecutionContext.getTestGroup().getId();
        String testName = getTestName(testResult);
        TestMethod testMethod = testResult.getTestMethod();
        Method method = testMethod.getJavaMethod();
        Object[] parameters = testMethod.getParameters();

        AllureFacade.startTestCase(suiteName, testName, testName, new TestCaseBean(parameters, method));
    }

    private static String getTestName(TestMethodExecutionResult iTestResult) {
        TestMethod testMethod = iTestResult.getTestMethod();
        String testName = testMethod.getName();
        return ParametersManager.getParametrizedName(testName, testMethod.getParameters());
    }

    private TestSuite getSuiteAnnotation(TestMethodExecutionResult testResult) {
        TestMethod testMethod = testResult.getTestMethod();
        Method method = testMethod.getJavaMethod();
        Annotation[] annotations = method.getDeclaredAnnotations();
        TestSuite testSuite = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof TestSuite) {
                testSuite = (TestSuite) annotation;
            }
        }
        return testSuite;
    }

    private boolean hasSuiteName(TestSuite annotation) {
        return !annotation.value().equals(TestSuite.NULL);
    }

    private void fireFinishTest(TestMethodExecutionResult iTestResult, boolean passed) {
        TestSuite suiteAnnotation = getSuiteAnnotation(iTestResult);
        if (suiteAnnotation == null) {
            // regular test case
            if (!passed) {
                AllureFacade.failTestCase(iTestResult.getThrowable());
            }
            AllureFacade.finishTestCase();
        } else {
            if (hasSuiteName(suiteAnnotation)) {
                // test suite
                AllureFacade.finishSuite(suiteAnnotation.value());
            } else {
                // no suite name - keep on current suite
            }
            AllureFacade.addLabelsToAllure();
        }
    }

    public ConfigMethodType getConfigMethodType(TestMethodExecutionResult testResult) {
        TestMethod testMethod = testResult.getTestMethod();
        if (testMethod.isBeforeTestMethodConfiguration()) {
            return ConfigMethodType.BEFORE_TEST_METHOD;
        }
        if (testMethod.isBeforeTestClassConfiguration()) {
            return ConfigMethodType.BEFORE_CLASS;
        }
        if (testMethod.isBeforeTestGroupConfiguration()) {
            return ConfigMethodType.BEFORE_GROUPS;
        }
        if (testMethod.isAfterTestMethodConfiguration()) {
            return ConfigMethodType.AFTER_TEST;
        }
        if (testMethod.isAfterTestClassConfiguration()) {
            return ConfigMethodType.AFTER_CLASS;
        }
        if (testMethod.isAfterTestGroupConfiguration()) {
            return ConfigMethodType.AFTER_GROUPS;
        }
        return null;
    }

    private TafAnnotationManager getTafAnnotationManager(TestMethodExecutionResult testExecutionResult) {
        TafAnnotationManagerFactory tafAnnotationManagerFactory = ServiceRegistry.getTafAnnotationManagerFactory();
        return tafAnnotationManagerFactory.create(testExecutionResult);
    }

}
