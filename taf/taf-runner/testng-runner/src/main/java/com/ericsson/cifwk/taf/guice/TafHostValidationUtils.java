package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.ericsson.cifwk.taf.data.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * Created by ekongla on 18/05/2017.
 */
class TafHostValidationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TafHostValidationUtils.class);

    private static final String LOCATION = "Class %s (field '%s')";

    private TafHostValidationUtils() {
    }

    static boolean isHostTarget(Field field) {
        return isHost(field) || isList(field) || isSetCollection(field) || isCollection(field);
    }

    static boolean isList(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    static boolean isSetCollection(Field field) {
        return Set.class.isAssignableFrom(field.getType());
    }

    static boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    static boolean isHost(Field field) {
        return Host.class.isAssignableFrom(field.getType());
    }

    static boolean isHostAnnotationPresent(Field field) {
        return field.isAnnotationPresent(TafHost.class);
    }

    static String getLocation(Field field) {
        String className = field.getDeclaringClass().getCanonicalName();
        String fieldName = field.getName();
        return format(LOCATION, className, fieldName);
    }

    static RuntimeException logMessageAndThrowException(String pattern, String... parameters) {
        String message = format(pattern, (Object[]) parameters);
        LOG.error(message);
        throw new IllegalStateException(message);
    }

    static RuntimeException logMessageAndThrowException(String pattern, Exception e, String... parameters) {
        String message = format(pattern, (Object[]) parameters);
        IllegalStateException exception = new IllegalStateException(message, e);
        LOG.error(message, exception);
        throw exception;
    }

    static TafProperty getTafPropertyAnnotation(Field field) {
        return getAnnotation(field, TafProperty.class);
    }

    static TafHost getTafHostAnnotation(Field field) {
        return getAnnotation(field, TafHost.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAnnotation(Field field, Class<T> annotationType) {
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            if (annotationType == annotation.annotationType()) {
                return (T) annotation;
            }
        }
        return null;
    }

}
