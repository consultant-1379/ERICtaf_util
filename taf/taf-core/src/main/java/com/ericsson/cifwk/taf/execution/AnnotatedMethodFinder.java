/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.taf.annotations.TestStep;

import java.lang.reflect.Method;

public class AnnotatedMethodFinder {

    private AnnotatedMethodFinder(){
        throw new IllegalAccessError("AnnotatedMethodFinder class");
    }

    public static Method findTestStepAnnotatedMethod(Object instance, String testStep) {
        Class<?> type = instance.getClass();
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestStep.class)) {
                TestStep annotation = method.getAnnotation(TestStep.class);
                if (annotation.id().equals(testStep)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Method annotated with @TestStep(id = \""
                + testStep + "\") not found for class " + instance.getClass().getSimpleName() + "!");
    }
}
