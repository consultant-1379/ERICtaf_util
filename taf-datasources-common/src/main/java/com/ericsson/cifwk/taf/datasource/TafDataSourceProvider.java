package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.data.pool.DataPoolStrategy;
import com.ericsson.cifwk.taf.data.pool.DataPoolUsage;
import com.ericsson.cifwk.taf.management.TafContext;
import com.google.common.base.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Entry point for getting new TestDataSource instances.
 */
@API(Internal)
public final class TafDataSourceProvider {

    private TafDataSourceProvider() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TafDataSourceProvider.class);

    private static int contextShifter;

    public static <T extends DataRecord> TestDataSource<T> provide(DataSourceFactory factory,
                                                                   String name,
                                                                   @Nullable Method method,
                                                                   @Nullable DataSourceContext globalContext,
                                                                   Predicate<? super T> predicate,
                                                                   Class<T> dataRecordType) throws UnknownDataSourceTypeException, DataSourceNotFoundException {
        TestDataSource<T> dataSource = provideDataSource(factory, name, method, globalContext, dataRecordType);
        if (predicate != null) {
            return new FilteredDataSource<>(dataSource, predicate);
        } else {
            return dataSource;
        }
    }

    private static <T extends DataRecord> TestDataSource<T> provideDataSource(DataSourceFactory factory,
                                                                              String name,
                                                                              Method method,
                                                                              DataSourceContext globalContext,
                                                                              Class<T> dataRecordType) throws UnknownDataSourceTypeException, DataSourceNotFoundException {
        DataPoolUsage usage;
        TestDataSource<T> dataSource;

        if (contextDataSourceExists(name)) {
            usage = DataPoolUsage.COPIED;
            TestDataSource<DataRecord> contextDataSource =
                    new ContextTafDataSource(name, TafTestContext.getContext().dataSource(name));
            dataSource = makeDataSourceTyped(contextDataSource, dataRecordType);
        } else {
            usage = getUsageFromProperties(name);
            dataSource = provideFromProperties(factory, name, usage, globalContext, dataRecordType);
        }

        if (dataSource == null) {
            String msg = String.format("Data Source `%s` not found%s", name, method == null ? "" : " for method " + method);
            throw new DataSourceNotFoundException(msg);
        }

        updateMethodContextAttributes(method, usage);

        return dataSource;
    }

    public static <T extends DataRecord> TestDataSource<T> provideFromProperties(DataSourceFactory factory,
                                                                                 String name,
                                                                                 @Nullable DataPoolUsage usage,
                                                                                 @Nullable DataSourceContext globalContext,
                                                                                 Class<T> dataRecordType) throws UnknownDataSourceTypeException {
        TestDataSource<T> dataSource;
        ConfigurationSource reader = new PropertiesReader(name);
        String type = reader.getProperty(PropertiesReader.DATA_SOURCE_TYPE);
        if (type == null) {
            return null;
        }

        if (usage == null) {
            usage = getUsageFromProperties(name);
        }

        TestDataSource<DataRecord> ordinaryTestDataSource = getOrdinaryTestDataSource(factory, reader, usage, name, globalContext, type);
        dataSource = makeDataSourceTyped(ordinaryTestDataSource, dataRecordType);
        TafContext.setDataSource(name, dataSource);

        return dataSource;
    }

    public static DataPoolUsage getUsageFromProperties(String name) {
        ConfigurationSource reader = new PropertiesReader(name);
        String usageString = reader.getProperty(PropertiesReader.DATA_SOURCE_USAGE);

        return determineDataUsage(usageString);
    }

    public static <T extends DataRecord> TestDataSource<T> makeDataSourceTyped(TestDataSource<DataRecord> contextDataSource, Class<T> dataRecordType) {
        if (dataRecordType == DataRecord.class) {
            //noinspection unchecked
            return (TestDataSource<T>) contextDataSource;
        } else {
            return new GenericDataSource<>(contextDataSource, dataRecordType);
        }
    }

    private static void updateMethodContextAttributes(Method method, DataPoolUsage usage) {
        if (method == null) {
            return;
        }

        if (DataPoolUsage.COPIED.equals(usage)) {
            List matrix = TafContext.getMethodAttributes().get(method);
            if (TafContext.getDataDrivenAttributes().get(method) != null) {
                contextShifter = (Integer) TafContext.getDataDrivenAttributes().get(method)[0];
            } else {
                contextShifter = 0;
            }
            contextShifter = ++contextShifter % matrix.size();
        }

        Object[] dataDrivenValues = new Object[2];
        dataDrivenValues[0] = contextShifter;
        dataDrivenValues[1] = usage;
        TafContext.getDataDrivenAttributes().put(method, dataDrivenValues);
    }

    private static boolean contextDataSourceExists(String name) {
        return TafTestContext.getContext().doesDataSourceExist(name);
    }

    private static TestDataSource<DataRecord> getOrdinaryTestDataSource(DataSourceFactory factory,
                                                                        ConfigurationSource reader,
                                                                        DataPoolUsage usage,
                                                                        String name,
                                                                        DataSourceContext context,
                                                                        String type) throws UnknownDataSourceTypeException {
        String strategyString = reader.getProperty(PropertiesReader.DATA_SOURCE_STRATEGY);
        DataPoolStrategy strategy = determineDataStrategy(strategyString);

        TestDataSource<DataRecord> dataSource;

        if (DataPoolUsage.SHARED.equals(usage)) {
            dataSource = provideSharedDataSource(factory, name, type, reader, strategy, context);
        } else {
            dataSource = provide(factory, name, type, reader, strategy);
        }
        return dataSource;
    }

    private static TestDataSource provideSharedDataSource(DataSourceFactory factory, String name, String type, ConfigurationSource reader,
                                                          DataPoolStrategy strategy, DataSourceContext context) throws UnknownDataSourceTypeException {
        if (context == null) {
            // no point of sharing DS, as without context there is no guarantee of single instance
            // delegate sharing to whoever able to handle com.ericsson.cifwk.taf.datasource.DataSourceOptions anotation
            return new MarkedAsSharedTafDataSource(provide(factory, name, type, reader, strategy));
        }

        Map<String, TestDataSource> dataSources = getDataSources(context);
        TestDataSource<DataRecord> dataSource = dataSources.get(name);
        if (dataSource == null) {
            synchronized (dataSources) {
                dataSource = dataSources.get(name);
                if (dataSource == null) {
                    TestDataSource<DataRecord> originalDataSource = provide(factory, name, type, reader, strategy);
                    dataSource = new SharedTafDataSource<>(originalDataSource);
                    dataSources.put(name, dataSource);
                }
            }
        }
        LOGGER.debug("Reusing shared data source : " + name);
        return dataSource;
    }

    static TestDataSource<DataRecord> provide(DataSourceFactory factory, String name, String type, ConfigurationSource reader, DataPoolStrategy strategy) throws UnknownDataSourceTypeException {
        TestDataSource<DataRecord> dataSource = dataSourceOfType(factory, type, reader, strategy);
        LOGGER.debug("Opening new data source : " + name);
        return dataSource;
    }

    static Map<String, TestDataSource> getDataSources(DataSourceContext context) {
        Map<String, TestDataSource> dataSources = context.getDataSources();
        if (dataSources == null) {
            synchronized (context) {
                dataSources = context.getDataSources();
                if (dataSources == null) {
                    dataSources = new ConcurrentHashMap<>();
                    context.setDataSources(dataSources);
                }
            }
        }
        return dataSources;
    }

    private static DataPoolStrategy determineDataStrategy(String strategyString) {
        DataPoolStrategy strategy;
        if (strategyString != null && !strategyString.trim().isEmpty()) {
            strategy = DataPoolStrategy.valueOf(strategyString.toUpperCase());
        } else {
            strategy = DataPoolStrategy.STOP_ON_END;
        }
        return strategy;
    }

    private static DataPoolUsage determineDataUsage(String usageString) {
        DataPoolUsage usage;
        if (usageString != null && !usageString.trim().isEmpty()) {
            usage = DataPoolUsage.valueOf(usageString.toUpperCase());
        } else {
            usage = DataPoolUsage.COPIED;
        }
        return usage;
    }

    public static void releaseDataSources(DataSourceContext context) {
        Map<String, TestDataSource> dataSources = context.getDataSources();
        if (dataSources == null) {
            return;
        }

        for (Map.Entry<String, TestDataSource> entry : dataSources.entrySet()) {
            try {
                TestDataSource dataSource = entry.getValue();
                dataSource.close();
            } catch (Exception e) {
                LOGGER.error(String.format("Failed to close data source [%s], since the exception [%s] was thrown",
                        entry.getKey(), e.getMessage()), e);
            }
            dataSources.remove(entry.getKey());
        }
    }

    public static TestDataSource<DataRecord> dataSourceOfType(DataSourceFactory factory, String type,
                                                              ConfigurationSource reader,
                                                              DataPoolStrategy strategy) throws UnknownDataSourceTypeException {

        TestDataSource<DataRecord> dataSource = factory.createDataSource(type, reader);
        return decorateDataSource(dataSource, strategy);
    }

    public static <T extends DataRecord> TestDataSource<T> dataSourceOfType(DataSourceFactory factory,
                                                                            String type,
                                                                            ConfigurationSource reader,
                                                                            DataPoolStrategy strategy,
                                                                            Class<T> dataRecordType) throws UnknownDataSourceTypeException {

        TestDataSource<DataRecord> dataSource = factory.createDataSource(type, reader);
        GenericDataSource<T> genericDataSource = new GenericDataSource<>(dataSource, dataRecordType);
        return decorateDataSource(genericDataSource, strategy);
    }

    private static <T extends DataRecord> TestDataSource<T> decorateDataSource(TestDataSource<T> dataSource,
                                                                               DataPoolStrategy strategy) {
        return handleBehavior(dataSource, strategy);
    }

    private static <T extends DataRecord> TestDataSource<T> handleBehavior(TestDataSource<T> dataSource,
                                                                           DataPoolStrategy strategy) {
        switch (strategy) {
            case STOP_ON_END:
                return dataSource;
            case REPEAT_UNTIL_STOPPED:
                return new CyclicDataSource<>(dataSource);
            case RESET_ON_END:
                throw new UnsupportedOperationException();
            default:
                throw new IllegalArgumentException();
        }
    }


}
