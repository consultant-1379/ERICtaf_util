package com.ericsson.cifwk.taf.operators;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.configuration.TafProperty;

import eu.infomas.annotation.AnnotationDetector;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Class is responsible for getting all classes which contain
 * taf annotations on class and field level.
 * Currently detected annotations:
 * {@link com.ericsson.cifwk.taf.configuration.TafProperty}
 * {@link com.ericsson.cifwk.taf.configuration.TafHost}
 * {@link com.ericsson.cifwk.taf.annotations.Operator}
 *
 * It searches the class path for all classes which start at the
 * base package level = "com.ericsson".
 */
@API(Internal)
public final class ConfigurationAnnoatationScanner {

    public static final String BASE_PCKG = "com.ericsson";

    public Set<Class> findClassesByAnnotation() {
        final Set<Class> classes = new HashSet<>();

        AnnotationDetector operator = getOperatorAnnotationDetector(classes);
        AnnotationDetector field = getFieldAnnotationDetector(classes);

        try {
            operator.detect(BASE_PCKG);
            field.detect(BASE_PCKG);
        } catch (IOException e) {
            throw new RuntimeException(e); // NOSONAR
        }
        return classes;
    }

    private AnnotationDetector getFieldAnnotationDetector(final Set<Class> classes) {
        return new AnnotationDetector(new AnnotationDetector.FieldReporter() {
            @Override
            public Class<? extends Annotation>[] annotations() {
                return new Class[]{TafProperty.class, TafHost.class};
            }

            @Override
            public void reportFieldAnnotation(final Class<? extends Annotation> annotation, final String className,
                    final String fieldName) {
                ClassLoader classLoader = getClass().getClassLoader();
                Class<?> klass;
                try {
                    klass = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e); // NOSONAR
                }
                classes.add(klass);
            }
        });
    }

    private AnnotationDetector getOperatorAnnotationDetector(final Set<Class> classes) {
        return new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public Class<Annotation>[] annotations() {
                    return new Class[]{Operator.class};
                }

                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    ClassLoader classLoader = getClass().getClassLoader();
                    Class<?> klass;
                    try {
                        klass = classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e); // NOSONAR
                    }
                    classes.add(klass);
                }
            });
    }
}
