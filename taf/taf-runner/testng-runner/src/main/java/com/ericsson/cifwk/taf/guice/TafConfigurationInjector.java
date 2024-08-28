package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.google.inject.MembersInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static com.ericsson.cifwk.taf.guice.ParameterizedFieldUtils.getProviderParameter;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getLocation;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getTafPropertyAnnotation;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.logMessageAndThrowException;

class TafConfigurationInjector<T> implements MembersInjector<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TafConfigurationInjector.class);

    static final String UNABLE_TO_INJECT = "Unable to inject property in %s:";

    private static final String NOT_ASSIGNABLE = UNABLE_TO_INJECT +
            "Field type %s is not assignable to value of type %s";

    private final MembersInjector<T> fieldInjector;

    TafConfigurationInjector(Field field) {
        if (isGoogleProvider(field)) {
            fieldInjector = new GoogleProviderInjector<>(field);
        } else if (isJavaxProvider(field)) {
            fieldInjector = new JavaxProviderInjector<>(field);
        } else {
            fieldInjector = new FieldValueInjector<>(field);
        }
    }

    @Override
    public void injectMembers(T instance) {
        fieldInjector.injectMembers(instance);
    }

    private static boolean isGoogleProvider(Field field) {
        return com.google.inject.Provider.class.equals(field.getType());
    }

    private static boolean isJavaxProvider(Field field) {
        return javax.inject.Provider.class.equals(field.getType());
    }

    private static abstract class AbstractFieldValueInjector<T> implements MembersInjector<T> {

        protected final Field target;

        protected final PropertyProvider propertyProvider;

        public AbstractFieldValueInjector(Field target) {
            this.target = target;
            target.setAccessible(true);
            TafProperty tafProperty = getTafPropertyAnnotation(target);
            this.propertyProvider = new PropertyProvider(getExpectedValueType(), tafProperty);
        }

        public abstract Class getExpectedValueType();

        public abstract void injectMembersInternally(T instance) throws IllegalAccessException;

        @Override
        public void injectMembers(T instance) {
            try {
                injectMembersInternally(instance);
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        protected Object checkAssignable(Object value) {
            Class valueType = value.getClass();
            Class<?> expectedValueType = getExpectedValueType();
            if (!expectedValueType.isPrimitive() && !expectedValueType.isAssignableFrom(valueType)) {
                String location = getLocation(target);
                String targetTypeName = expectedValueType.getCanonicalName();
                String valueTypeName = valueType.getCanonicalName();
                throw logMessageAndThrowException(NOT_ASSIGNABLE, location, targetTypeName, valueTypeName);
            }
            return value;
        }

        protected Object getValue() {
            try {
                return checkAssignable(propertyProvider.get());
            } catch (IllegalStateException e) {
                String location = getLocation(target);
                throw logMessageAndThrowException(UNABLE_TO_INJECT, e, location);
            }
        }

    }

    private static class FieldValueInjector<T> extends AbstractFieldValueInjector<T> {

        public FieldValueInjector(Field target) {
            super(target);
        }

        @Override
        public Class getExpectedValueType() {
            return target.getType();
        }

        @Override
        public void injectMembersInternally(Object instance) throws IllegalAccessException {
            target.set(instance, getValue());
        }

    }

    private static class GoogleProviderInjector<T> extends AbstractFieldValueInjector<T> {

        public GoogleProviderInjector(Field target) {
            super(target);
        }

        @Override
        public Class getExpectedValueType() {
            return getProviderParameter(target);
        }

        @Override
        public void injectMembersInternally(Object instance) throws IllegalAccessException {
            com.google.inject.Provider provider = new com.google.inject.Provider() {
                @Override
                public Object get() {
                    return getValue();
                }
            };
            target.set(instance, provider);
        }

    }

    private static class JavaxProviderInjector<T> extends AbstractFieldValueInjector<T> {

        public JavaxProviderInjector(Field target) {
            super(target);
        }

        @Override
        public Class getExpectedValueType() {
            return getProviderParameter(target);
        }

        @Override
        public void injectMembersInternally(Object instance) throws IllegalAccessException {
            javax.inject.Provider provider = new javax.inject.Provider() {
                @Override
                public Object get() {
                    return getValue();
                }
            };
            target.set(instance, provider);
        }

    }

}
