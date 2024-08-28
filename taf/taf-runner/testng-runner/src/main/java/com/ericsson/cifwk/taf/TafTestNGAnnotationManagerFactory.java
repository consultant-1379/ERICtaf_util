package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResultImpl;

import java.lang.reflect.Method;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
public class TafTestNGAnnotationManagerFactory implements TafAnnotationManagerFactory {

    @Override
    public TafAnnotationManager create(TestMethodExecutionResult testMethodExecutionResult) {
        TestMethodExecutionResultImpl testNgTestResult = (TestMethodExecutionResultImpl) testMethodExecutionResult;
        return new TafTestNGAnnotationManager(testNgTestResult.getTestResult());
    }

    @Override
    public TafAnnotationManager create(Method method, Object[] parameters) {
        return new TafTestNGAnnotationManager(method, parameters);
    }
}
