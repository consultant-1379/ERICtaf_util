package com.ericsson.cifwk.taf.api.scanner;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Experimental;
import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.api.scanner.ApiPredicate.ApiAnnotationSources.CLASS;
import static com.ericsson.cifwk.taf.api.scanner.ApiPredicate.ApiAnnotationSources.CONSTRUCTOR;
import static com.ericsson.cifwk.taf.api.scanner.ApiPredicate.ApiAnnotationSources.METHOD;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;
import com.google.common.base.Predicate;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.06.2016
 */
class ApiPredicate<T> implements Predicate<T> {

    public static final ApiPredicate<Class> DEPRECATED_CLASS = new ApiPredicate<>(Deprecated, CLASS);

    public static final ApiPredicate<Class> INTERNAL_CLASS = new ApiPredicate<>(Internal, CLASS);

    public static final ApiPredicate<Class> EXPERIMENTAL_CLASS = new ApiPredicate<>(Experimental, CLASS);

    public static final ApiPredicate<Method> DEPRECATED_METHOD = new ApiPredicate<>(Deprecated, METHOD);

    public static final ApiPredicate<Method> INTERNAL_METHOD = new ApiPredicate<>(Internal, METHOD);

    public static final ApiPredicate<Method> EXPERIMENTAL_METHOD = new ApiPredicate<>(Experimental, METHOD);

    public static final ApiPredicate<Constructor> DEPRECATED_CONSTRUCTOR = new ApiPredicate<>(Deprecated, CONSTRUCTOR);

    public static final ApiPredicate<Constructor> INTERNAL_CONSTRUCTOR = new ApiPredicate<>(Internal, CONSTRUCTOR);

    public static final ApiPredicate<Constructor> EXPERIMENTAL_CONSTRUCTOR = new ApiPredicate<>(Experimental, CONSTRUCTOR);

    private Quality apiQuality;

    private ApiAnnotationSource<T> apiAnnotationSource;

    private ApiPredicate(Quality apiQuality, ApiAnnotationSource<T> apiAnnotationSource) {
        this.apiQuality = apiQuality;
        this.apiAnnotationSource = apiAnnotationSource;
    }

    @Override
    public boolean apply(T input) {
        return apiQuality.equals(apiAnnotationSource.getApi(input));
    }

    private interface ApiAnnotationSource<T> {
        Quality getApi(T annotationSource);
    }

    public static class ApiAnnotationSources {

        public static final ApiAnnotationSource<Class> CLASS = new ApiAnnotationSource<Class>() {
            @Override
            public Quality getApi(Class annotationSource) {
                API annotation = (API) annotationSource.getAnnotation(API.class);
                if (annotation == null) {
                    throw new ApiAnnotationNotFoundException(annotationSource.getName());
                }
                return annotation.value();
            }
        };

        public static final ApiAnnotationSource<Method> METHOD = new ApiAnnotationSource<Method>() {
            @Override
            public Quality getApi(Method annotationSource) {
                if (annotationSource == null) {
                    return null;
                }
                return annotationSource.getAnnotation(API.class).value();
            }
        };

        public static final ApiAnnotationSource<Constructor> CONSTRUCTOR = new ApiAnnotationSource<Constructor>() {
            @Override
            public Quality getApi(Constructor annotationSource) {
                return ((API) annotationSource.getAnnotation(API.class)).value();
            }
        };

    }

    public static class ApiAnnotationNotFoundException extends RuntimeException {
        public ApiAnnotationNotFoundException(String className) {
            super(String.format("Something went wrong with %s class. " +
                    "API annotation couldn't be retrieved. ", className));
        }
    }
}
