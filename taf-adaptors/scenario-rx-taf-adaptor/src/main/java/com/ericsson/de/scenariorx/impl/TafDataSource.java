package com.ericsson.de.scenariorx.impl;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.de.scenariorx.impl.Bridge.checkRxState;
import static com.ericsson.de.scenariorx.impl.StackTraceFilter.filterFrameworkStackTrace;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.datasource.CyclicDataSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceOptions;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.management.TafContext;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecordTransformer;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;

public class TafDataSource {

    public static final String ERROR_NOT_FOUND = "Unable to find DataSource: ";
    public static final String ERROR_IMPLICIT_SHARING = "To avoid confusion it is strongly not recommended to implicitly make " +
            "Data Source shared for all flows. To achieve the same functionality use dataSource(\"name\").shared() " +
            "when defining Data Source for the flow. This will share Data Source in the scope of one flow execution";
    public static final String ERROR_IMPLICIT_CYCLING = "Implicit cycling is prohibited. To achieve the same " +
            "functionality use dataSource(\"name\").cyclic() when defining Data Source for the flow. " +
            "This will make Data Source cyclic in the scope of one flow execution";
    public static final String ERROR_EMPTY = "TAF Data Source can't be empty";

    private static final Logger logger = LoggerFactory.getLogger(TafDataSource.class);

    private static Function<? super DataRecord, ? extends RxDataRecord> tafToRxDataRecord() {
        return new Function<DataRecord, RxDataRecord>() {
            @Override
            public RxDataRecord apply(DataRecord input) {
                return new TafDataRecordAdapter(input);
            }
        };
    }

    public static class TafDataSourceBridge<T extends DataRecord> extends TafDataSourceDefinition<T> {
        private TestDataSource<T> dataSource;

        public TafDataSourceBridge(String tafName, TestDataSource<T> dataSource, Class<?> type) {
            super(tafName, type);
            this.dataSource = dataSource;
        }

        @Override
        protected TestDataSource<T> getTafDataSource() {
            return dataSource;
        }

        @Override
        public RxDataSource<T> newDefinition() {
            return new TafDataSourceBridge<>(name, dataSource, getType());
        }
    }

    public static class TafDataSourceProviderDefinition<T extends DataRecord> extends TafDataSourceDefinition<T> {
        public TafDataSourceProviderDefinition(String tafName, Class<?> type) {
            super(tafName, type);
        }

        protected TestDataSource<T> getTafDataSource() {
            List<DataSourceAdapter> allDataSourceAdapters = ServiceRegistry.getAllDataSourceAdapters();

            for (DataSourceAdapter dataSourceAdapter : allDataSourceAdapters) {
                Optional<TestDataSource<DataRecord>> tafDataSource
                        = dataSourceAdapter
                        .<DataRecord>provide(name,
                                null,
                                null,
                                null,
                                DataRecord.class);

                if (tafDataSource.isPresent()) {
                    //TestDataSource<DataRecord> dataSource = tafDataSource.get();
                    //TafContext.setDataSource(name, dataSource);
                    //return (TestDataSource<T>) dataSource;
                    return (TestDataSource<T>) tafDataSource.get();
                }
            }
            throw filterFrameworkStackTrace(new IllegalArgumentException(ERROR_NOT_FOUND + name));
        }

        @Override
        public RxDataSource<T> newDefinition() {
            return new TafDataSourceProviderDefinition<>(name, getType());
        }
    }

    protected abstract static class TafDataSourceDefinition<T extends DataRecord> extends RxIterableDataSource<T> {
        public TafDataSourceDefinition(String tafName, Class<?> type) {
            super(tafName, type);
        }

        @Override
        public Iterator<RxDataRecord> getIterator() {
            TestDataSource<T> dataSource = getTafDataSource();

            validateImplicitSharingAndCycling(dataSource);
            checkRxState(dataSource.iterator().hasNext(), ERROR_EMPTY);

            return Iterators.transform(dataSource.iterator(), tafToRxDataRecord());
        }

        protected abstract TestDataSource<T> getTafDataSource();

        @Override
        public RxDataRecordTransformer getDataRecordTransformer() {
            return new TafDataRecordTransformer(name);
        }

        private void validateImplicitSharingAndCycling(TestDataSource dataSource) {
            while (dataSource != null) {
                if (shared(dataSource)) {
                    logger.warn(ERROR_IMPLICIT_SHARING);
                }
                checkRxState(notCyclic(dataSource), ERROR_IMPLICIT_CYCLING);
                dataSource = dataSource.getSource();
            }
        }

        private boolean shared(TestDataSource dataSource) {
            DataSourceOptions options = dataSource.getClass().getAnnotation(DataSourceOptions.class);
            return options != null && options.shared();
        }

        private boolean notCyclic(TestDataSource dataSource) {
            return !CyclicDataSource.class.isAssignableFrom(dataSource.getClass());
        }

        @Override
        public abstract RxDataSource<T> newDefinition();
    }

    static class TafDataRecordAdapter implements RxDataRecord {
        final DataRecord source;

        TafDataRecordAdapter(DataRecord source) {
            this.source = source;
        }

        @Override
        public <T> T getFieldValue(String name) {
            return source.getFieldValue(name);
        }

        @Override
        public Map<String, Object> getAllFields() {
            return source.getAllFields();
        }
    }
}
