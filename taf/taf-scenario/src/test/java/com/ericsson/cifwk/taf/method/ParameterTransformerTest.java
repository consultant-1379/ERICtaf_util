package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.annotations.OptionalValue;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.ericsson.cifwk.taf.method.ParameterTransformer.hasDefaultValue;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.05.2016
 */
public class ParameterTransformerTest {

    @Test
    public void defaultValueIsDefined() throws Exception {
        assertThat(hasDefaultValue(null)).isFalse();
        assertThat(isDefaultValueDefined("withoutDefaultValue")).isFalse();
        assertThat(isDefaultValueDefined("withDefaultValue")).isTrue();
        assertThat(isDefaultValueDefined("withEmptyDefaultValue")).isTrue();
    }

    @SuppressWarnings("unused")
    private static void withoutDefaultValue(@OptionalValue String parameter) {
        // empty
    }

    @SuppressWarnings("unused")
    private static void withDefaultValue(@OptionalValue("defaultValue") String parameter) {
        // empty
    }

    @SuppressWarnings("unused")
    private static void withEmptyDefaultValue(@OptionalValue("") String parameter) {
        // empty
    }

    private boolean isDefaultValueDefined(String methodName) throws NoSuchMethodException {
        Method method = ParameterTransformerTest.class.getDeclaredMethod(methodName, String.class);
        return hasDefaultValue((OptionalValue) method.getParameterAnnotations()[0][0]);
    }
}