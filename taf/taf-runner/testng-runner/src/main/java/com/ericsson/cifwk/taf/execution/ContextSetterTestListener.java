/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.management.TafContext;
import com.ericsson.cifwk.taf.management.TafExecutionAttributes;
import com.ericsson.cifwk.taf.testapi.TestMethod;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * This class is called before a TC Method is invoked. Using this class, the values of Current VUser and Context is set for the Class
 * based on the parameter values for the TC
 * @author qshatus
 *
 */
public class ContextSetterTestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextSetterTestListener.class);
    public static final int CONTEXT_SETTER_PRIORITY = 50;

    /**
     * This method is called before invoking a TC Method. It calls methods to set invocationCounterMap and parameterHashMap. It also
     * sets the current Vuser and Context values for the TC Method.
     */
    @Subscribe
    @Priority(CONTEXT_SETTER_PRIORITY)
    public void onTestStart(TestCaseStartedEvent event) {
        TestMethodExecutionResult testExecutionResult = event.getTestExecutionResult();
        TestMethod testMethod = testExecutionResult.getTestMethod();
        LOGGER.debug("Processing method " + testMethod.getName());
        Method nativeMethod = testMethod.getJavaMethod();

        List<List> matrix = TafContext.getMethodAttributes().get(nativeMethod);
        List values;
        if (matrix != null && !matrix.isEmpty()) {
            int size = matrix.size();
            // Due to DataProviders there could be multiple loops of VUser and Context combinations
            values = matrix.get(getInvocationCount(testMethod) % size);
        } else {
            values = singletonList(TestNGAnnotationTransformer.DEFAULT_VUSER);
        }
        updateDataHandler(values);
    }

    private void updateDataHandler(List values) {
        TafExecutionAttributes attr = TafContext.getRuntimeAttributes();
        TafExecutionAttributes parentAttr = TafContext.getParentRuntimeAttributes();
        LOGGER.trace("Setting TafContext to " + values);
        attr.setAttribute(TafContext.VUSER, values.get(0));
        parentAttr.setAttribute(TafContext.VUSER, values.get(0));
    }

    /**
     * This method is pure hack around testng behaviour of doubling invocation count for methods that are using timeouts.
     * It's ugly and should be eliminated.
     */
    private int getInvocationCount(TestMethod method) {
        int invocationCount = method.getCurrentInvocationCount();
        if (method.getJavaMethod().getAnnotation(Test.class).timeOut() > 0) {
            invocationCount = invocationCount / 2;
        }
        return invocationCount;
    }

}
