package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;

import java.lang.annotation.Annotation;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
@API(Internal)
public interface TafAnnotationManager {

    String getTestCaseTitle();

    String getTestId();

    boolean isSetUpMethod();

    boolean isTearDownMethod();

    String getFixtureName();

    boolean isTestIdAnnotationPresent();

    Annotation getFixtureAnnotation();

    Annotation getSetUpAnnotation();

    Annotation getTearDownAnnotation();

    <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType);

    Annotation getAnyAnnotation(Class<? extends Annotation>... annotationTypes);

    <T extends Annotation> T getAnyAnnotation(Class<T> annotationType);
}
