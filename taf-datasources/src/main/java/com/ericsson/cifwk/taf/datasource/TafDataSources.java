/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.datasource;

import static java.lang.String.format;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.ericsson.cifwk.taf.datasource.TestDataSourceFactory.fromServiceRegistry;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.data.pool.DataPoolStrategy;
import com.ericsson.cifwk.taf.data.pool.DataPoolUsage;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * <pre>
 * Class Name: TafDataSources
 * Description: Set of helper functions around TafDataSource functionality.
 * suite.
 * </pre>
 */
@API(Stable)
public final class TafDataSources {

    private TafDataSources() {
    }

    /**
     * <pre>
     * Name: fromTafDataProvider(String)
     * Description: Creates a new DataSource instance from Taf Data Provider and assume that each DataSource record is a generic DataRecord object.
     *              NB: Could not be used for Data Sources with usage shared: use method TafDataSources.shared(...)
     *              (@see also {@link TafDataSources#fromTafDataProvider(String, Class)})
     * </pre>
     *
     * @param dataProviderName label for selected provider: in this exemple provider name is 'calculator' (dataprovider.calculator.type=csv)
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromTafDataProvider(final String dataProviderName) {
        return fromTafDataProvider(dataProviderName, DataRecord.class);
    }

    /**
     * <pre>
     * Name: fromTafDataProvider(String, Class&lt;T&gt;)
     * Description: Creates a new DataSource instance from Taf Data Provider and assume that each DataSource record is 'type' object.
     *              NB: Could not be used for Data Sources with usage shared: use method TafDataSources.shared(...)
     *              (@see also {@link TafDataSources#fromTafDataProvider(String)})
     * </pre>
     *
     * @param dataProviderName  label for selected provider: in this exemple provider name is 'calculator' (dataprovider.calculator.type=csv)
     * @param type  Selected object type fot creating DataSource
     * @return generated Datasource with 'type' DataRecord.
     */
    public static <T extends DataRecord> TestDataSource<T> fromTafDataProvider(final String dataProviderName, final Class<T> type) {
        final DataPoolUsage usage = TafDataSourceProvider.getUsageFromProperties(dataProviderName);
        checkSharedUsage(usage, dataProviderName);

        final TestDataSource<T> dataSource = fromServiceRegistry(dataProviderName, type);

        checkArgument(dataSource != null, format("Data Provider `%s` not found", dataProviderName));
        return dataSource;
    }

    /**
     * <pre>
     * Name: fromTafDataProviderForSize(String)
     * Description: Creates a new DataSource instance from Taf Data Provider and assume that each DataSource record is a generic DataRecord object.
     *              NB: Has been used to get the size of Data Sources with usage shared
     * </pre>
     *
     * @param dataProviderName label for selected provider: in this example provider name is 'user' (dataprovider.user.type=csv)
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromTafDataProviderForSize(final String dataProviderName) {
        return fromTafDataProviderForSize(dataProviderName, DataRecord.class);
    }

    public static <T extends DataRecord> TestDataSource<T> fromTafDataProviderForSize(final String dataProviderName, final Class<T> type) {
        final TestDataSource<T> dataSource = fromServiceRegistry(dataProviderName, type);
        checkArgument(dataSource != null, format("Data Provider `%s` not found", dataProviderName));
        return dataSource;
    }

    private static void checkSharedUsage(final DataPoolUsage usage, final String dataProviderName) {
        if (DataPoolUsage.SHARED.equals(usage)) {
            throw new IllegalArgumentException(format("Could not initialize Shared Data Source from provider `%s`. "
                            + "Please remove `%s.%s` from `datadriven.properties` and use method TafDataSources.shared(...) instead.",
                    dataProviderName,
                    PropertiesReader.DATA_SOURCE_USAGE,
                    DataPoolUsage.SHARED.toString().toLowerCase()));
        }
    }

    /**
     * <pre>
     * Name: fromCsv(String)
     * Description: Creates a new DataSource instance from CSV file located in specific location (from data directory in resource location).
     *              (@see also {@link TafDataSources#fromCsv(String, DataPoolStrategy)})
     *
     * </pre>
     *
     * @param location  pathname for CSV file.
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromCsv(final String location) {
        return fromCsv(location, DataPoolStrategy.STOP_ON_END);
    }

    /**
     * <pre>
     * Name: fromCsv(String)
     * Description: Creates a new DataSource instance from CSV file located in specific location (from data directory in resource location) and
     *              with specified strategy (STOP_ON_END, REPEAT_UNTIL_STOPPED, RESET_ON_END).
     *              (@see also {@link TafDataSources#fromCsv(String)})
     * </pre>
     *
     * @param location  pathname for CSV file.
     * @param strategy  Selected Strategy for created DataSource
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromCsv(final String location, final DataPoolStrategy strategy) {
        final MapSource source = getLocationProperty(location);
        try {
            return TafDataSourceProvider.dataSourceOfType(new TafDataSourceFactory(), TafDataSourceFactory.TYPE_CSV, source, strategy);
        } catch (final UnknownDataSourceTypeException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * <pre>
     * Name: fromCsv(String, Class&lt;T&gt;)
     * Description: Creates a new DataSource instance from CSV file located in specific location (from data directory in resource location) and
     *              with specific object type.
     *              (@see also {@link TafDataSources#fromCsv(String, DataPoolStrategy, Class)})
     * </pre>
     *
     * @param location  pathname for CSV file.
     * @param type  Selected object type fot creating DataSource
     * @return generated Datasource with 'type' type
     */
    public static <T extends DataRecord> TestDataSource<T> fromCsv(final String location, final Class<T> type) {
        return fromCsv(location, DataPoolStrategy.STOP_ON_END, type);
    }

    /**
     *
     * <pre>
     * Name: fromCsv(String, DataPoolStrategy, Class&lt;T&gt;)
     * Description: Creates a new DataSource instance from CSV file located in specific location (from data directory in resource location),
     *              with a specific strategy for generated Datasource (STOP_ON_END, REPEAT_UNTIL_STOPPED, RESET_ON_END) and with a specific object
     *              type structure.
     *              (@see also {@link TafDataSources#fromCsv(String, Class)})
     * </pre>
     *
     * @param location  pathname for CSV file.
     * @param strategy  Selected Strategy for created DataSource
     * @param dataRecordType  Selected object type fot creating DataSource
     * @return generated Datasource with 'type' type
     */
    private static <T extends DataRecord> TestDataSource<T> fromCsv(final String location, final DataPoolStrategy strategy,
            final Class<T> dataRecordType) {
        final MapSource source = getLocationProperty(location);
        try {
            return TafDataSourceProvider
                    .dataSourceOfType(new TafDataSourceFactory(), TafDataSourceFactory.TYPE_CSV, source, strategy, dataRecordType);
        } catch (final UnknownDataSourceTypeException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * <pre>
     * Name: fromCsv(String...)
     * Description: Creates a new DataSource instance from combined CSV files: a list (array) of CSV filename/pathname give location of files to be
     *              use for Datasource creation. Datasources are composed by Datarecord objects.
     *              (@see also {@link TafDataSources#fromCsv(String)})
     * </pre>
     *
     * @param locations Array of CSV files locations
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromCsv(final String... locations) {
        final Function<String, TestDataSource<DataRecord>> fromCSVFunction = new Function<String, TestDataSource<DataRecord>>() {
            @Override
            public TestDataSource<DataRecord> apply(final String input) {
                return TafDataSources.fromCsv(input);
            }
        };

        final List<TestDataSource<DataRecord>> list = Lists.transform(Arrays.asList(locations), fromCSVFunction);
        return combine(list.toArray((TestDataSource<? extends DataRecord>[]) new TestDataSource[locations.length]));
    }

    private static MapSource getLocationProperty(final String location) {
        final Map<String, String> properties = Maps.<String, String>newHashMap();
        properties.put(CsvDataSource.LOCATION, location);
        return new MapSource(properties);
    }

    /**
     * <pre>
     * Name: fromClass(Class&lt;?&gt;)
     * Description: Creates a new DataSource from class method annotated with {@link com.ericsson.cifwk.taf.annotations.DataSource } and with
     *              STOP_ON_END strategy.
     *              (@see also {@link TafDataSources#fromClass(Class, DataPoolStrategy)})
     * </pre>
     *
     * @param type Class used for DataSource creation
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromClass(final Class<?> type) {
        return fromClass(type, DataPoolStrategy.STOP_ON_END);
    }

    /**
     * <pre>
     * Name: fromClass(Class&lt;?&gt;, DataPoolStrategy)
     * Description: Creates a new DataSource from class method annotated with {@link com.ericsson.cifwk.taf.annotations.DataSource } and with a
     *              specified strategy.
     *              (@see also {@link TafDataSources#fromClass(Class)})
     * </pre>
     *
     * @param type  Class used for DataSource creation
     * @param strategy  Selected Strategy for created DataSource
     * @return generated Datasource with DataRecord type
     */
    public static TestDataSource<DataRecord> fromClass(final Class<?> type, final DataPoolStrategy strategy) {
        final MapSource source = getClassProperty(type);
        try {
            return TafDataSourceProvider.dataSourceOfType(new TafDataSourceFactory(), TafDataSourceFactory.TYPE_CLASS, source, strategy);
        } catch (final UnknownDataSourceTypeException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * <pre>
     * Name: fromClass(Class&lt;?&gt;, Class&lt;?&gt)
     * Description: Creates a new DataSource from class method annotated with {@link com.ericsson.cifwk.taf.annotations.DataSource } and with a
     *              specified DataRecordType (extension of DataRecord).
     *              (@see also {@link TafDataSources#fromClass(Class, DataPoolStrategy, Class)})
     * </pre>
     *
     * @param type Class used for DataSource creation
     * @param dataRecordType Selected object type fot creating DataSource
     * @return generated Datasource with DataRecordType type
     */
    public static <T extends DataRecord> TestDataSource<T> fromClass(final Class<?> type, final Class<T> dataRecordType) {
        return fromClass(type, DataPoolStrategy.STOP_ON_END, dataRecordType);
    }

    /**
     * <pre>
     * Name: fromClass(Class&lt;?&gt;, DataPoolStrategy, Class&lt;?&gt)
     * Description: Creates a new DataSource from class method annotated with {@link com.ericsson.cifwk.taf.annotations.DataSource }, with a
     *              specified DataRecordType (extension of DataRecord) and a specified Strategy.
     *              (@see also {@link TafDataSources#fromClass(Class, Class)})
     * </pre>
     *
     * @param type Class used for DataSource creation
     * @param strategy Selected Strategy for created DataSource
     * @param dataRecordType Selected object type fot creating DataSource
     * @return generated Datasource with DataRecordType type
     */
    private static <T extends DataRecord> TestDataSource<T> fromClass(final Class<?> type, final DataPoolStrategy strategy,
            final Class<T> dataRecordType) {
        final MapSource source = getClassProperty(type);
        try {
            return TafDataSourceProvider
                    .dataSourceOfType(new TafDataSourceFactory(), TafDataSourceFactory.TYPE_CLASS, source, strategy, dataRecordType);
        } catch (final UnknownDataSourceTypeException e) {
            throw Throwables.propagate(e);
        }
    }

    private static MapSource getClassProperty(final Class<?> type) {
        final Map<String, String> properties = Maps.<String, String>newHashMap();
        properties.put(ClassDataSource.CLASS, type.getName());
        return new MapSource(properties);
    }

    /**
     * <pre>
     * Name: combine(TestDataSource&lt;?&gt; ...)
     * Description: Combines multiple data sources into one. Result of this operation will be new data source where all data source records (rows)
     *              are appended one after another.
     *              N.B.: Data sources passed as arguments are left unchanged.
     * </pre>
     *
     * @param dataSourcesToCombine list of dataSources to combine
     * @return combined data source
     */
    public static <T extends DataRecord> TestDataSource<DataRecord> combine(final TestDataSource<? extends DataRecord>... dataSourcesToCombine) {
        return new AbstractCompositeDataSource<DataRecord>() {
            @Override
            public Iterator<DataRecord> iterator() {
                return Iterables.concat(dataSourcesToCombine).iterator();
            }
        };
    }

    /**
     * Merges two data sources into one. Records columns (headers) are merged one by one.
     * Values from the second data source will overwrite the first one in case header names match.
     * analogous to Inner join in SQL
     *
     * @param one
     *         first data source
     * @param two
     *         second data source
     *
     * @return merged data source
     */
    /**
     * <pre>
     * Name: merge(TestDataSource&lt;T&gt;, TestDataSource&lt;U&gt;)
     * Description: Merges two data sources into one. Records columns (headers) are merged one by one. Values from the second data source will
     *               overwrite the first one in case header names match. Analogous to Inner join in SQL.
     * </pre>
     *
     * @param one first data source
     * @param two second data source
     * @return merged data source
     */
    public static <T extends DataRecord, U extends DataRecord> TestDataSource<DataRecord> merge(final TestDataSource<T> one,
            final TestDataSource<U> two) {
        return new AbstractCompositeDataSource<DataRecord>() {
            @Override
            public Iterator<DataRecord> iterator() {
                return new CompositeIteratorInner(one.iterator(), two.iterator());
            }
        };
    }

    /**
     * <pre>
     * Name: merge(TestDataSource&lt;T&gt;, TestDataSource&lt;U&gt;, MergeType)
     * Description: Merges two data sources into one. Records columns (headers) are merged one by one. Values from the second data source will
     *               overwrite the first one in case header names match. It's possible to add Merge type: INNER / OUTER.
     * </pre>
     *
     * @param one first data source
     * @param two second data source
     * @param mergeType mergeType to be used
     * @return merged data source
     */
    public static <T extends DataRecord, U extends DataRecord> TestDataSource<DataRecord> merge(final TestDataSource<T> one,
            final TestDataSource<U> two, final MergeType mergeType) {
        switch (mergeType) {
            case OUTER:
                return new AbstractCompositeDataSource<DataRecord>() {
                    @Override
                    public Iterator<DataRecord> iterator() {
                        return new CompositeIteratorOuter(one.iterator(), two.iterator());
                    }
                };
            case INNER:
            default:
                return new AbstractCompositeDataSource<DataRecord>() {
                    @Override
                    public Iterator<DataRecord> iterator() {
                        return new CompositeIteratorInner(one.iterator(), two.iterator());
                    }
                };
        }
    }

    /**
     * <pre>
     * Name: filter(TestDataSource&lt;T&gt;, Predicate&lt;?&gt;)
     * Description: Filters provided data source and return new filtered instance.
     * </pre>
     *
     * @param one original data source
     * @param predicate filtering function
     * @return filtered data source
     */
    public static <T extends DataRecord> TestDataSource<T> filter(final TestDataSource<T> one, final Predicate<? super T> predicate) {
        return new AbstractCompositeDataSource<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.filter(one.iterator(), predicate);
            }

            @Override
            public TestDataSource getSource() {
                return one;
            }
        };
    }

    /**
     * <pre>
     * Name: shared(TestDataSource&lt;T&gt;)
     * Description: Provided data source will be shared with other vUsers as single data source, so the row of data is used only once in scenario.
     * </pre>
     *
     * @param one original data source
     * @return transformed data source
     */
    public static <T extends DataRecord> TestDataSource<T> shared(final TestDataSource<T> one) {
        return new MarkedAsSharedTafDataSource<>(one);
    }

    /**
     * <pre>
     * Name: copy(TestDataSource)
     * Description: Creates new data source with data from given one. Used to 'unwrap' data sources.
     * </pre>
     *
     * @param one original data source
     * @return transformed data source
     */
    @SuppressWarnings("unchecked")
    public static TestDataSource copy(final TestDataSource one) {
        final TestDataSource newDataSource = TestDataSourceFactory.createDataSource();

        final TestDataSource unwrapped = DataSourceControl.unwrap(one);
        for (final Object dataRecord : unwrapped) {
            newDataSource.addRecord().setFields((DataRecord) dataRecord);
        }

        return newDataSource;
    }

    /**
     * <pre>
     * Name: reverse(TestDataSource)
     * Description: Creates new data source with data in reverse from given one. Used to 'unwrap' data sources.
     * </pre>
     *
     * @param one original data source
     * @return transformed data source
     */
    public static TestDataSource reverse(final TestDataSource one) {
        final TestDataSource newDataSource = TestDataSourceFactory.createDataSource();
        final TestDataSource unwrapped = DataSourceControl.unwrap(one);
        final List<Object> recordList = new ArrayList<>();
        for (final Object dataRecord : unwrapped) {
            recordList.add(dataRecord);
        }

        final Iterator iterator = Lists.reverse(recordList).iterator();
        while (iterator.hasNext()) {
            newDataSource.addRecord().setFields((DataRecord) iterator.next());
        }
        return newDataSource;
    }

    /**
     * <pre>
     * Name: cyclic(TestDataSource&lt;T&gt;)
     * Description: Provided data source will be read in a loop. If data source is depleted it starts over.
     * </pre>
     *
     * @param one original data source
     * @return transformed data source
     */
    public static <T extends DataRecord> TestDataSource<T> cyclic(final TestDataSource<T> one) {
        return new CyclicDataSource<>(one);
    }

    /**
     * <pre>
     * Name: copyDataSource(String)
     * Description: Returns runnable which replaces data source with given name to its copy. Used to 'unwrap' data sources.
     *               May be used in beforeFlow/AfterFlow methods.
     * </pre>
     *
     * @param name name of data source in context
     * @return runnable
     */
    public static Runnable copyDataSource(final String name) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext context = TafTestContext.getContext();
                context.addDataSource(name, copy(getDataSourceFromContextOrProvider(name)));
            }
        };
    }

    /**
     * <pre>
     * Name: shareDataSource(String)
     * Description: Returns runnable which makes data source with given name shared.
     *               May be used in beforeFlow/AfterFlow methods.
     * </pre>
     *
     * @param name name of data source in context
     * @return runnable
     */
    public static Runnable shareDataSource(final String name) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext context = TafTestContext.getContext();
                context.addDataSource(name, shared(getDataSourceFromContextOrProvider(name)));
            }
        };
    }

    /**
     * <pre>
     * Name: makeDataSourceCyclic(String)
     * Description: Returns runnable which makes data source with given name cyclic.
     *               May be used in beforeFlow/AfterFlow methods.
     * </pre>
     *
     * @param name name of data source in context
     * @return runnable
     */
    public static Runnable makeDataSourceCyclic(final String name) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext context = TafTestContext.getContext();
                context.addDataSource(name, cyclic(getDataSourceFromContextOrProvider(name)));
            }
        };
    }

    private static TestDataSource<DataRecord> getDataSourceFromContextOrProvider(final String dataSourceName) {
        final TestContext context = TafTestContext.getContext();
        if (context.doesDataSourceExist(dataSourceName)) {
            return context.dataSource(dataSourceName);
        } else {
            return fromServiceRegistry(dataSourceName, DataRecord.class);
        }
    }

    /**
     * <pre>
     * Name: transform(TestDataSource&lt;T&gt;, Function&lt;T, U&gt;)
     * Description: Applies transformation function on the provided data source. Used to build a transformed data source instance.
     * </pre>
     *
     * @param one original data source
     * @param function transformation function
     * @return transformed data source
     */
    public static <T extends DataRecord, U extends DataRecord> TestDataSource<U> transform(final TestDataSource<T> one,
            final Function<T, U> function) {
        return new AbstractCompositeDataSource<U>() {
            @Override
            public Iterator<U> iterator() {
                return Iterators.transform(one.iterator(), function);
            }

            @Override
            public TestDataSource getSource() {
                return one;
            }
        };
    }

    /**
     * @return Builder for {@link DataRecord}.
     */
    /**
     * <pre>
     * Name: dataRecordBuilder()
     * Description: DataRecord builder.
     * </pre>
     *
     * @return Builder for {@link DataRecord}.
     */
    public static DataRecordBuilder dataRecordBuilder() {
        return new DataRecordBuilder();
    }

    /**
     * Merge Type to be used while merging two dataSources.
     */
    public enum MergeType {
        /**
         * analogous to inner join in SQL.
         */
        INNER, /**
         * analogous to outer join in SQL.
         */
        OUTER
    }

    private static final class CompositeIteratorInner implements Iterator<DataRecord> {
        private final Iterator<? extends DataRecord>[] iterators;

        CompositeIteratorInner(final Iterator<? extends DataRecord>... iterators) {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            for (final Iterator<? extends DataRecord> iterator : iterators) {
                if (!iterator.hasNext()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public DataRecord next() {
            final Map<String, Object> combinedData = Maps.newHashMap();
            for (final Iterator<? extends DataRecord> iterator : iterators) {
                final DataRecord dataRecord = iterator.next();
                combinedData.putAll(dataRecord.getAllFields());
            }

            return new DataRecordImpl(combinedData);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class CompositeIteratorOuter implements Iterator<DataRecord> {
        private final Iterator<? extends DataRecord>[] iterators;

        CompositeIteratorOuter(final Iterator<? extends DataRecord>... iterators) {
            this.iterators = iterators;
        }

        @Override
        public boolean hasNext() {
            for (final Iterator<? extends DataRecord> iterator : iterators) {
                if (iterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public DataRecord next() {
            final Map<String, Object> combinedData = Maps.newHashMap();
            for (final Iterator<? extends DataRecord> iterator : iterators) {
                if (iterator.hasNext()) {
                    final DataRecord dataRecord = iterator.next();
                    combinedData.putAll(dataRecord.getAllFields());
                }
            }

            return new DataRecordImpl(combinedData);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
