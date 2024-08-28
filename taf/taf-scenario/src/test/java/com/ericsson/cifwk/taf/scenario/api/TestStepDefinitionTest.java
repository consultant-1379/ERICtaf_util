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
package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.method.Parameter;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStepDefinitionTest {
    
    private static List<Parameter> parameters = new ArrayList<>();
    private static Parameter parameter = mock(Parameter.class);
    private static Annotation annotation = new Annotation() {
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }
    };

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoAnnotationsOnTestStepVariables(){
        when(parameter.getAnnotation(Input.class)).thenReturn(null);
        when(parameter.getAnnotation(Output.class)).thenReturn(null);
        parameters.add(parameter);
        TestStepDefinition.checkParametersForAnnotations("method", parameters);
        parameters.clear();
    }
    
    @Test()
    public void shouldNotThrowExceptionWhenAnnotationOnTestStepVariable(){
        when(parameter.getAnnotation(Input.class)).thenReturn(null);
        when(parameter.getAnnotation(Output.class)).thenReturn(annotation);
        parameters.add(parameter);
        TestStepDefinition.checkParametersForAnnotations("method", parameters);
        parameters.clear();
    }
}
