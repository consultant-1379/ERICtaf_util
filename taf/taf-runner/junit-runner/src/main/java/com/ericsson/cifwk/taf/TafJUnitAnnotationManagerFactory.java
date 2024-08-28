package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
public class TafJUnitAnnotationManagerFactory implements TafAnnotationManagerFactory {

    @Override
    public TafAnnotationManager create(TestMethodExecutionResult testMethodExecutionResult) {

        // TODO: cast to JUnit impl and get following
        Class testClass = null;
        String name = "";
        Annotation[] annotations = new Annotation[]{};

        Description description = Description.createTestDescription(testClass, name, annotations);
        return new TafJUnitAnnotationManager(description);
    }

    @Override
    public TafAnnotationManager create(Method method, Object[] parameters) {
        throw new UnsupportedOperationException("JUnit Description is required");
    }
}
