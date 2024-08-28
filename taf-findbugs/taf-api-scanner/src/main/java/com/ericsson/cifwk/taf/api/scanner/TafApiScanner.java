package com.ericsson.cifwk.taf.api.scanner;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Experimental;
import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 07.06.2016
 */
class TafApiScanner {

    private static final String PACKAGE_TO_SCAN = "com.ericsson";

    public AnnotatedApi scan() {

        Reflections reflections = new Reflections(PACKAGE_TO_SCAN,
                new TypeAnnotationsScanner(),
                new MethodAnnotationsScanner()
        );



        // Type Annotations
        Set<Class<?>> annotatedTypes = reflections.getTypesAnnotatedWith(API.class);
        List<Class<?>> deprecatedClasses = newArrayList(filter(annotatedTypes, ApiPredicate.DEPRECATED_CLASS));
        List<Class<?>> internalClasses = filter(annotatedTypes, ApiPredicate.INTERNAL_CLASS);
        List<Class<?>> experimentalClasses = filter(annotatedTypes, ApiPredicate.EXPERIMENTAL_CLASS);

        // Java Deprecated types
        Collection<Class<?>> javaDeprecatedClasses = filterNull(reflections.getTypesAnnotatedWith(Deprecated.class));
        deprecatedClasses.addAll(javaDeprecatedClasses);

        // Method Annotations
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(API.class);
        List<Method> deprecatedMethods = filter(annotatedMethods, ApiPredicate.DEPRECATED_METHOD);
        List<Method> internalMethods = filter(annotatedMethods, ApiPredicate.INTERNAL_METHOD);
        List<Method> experimentalMethods = filter(annotatedMethods, ApiPredicate.EXPERIMENTAL_METHOD);

        // Java Deprecated methods
        Collection<Method> javaDeprecatedMethods = filterNull(reflections.getMethodsAnnotatedWith(Deprecated.class));
        System.out.println(javaDeprecatedMethods);
        deprecatedMethods.addAll(javaDeprecatedMethods);

        // Constructor Annotations

        // TODO: upgrade org.reflections (>0.9.10) once recursive annotations issue is fixed (https://github.com/ronmamo/reflections/issues/139)
//        Set<Constructor> annotatedConstructors = reflections.getConstructorsAnnotatedWith(API.class);
        Set<Constructor> annotatedConstructors = Sets.newHashSet();

        List<Constructor> deprecatedConstructors = filter(annotatedConstructors, ApiPredicate.DEPRECATED_CONSTRUCTOR);
        List<Constructor> internalConstructors = filter(annotatedConstructors, ApiPredicate.INTERNAL_CONSTRUCTOR);
        List<Constructor> experimentalConstructors = filter(annotatedConstructors, ApiPredicate.EXPERIMENTAL_CONSTRUCTOR);

        // Java Deprecated constructors
        // TODO: upgrade org.reflections (>0.9.10) once recursive annotations issue is fixed (https://github.com/ronmamo/reflections/issues/139)
//        Collection<Constructor> javaDeprecatedConstructors = filterNull(reflections.getConstructorsAnnotatedWith(Deprecated.class));
        Set<Constructor> javaDeprecatedConstructors = Sets.newHashSet();
        deprecatedConstructors.addAll(javaDeprecatedConstructors);

        return new AnnotatedApi()
                .setClasses(deprecatedClasses, Deprecated)
                .setClasses(internalClasses, Internal)
                .setClasses(experimentalClasses, Experimental)
                .setMethods(deprecatedMethods, Deprecated)
                .setMethods(internalMethods, Internal)
                .setMethods(experimentalMethods, Experimental)
                .setConstructors(deprecatedConstructors, Deprecated)
                .setConstructors(internalConstructors, Internal)
                .setConstructors(experimentalConstructors, Experimental);
    }

    private <E> List<E> filter(Collection<E> unfiltered, Predicate<? super E> predicate) {
        return newArrayList(com.google.common.collect.Collections2.filter(unfiltered, predicate));
    }

    private <E> Collection<E> filterNull(Collection<E> unfiltered) {
        return com.google.common.collect.Collections2.filter(unfiltered, new Predicate<E>() {
            @Override
            public boolean apply(E input) {
                return input != null;
            }
        });
    }

}
