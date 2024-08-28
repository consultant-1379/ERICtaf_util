package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkState;
import static java.beans.Introspector.getBeanInfo;

@API(Internal)
class DataRecordBridge implements InvocationHandler {

    private static final Method equalsMethod;
    private static final Method hashCodeMethod;
    private static final Method getFieldValueMethod;
    private static final Method getAllFieldsMethod;
    private static final Set<String> propertiesToBeExcluded;
    static {
        equalsMethod = getObjectMethod("equals", Object.class);
        hashCodeMethod = getObjectMethod("hashCode");
        getFieldValueMethod = getDataRecordMethod("getFieldValue", String.class);
        getAllFieldsMethod = getDataRecordMethod("getAllFields");
        propertiesToBeExcluded = getProperties(DataRecordImpl.class);
    }

    private final DataRecord dataRecord;

    DataRecordBridge(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(DataRecord.class) || method.getDeclaringClass().equals(Object.class)) {
            if (equalsMethod.equals(method)) {
                return equals(proxy, args[0]);
            }
            if (hashCodeMethod.equals(method)) {
                return hashCode(proxy);
            }
            if (getFieldValueMethod.equals(method) || getAllFieldsMethod.equals(method)){
                Object value = method.invoke(dataRecord, args);
                return ObjectTypeConverter.map(value, method.getReturnType());
            }
            return method.invoke(dataRecord, args);
        }

        checkState(isGetter(method), "Only get and is methods are supported in DataRecord interface!");

        String propertyName = new BeanProperty(method).getName(method.getName().startsWith("get"));
        Object value = dataRecord.getFieldValue(propertyName);
        return ObjectTypeConverter.map(value, method.getReturnType());
    }

    private boolean equals(Object proxy1, Object proxy2) {
        return Arrays.deepEquals(getFieldsToCompare(proxy1), getFieldsToCompare(proxy2));
    }

    private int hashCode(Object proxy) {
        return Arrays.deepHashCode(getFieldsToCompare(proxy));
    }

    private Object[] getFieldsToCompare(Object proxy) {
        List<Object> fieldsToCompare = new ArrayList<>();
        Map<String, Object> properties = getProperties(proxy);
        for (String key : properties.keySet()) {

            // adding field name
            fieldsToCompare.add(key);

            // adding field value
            fieldsToCompare.add(properties.get(key));
        }
        return fieldsToCompare.toArray();
    }

    private static Set<String> getProperties(Class<?> clazz) {
        try {
            Set<String> properties = new HashSet<>();
            for (PropertyDescriptor propertyDescriptor : getBeanInfo(clazz).getPropertyDescriptors()) {
                properties.add(propertyDescriptor.getReadMethod().getName());
            }
            return properties;
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    protected static Map<String, Object> getProperties(Object proxy) {
        try {
            Map<String, Object> values = new TreeMap<>();
            Method[] methods = proxy.getClass().getMethods();
            for (Method method : methods) {
                if (!isGetter(method)) {
                    continue;
                }
                String key = method.getName();
                if (!propertiesToBeExcluded.contains(key)) {
                    Object value = method.invoke(proxy);
                    if (value != null) {
                        values.put(key, value);
                    }
                }
            }
            return values;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isGetter(Method method) {
        return !method.getReturnType().equals(Void.TYPE)
                && (method.getName().startsWith("get") || method.getName().startsWith("is"))
                && (method.getParameterTypes().length == 0);
    }

    private static Method getObjectMethod(String methodName, Class<?>... arguments) {
        try {
            return Object.class.getMethod(methodName, arguments);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get Object method", e);
        }
    }

    private static Method getDataRecordMethod(String methodName, Class<?>... arguments) {
        try {
            return DataRecord.class.getMethod(methodName, arguments);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get DataRecord method", e);
        }
    }
}
