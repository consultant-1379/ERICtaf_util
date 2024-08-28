package com.ericsson.cifwk.taf.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.configuration.ConvertUtilsConfiguration;
import com.ericsson.cifwk.taf.configuration.KeySubstitutor;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Defaults;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("ALL")
public final class ParameterTransformer {

    public static final Logger LOGGER = LoggerFactory.getLogger(ParameterTransformer.class);

    private ParameterTransformer() {
    }

    public static Iterator<Object[]> transform(final LinkedHashMap<String, Iterator<DataRecord>> dataSources, Method method) {
        final List<Parameter> parameters = Parameter.parametersFor(method);
        JoinedIterator<DataRecord> joinedIterator = JoinedIterator.join(Lists.newArrayList(dataSources.values()));

        Iterator<LinkedHashMap<String, DataRecord>> dataSourceDataRecordIterator =
                Iterators.transform(joinedIterator, composeDataSourceDataRecordMap(dataSources.keySet()));

        return Iterators.transform(dataSourceDataRecordIterator, forMethod(parameters));
    }

    public static Function<Collection<DataRecord>, LinkedHashMap<String, DataRecord>>
    composeDataSourceDataRecordMap(final Collection<String> dataSourceNames) {
        return new Function<Collection<DataRecord>, LinkedHashMap<String, DataRecord>>() {
            @Override
            public LinkedHashMap<String, DataRecord> apply(Collection<DataRecord> dataRecords) {
                LinkedHashMap<String, DataRecord> dataSourcesRecords = new LinkedHashMap<String, DataRecord>();
                Iterator<String> dataSourceNameIterator = dataSourceNames.iterator();
                for (DataRecord dataRecord : dataRecords) {
                    dataSourcesRecords.put(dataSourceNameIterator.next(), dataRecord);
                }
                return dataSourcesRecords;
            }
        };
    }

    public static Function<LinkedHashMap<String, DataRecord>, Object[]> forMethod(final List<Parameter> parameters) {
        return new Function<LinkedHashMap<String, DataRecord>, Object[]>() {
            @Override
            public Object[] apply(final LinkedHashMap<String, DataRecord> dataRecords) {
                List<Object> params = Lists.transform(parameters, forDataRecords(dataRecords));
                return params.toArray();
            }
        };
    }

    static Function<Parameter, Object> forDataRecords(final LinkedHashMap<String, DataRecord> dataSourcesRecords) {
        return new Function<Parameter, Object>() {
            @Override
            public Object apply(final Parameter parameter) {
                Optional optionalValue = Optional.absent();
                for (Map.Entry<String, DataRecord> dataRecord : dataSourcesRecords.entrySet()) {
                    optionalValue = transform(dataRecord.getKey(), dataRecord.getValue(), parameter);
                    if (optionalValue.isPresent()) {
                        break;
                    }
                }

                Object value = null;
                if (optionalValue.isPresent()) {
                    value = optionalValue.get();
                } else {
                    OptionalValue annotation = (OptionalValue) parameter.getAnnotation(OptionalValue.class);
                    if (hasDefaultValue(annotation)) {
                        value = annotation.value();
                    }
                }
                return map(value, parameter.getType());
            }
        };
    }

    @VisibleForTesting
    protected static boolean hasDefaultValue(OptionalValue optionalValue) {
        if (optionalValue == null) {
            return false;
        }
        String defaultValue = optionalValue.value();
        return !OptionalValue.VALUE_NOT_DEFINED.equals(defaultValue);
    }

    static Optional transform(String dataSourceName, DataRecord record, Parameter<?> parameter) {
        String name = parameter.getName();
        Class<?> type = parameter.getType();

        if (name == null) {
            return Optional.absent();
        }

        if (DataRecord.class.isAssignableFrom(type) && name.equals(dataSourceName)) {
            Object dataRecord = injectDataRecord(dataSourceName, record, (Class<? extends DataRecord>) type);
            return Optional.of(dataRecord);
        } else if (name.equals(dataSourceName)) {
            Map<String, Object> properties = Maps.newHashMap(record.getAllFields());
            BeanPropertyContainer container = new BeanPropertyContainer(properties);
            return Optional.of(container);
        } else {
            if (name.startsWith(dataSourceName + ".")) {
                name = name.substring(name.indexOf('.') + 1);
            }
            Object value = record.getAllFields().get(name);
            if (value instanceof DataRecord) {
                value = injectDataRecord("unknown", DataRecord.class.cast(value), (Class<? extends DataRecord>) type);
            }
            return Optional.fromNullable(value);
        }
    }

    private static Object injectDataRecord(String dataSourceName, DataRecord record, Class<? extends DataRecord> type) {
        Map<String, Object> allFields = record.getAllFields();
        DataRecord dataRecord = TestDataSourceFactory.createDataRecord(dataSourceName, allFields);
        return DataRecordProxyFactory.createProxy(dataRecord, type);
    }

    static <T> T map(Object value, Class<T> type) {
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

    public static <T> T convertValue(Object value, Class<T> type) {
        if(type.isPrimitive() && (value == null || value.toString().isEmpty())){
            return Defaults.defaultValue(type);
        }
        ConvertUtilsConfiguration.configure(Optional.<char[]>absent());
        try {
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
