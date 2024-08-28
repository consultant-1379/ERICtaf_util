package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.api.scanner.AnnotatedApiSamples;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.06.2016
 */
public class UsingAnnotatedAnnotations {

    /*
     * TYPE
     */

    @AnnotatedApiSamples.InternalAnnotation
    private static class AnnotatedInternalType {
    }

    @AnnotatedApiSamples.DeprecatedAnnotation
    private static class AnnotatedDeprecatedType {
    }

    @AnnotatedApiSamples.ExperimentalAnnotation
    private static class AnnotatedExperimentalType {
    }

    /*
     * FIELD
     */

    @AnnotatedApiSamples.InternalAnnotation
    private String annotatedInternalField;

    @AnnotatedApiSamples.DeprecatedAnnotation
    private String annotatedDeprecatedField;

    @AnnotatedApiSamples.ExperimentalAnnotation
    private String annotatedExperimentalField;

    /*
     * METHOD
     */

    @AnnotatedApiSamples.InternalAnnotation
    private void usingInternalAnnotation() {
    }

    @AnnotatedApiSamples.DeprecatedAnnotation
    private void usingDeprecatedAnnotation() {
    }

    @AnnotatedApiSamples.ExperimentalAnnotation
    private void usingExperimentalAnnotation() {
    }

    /*
     * PARAMETER
     */

    /*
     * CONSTRUCTOR
     */

    @AnnotatedApiSamples.InternalAnnotation
    private UsingAnnotatedAnnotations(String internal) {

    }

    @AnnotatedApiSamples.DeprecatedAnnotation
    private UsingAnnotatedAnnotations(StringBuilder deprecated) {

    }

    @AnnotatedApiSamples.ExperimentalAnnotation
    private UsingAnnotatedAnnotations(StringBuffer experimental) {

    }

    /*
     * LOCAL_VARIABLE
     */

    /*
     * ANNOTATION_TYPE
     */

    /*
     * PACKAGE
     */

    /*
     * TYPE_PARAMETER
     */

    /*
     * TYPE_USE
     */

}
