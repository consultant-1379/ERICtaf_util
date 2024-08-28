package com.ericsson.cifwk.taf.api.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Experimental;
import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Collections2.transform;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
public class TafApiScannerTest {

    @Test
    public void internalClasses() {
        AnnotatedApi annotatedApi = new TafApiScanner().scan();
        assertThat(annotatedApi.getClasses(Internal))
                .contains(
                        AnnotatedApiSamples.InternalType.class,
                        AnnotatedApiSamples.InternalAnnotation.class);

        assertThat(annotatedApi.getClasses(Deprecated))
                .contains(
                        AnnotatedApiSamples.DeprecatedType.class,
                        AnnotatedApiSamples.DeprecatedAnnotation.class,
                        AnnotatedApiSamples.JavaDeprecatedType.class,
                        AnnotatedApiSamples.JavaDeprecatedAnnotation.class);

        assertThat(annotatedApi.getClasses(Experimental))
                .contains(
                        AnnotatedApiSamples.ExperimentalType.class,
                        AnnotatedApiSamples.ExperimentalAnnotation.class);

        assertMethods(annotatedApi.getMethods(Deprecated), "deprecatedMethod", "javaDeprecatedMethod");
        assertMethods(annotatedApi.getMethods(Internal), "internalMethod");
        assertMethods(annotatedApi.getMethods(Experimental), "experimentalMethod");
    }

    private void assertMethods(List<Method> actualMethods, String... expectedMethodNames) {
        Collection<String> actualMethodNames = transform(actualMethods, new Function<Method, String>() {
            @Override
            public String apply(Method method) {
                return method.getName();
            }
        });
        assertThat(actualMethodNames).containsExactlyInAnyOrder(expectedMethodNames);
    }

}
