package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Data source implementation loading data from custom class
 */
@API(Internal)
public class ClassDataSource extends UnmodifiableDataSource<DataRecord> {

    public static final String CLASS = "class";
    private static final String PACKAGE_SEPARATOR = ".";
    private static final String INNER_CLASS_SEPARATOR_CHAR = "$";

    private Object dataSource;

    private ConfigurationSource reader;

    @Override
    public void init(ConfigurationSource reader) {
        String className = reader.getProperty(CLASS);
        this.reader = reader;
        this.dataSource = loadClass(className);
    }

    private Object loadClass(String className) {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class type = getClass(className, contextClassLoader);
            return type.newInstance();
        } catch (Exception e) {
            String msg = String.format("Could not load data source from class [%s], since the exception [%s] was thrown", className, e);
            throw new IllegalArgumentException(msg, e);
        }
    }


    public Class getClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ex) {
            // allow path separators (.) as inner class name separators
            int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                return getClass(className.substring(0, lastDotIndex) +
                        INNER_CLASS_SEPARATOR_CHAR + className.substring(lastDotIndex + 1), classLoader);
            }
            throw ex;
        }
    }

    @Override
    public Iterator<DataRecord> iterator() {
        final Method method = findMethod(dataSource);
        final Iterable<Map<String, Object>> iterable = retrieveIterable(method, dataSource);
        return Iterators.transform(iterable.iterator(), new Function<Map<String, Object>, DataRecord>() {
            public DataRecord apply(Map<String, Object> row) {
                return TestDataSourceFactory.createDataRecord(row);
            }
        });
    }

    private Iterable<Map<String, Object>> retrieveIterable(Method method, Object dataSource) {
        Object result;
        try {
            if (method.getParameterTypes().length == 1 && isTypeMatches(method, ConfigurationSource.class)) {
                result = method.invoke(dataSource, reader);
            } else {
                result = method.invoke(dataSource);
            }

        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return (Iterable<Map<String, Object>>) result;
    }

    private boolean isTypeMatches(Method method, Class<ConfigurationSource> configurationSource) {
        Type type = method.getParameterTypes()[0];
        if (ConfigurationSource.class.isAssignableFrom((Class<?>) type)) {
            return true;
        }
        throw new IllegalArgumentException("Parameter Type should be com.ericsson.cifwk.taf.datasource.ConfigurationSource for the Method annotated with @DataSource");
    }

    private Method findMethod(Object dataSource) {
        Method[] methods = dataSource.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(DataSource.class)) {
                Class<?> returnType = method.getReturnType();
                checkIfIterable(returnType);
                return method;
            }
        }
        String msg = String.format("Method annotated with @DataSource is not found on class : %s", dataSource.getClass());
        throw new IllegalArgumentException(msg);
    }

    private void checkIfIterable(Class<?> returnType) {
        if (!Iterable.class.isAssignableFrom(returnType)) {
            String msg = String.format(
                    "For method annotated with @DataSource is class : %s , the return type [%s] can't implement Iterable<Map<String,Object>",
                    dataSource.getClass(), returnType);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void close() throws IOException {
        dataSource = null;
    }

    @Override
    public TestDataSource getSource() {
        return null;
    }

    @Override
    public String toString() {
        return "DataSource for class " + dataSource.getClass();
    }
}
