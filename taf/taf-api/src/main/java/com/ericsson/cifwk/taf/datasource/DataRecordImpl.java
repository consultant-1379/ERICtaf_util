package com.ericsson.cifwk.taf.datasource;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;

/**
 *
 */
public final class DataRecordImpl implements DataRecord {
    private final String dataSourceName;
    private final Map<String, Object> row = Maps.newLinkedHashMap();

    /**
     * Represents empty result of Test Step
     * @see TestStepDefinition#collectResultToDatasource
     */
    public static final DataRecord EMPTY = new DataRecord() {
        @Override
        public <T> T getFieldValue(String name) {
            return null;
        }

        @Override
        public Map<String, Object> getAllFields() {
            return Maps.newHashMap();
        }

        @Override
        public String getDataSourceName() {
            return "empty";
        }

        @Override
        public <T> T transformTo(Class<T> beanClass) {
            T instance;
            try {
                instance = beanClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
            return instance;
        }
    };

    public DataRecordImpl() {
        this(Collections.EMPTY_MAP);
    }

    public DataRecordImpl(Map<String, ?> data) {
        this("unknown", data);
    }

    public DataRecordImpl(String dataSourceName, Map<String, ?> data) {
        if (data != null) {
            row.putAll(data);
        }
        this.dataSourceName = dataSourceName;
    }

    @Override
    public <T> T getFieldValue(String name) {
        return (T) row.get(name);
    }

    void setFieldValues(Map<String, Object> values) {
        row.putAll(values);
    }

    @Override
    public Map<String, Object> getAllFields() {
        return Collections.unmodifiableMap(row);
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

    void setFieldValue(String name, Object value) {
        row.put(name, value);
    }

    @Override
    public String toString() {
        return String.format("Data value: %s", Objects.toString(row));
    }


    @Override
    public <T> T transformTo(Class<T> beanClass) {
        T instance;
        try {
            instance = beanClass.newInstance();
            BeanUtils.populate(instance, this.getAllFields());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw Throwables.propagate(e);
        }
        return instance;
    }
}
