package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;

import static com.ericsson.cifwk.taf.configuration.TafConfigurationProvider.provide;
import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;

/**
 * Class is responsible for getting TAF property,
 * falling back to default value as well as
 * converting it to proper type.
 */
class PropertyProvider {

    private static final String CONVERSION_ERROR = "Unable to convert value '%s' to type %s%n" +
            "Please correct injection field type, property key or property definition.";

    private static final String DEFAULT_VALUE_CONVERSION_ERROR = "Unable to convert default value '%s' to type %s%n" +
            "Please correct injection field type or property default value.";

    private static final String PROPERTY_NOT_FOUND = "No property found by key '%s' (and default value was not provided)%n" +
            "Properties available: %s";

    private final Class targetType;

    private final TafProperty tafProperty;

    private final ConvertUtilsBean converter;

    public PropertyProvider(Class targetType, TafProperty tafProperty) {
        this.targetType = targetType;
        this.tafProperty = tafProperty;

        // Apache ConvertUtils do not throw exceptions - have to configure my own converter
        converter = new ConvertUtilsBean();
        converter.register(true, false, 0);
    }

    public Object get() {

        // converting both default (eagerly) and property values
        Object defaultValue = getDefaultValue();
        Object value = getPropertyValue();
        ensurePropertyFound(defaultValue, value);

        // overriding value with default one
        return firstNonNull(value, defaultValue);
    }

    public void ensurePropertyFound(Object defaultValue, Object value) {
        String key = tafProperty.value();
        if (value == null && defaultValue == null) {
            // neither property defined nor default value provided
            String allProperties = provide().getProperties() + "";
            throw new IllegalStateException(format(PROPERTY_NOT_FOUND, key, allProperties));
        }
    }

    public Object getDefaultValue() {
        String defaultValueAsString = tafProperty.defaultValue();
        boolean defaultValueProvided = isAnnotationValueSet(defaultValueAsString);
        if (defaultValueProvided) {
            return tryToConvert(defaultValueAsString, targetType, DEFAULT_VALUE_CONVERSION_ERROR);
        }
        return null;
    }

    public Object getPropertyValue() {
        String key = tafProperty.value();
        Object rawValue = provide().getProperty(key);
        if (rawValue != null) {
            return tryToConvert(rawValue, targetType, CONVERSION_ERROR);
        }
        return null;
    }

    private Object tryToConvert(Object value, Class targetType, String errorMessagePattern) {
        try {
            return converter.convert(value, targetType);
        } catch (ConversionException e) {
            String targetTypeName = targetType.getCanonicalName();
            String valueAsString = value.toString();
            throw new IllegalStateException(format(errorMessagePattern, valueAsString, targetTypeName), e);
        }
    }

    @VisibleForTesting
    static boolean isAnnotationValueSet(String tafProperty) {
        return !tafProperty.equals(TafProperty.NOT_SET);
    }

}
