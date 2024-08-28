package com.ericsson.cifwk.taf.api.scanner;

import com.ericsson.cifwk.meta.API;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.*;
import static java.lang.annotation.ElementType.*;

/**
 * This class enumerates different non-public APIs,
 * so there are always all types of black-lists in this module artifact in Nexus
 * independent on real code annotations (which might change with time or disappear at all).
 * <p>
 * It might be used for testing according FindBug rules.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.06.2016
 */
public class AnnotatedApiSamples {

    @API(Internal)
    public AnnotatedApiSamples(String internalConstructor) {
    }

    @API(Experimental)
    public AnnotatedApiSamples(StringBuffer experimentalConstructor) {
    }

    @API(Deprecated)
    public AnnotatedApiSamples(StringBuilder deprecatedConstructor) {
    }

    @Deprecated
    public AnnotatedApiSamples(CharSequence javaDeprecatedConstructor) {
    }

    @API(Internal)
    public void internalMethod() {
    }

    @API(Experimental)
    public void experimentalMethod(String someParameter) {
    }

    @API(Deprecated)
    public void deprecatedMethod(String... someParameters) {
    }

    @Deprecated
    public void javaDeprecatedMethod() {
    }

    @API(Internal)
    public static class InternalType {
        public void someMethod() {
        }
    }

    @API(Experimental)
    public static class ExperimentalType {
        public void someMethod() {
        }
    }

    @API(Deprecated)
    public static class DeprecatedType {
        public void someMethod() {
        }
    }

    @Deprecated
    public static class JavaDeprecatedType {
        public void someMethod() {
        }
    }

    @Target({TYPE, METHOD, FIELD, CONSTRUCTOR})
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @API(Internal)
    public @interface InternalAnnotation {
    }

    @Target({TYPE, METHOD, FIELD, CONSTRUCTOR})
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @API(Experimental)
    public @interface ExperimentalAnnotation {

    }

    @Target({TYPE, METHOD, FIELD, CONSTRUCTOR})
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @API(Deprecated)
    public @interface DeprecatedAnnotation {
    }

    @Target({TYPE, METHOD, CONSTRUCTOR})
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @Deprecated
    public @interface JavaDeprecatedAnnotation {
    }

}
