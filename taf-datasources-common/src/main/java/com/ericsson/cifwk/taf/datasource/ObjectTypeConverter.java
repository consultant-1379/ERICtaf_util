package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.ConvertUtilsConfiguration;
import com.ericsson.cifwk.taf.configuration.KeySubstitutor;
import com.google.common.base.Optional;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/03/2016
 */
@API(Internal)
public final class ObjectTypeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypeConverter.class);

    private ObjectTypeConverter() {
    }

    public static <T> T map(Object value, Class<T> type) {
        if (value instanceof BeanPropertyContainer) {
            Map<String, Object> properties = ((BeanPropertyContainer) value)
                    .getProperties();
            return convertToBean(properties, type);
        } else if(value instanceof Map){
            return convertMap((Map<Object, Object>)value, type);
        } else if(type.isEnum()) {
            return convertEnum(value,type);
        } else {
            return convertValue(value, type);
        }
    }

    private static <T> T convertMap(final Map<Object, Object> map, final Class<T> type) {
        Map<Object, Object> converted = new HashMap<>(map);
        for(Map.Entry entry : map.entrySet()){
            Object value = entry.getValue();
            if(KeySubstitutor.stringValueContainsKeyIdentifier(value)){
                Object substitutedValue = KeySubstitutor.replaceKeyWithValue(value);
                converted.put(entry.getKey(), substitutedValue);
            }
        }
        return (T) ConvertUtils.convert(converted, type);
    }

    private static <T> T convertEnum(final Object value, final Class type) {
        T instance;
        try {
            if(value == null)
                return null;
            instance = (T) Enum.valueOf(type, value.toString());
        } catch (IllegalArgumentException e){
            throw e;
        }
        return instance;
    }

    private static <T> T convertValue(Object value, Class<T> type) {
        try {
            ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
            if(KeySubstitutor.stringValueContainsKeyIdentifier(value)) {
                String substitutedValue = KeySubstitutor.replaceKeyWithValue(value);
                return (T) ConvertUtils.convert(substitutedValue, type);
            } else {
                return (T) ConvertUtils.convert(value, type);
            }
        } catch (ConversionException e){
            LOGGER.warn("Error converting value from CSV. Value has been taken as NULL");
            LOGGER.debug("Exception is: ", e);
            return null;
        }
    }

    private static <T> T convertToBean(Map value, Class<T> type) {
        T instance;
        try {
            instance = type.newInstance();
            BeanUtils.populate(instance, value);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }
}
