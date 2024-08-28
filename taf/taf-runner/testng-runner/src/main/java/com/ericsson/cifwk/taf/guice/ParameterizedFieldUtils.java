package com.ericsson.cifwk.taf.guice;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getLocation;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.logMessageAndThrowException;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 06.06.2017
 */
public class ParameterizedFieldUtils {

    private static final String COLLECTION_WRONG_PARAMETER = "%s: collection expected parameter is %s type but was %s";

    private static final String COLLECTION_NO_PARAMETER = "%s: collection is not parametrized";

    private static final String PROVIDER_NO_PARAMETER = "%s: provider is not parametrized";

    public static Class getProviderParameter(Field field) {
        return getFirstParameter(field, PROVIDER_NO_PARAMETER);
    }

    public static void checkCollectionGenericType(Field field, Class<?> allowedParameterType) {
        Class<?> parameterType = getFirstParameter(field, COLLECTION_NO_PARAMETER);

        if (!parameterType.isAssignableFrom(allowedParameterType)) {
            String expectedClass = allowedParameterType.getCanonicalName();
            String location = getLocation(field);
            String parameterTypeName = parameterType.getCanonicalName();
            throw logMessageAndThrowException(COLLECTION_WRONG_PARAMETER, location, expectedClass, parameterTypeName);
        }
    }

    private static Class getFirstParameter(Field field, String errorMessagePattern) {
        Type genericType = field.getGenericType();
        String location = getLocation(field);
        if (!(genericType instanceof ParameterizedType)) {
            throw logMessageAndThrowException(errorMessagePattern, location);
        }
        ParameterizedType type = (ParameterizedType) genericType;
        Type typeArgument = type.getActualTypeArguments()[0];
        if (!(typeArgument instanceof Class)) {
            throw logMessageAndThrowException(errorMessagePattern, location);
        }

        return (Class) typeArgument;
    }

}
