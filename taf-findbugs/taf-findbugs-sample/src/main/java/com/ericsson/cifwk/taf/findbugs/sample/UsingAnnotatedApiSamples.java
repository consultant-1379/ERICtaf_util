package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.api.scanner.AnnotatedApiSamples;
import com.ericsson.cifwk.taf.api.scanner.AnnotatedApiSamples.InternalType;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.06.2016
 */
public class UsingAnnotatedApiSamples {

    // gets reported
    private void usingInternalTypeConstructor() {
        new InternalType();
    }

    // gets reported
    private void usingExperimentalTypeConstructor() {
        new AnnotatedApiSamples.ExperimentalType();
    }

    // gets reported
    private void usingDeprecatedTypeConstructor() {
        new AnnotatedApiSamples.DeprecatedType();
    }

    // gets reported
    private void usingInternalTypeMethod() {
        InternalType internal = null;
        internal.someMethod();
    }

    // SHOULD NOT get reported
    private void usingInternalSubTypeMethod() {
        InternalSubType internal = null;
        internal.someMethod();
    }

    // gets reported
    private void usingDeprecatedTypeMethod() {
        AnnotatedApiSamples.DeprecatedType deprecated = null;
        deprecated.someMethod();
    }

    // gets reported
    private void usingExperimentalTypeMethod() {
        AnnotatedApiSamples.ExperimentalType experimental = null;
        experimental.someMethod();
    }

    private void usingInternalConstructor() {
        new AnnotatedApiSamples("Internal constructor");
    }

    private void usingDeprecatedConstructor() {
        new AnnotatedApiSamples(new StringBuilder("Deprecated constructor"));
    }

    private void usingExperimentalConstructor() {
        new AnnotatedApiSamples(new StringBuffer("Experimental constructor"));
    }

    // gets reported
    private void usingInternalMethod() {
        AnnotatedApiSamples instance = null;
        instance.internalMethod();
    }

    // gets reported
    private void usingDeprecatedMethod() {
        AnnotatedApiSamples instance = null;
        instance.deprecatedMethod();
    }

    // gets reported
    private void usingExperimentalMethod() {
        AnnotatedApiSamples instance = null;
        instance.experimentalMethod("");
    }

    // TODO: use internal method in subclass of public API class - should get reported
    // TODO: use polymorphic methods - should NOT get reported (check method parameters)

    // gets reported
    private static class ExtendingInternalType extends InternalType {
    }

    // gets reported
    private static class ExtendingDeprecatedType extends AnnotatedApiSamples.DeprecatedType {
    }

    // gets reported
    private static class ExtendingExperimentalType extends AnnotatedApiSamples.ExperimentalType {
    }

    // TODO: check usage of static fields in internal types

    @API(Stable)
    private static class InternalSubType extends InternalType {

        @Override
        public void someMethod() {
            super.someMethod();
        }

    }

}
