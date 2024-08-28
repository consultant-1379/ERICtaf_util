package com.ericsson.cifwk.taf.scenario.datasources;

import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.management.TafRunnerContext;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.datasource.TestDataSourceFactory.createDataRecord;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         04/09/2017
 */
public class TrackingDataSourceAdapter implements DataSourceAdapter<DataRecord> {

    public static final String TRACKING_DATA_SOURCE = "trackingDataSource";

    private static final List<TrackingDataSource> dataSources = Lists.newArrayList();

    private static final Logger log  = getLogger(TrackingDataSourceAdapter.class);

    private static AtomicInteger counter = new AtomicInteger(1);

    public static List<TrackingDataSource> getDataSources() {
        return dataSources;
    }

    @Override
    public Optional<TestDataSource<DataRecord>> provide(String name, @Nullable Method method,
                                                        @Nullable DataSourceContext globalContext,
                                                        @Nullable Predicate<? super DataRecord> predicate,
                                                        @Nullable Class<DataRecord> dataRecordType) {

        return Optional.fromNullable(createDataSource(name));
    }

    private TestDataSource<DataRecord> createDataSource(String name) {
        if(name.startsWith(TRACKING_DATA_SOURCE)) {
            TrackingDataSource dataSource = new TrackingDataSource(name);

            TafRunnerContext context = TafRunnerContext.getContext();
            context.addCloseable(dataSource);
            dataSources.add(dataSource);

            return dataSource;
        } else {
            return null;
        }
    }

    public static class TrackingDataSource implements TestDataSource<DataRecord> {

        int id;
        boolean closed;
        List<DataRecord> records = Lists.newArrayList();
        private String name;

        public TrackingDataSource(String name) {
            this.name = name;
            id = counter.getAndAdd(1);

            Map<String, String> data = Maps.newConcurrentMap();
            data.put("name", "name");
            data.put("value", "value");

            records.add(createDataRecord(TRACKING_DATA_SOURCE, data));
        }

        @Override
        public void init(ConfigurationSource reader) {
        }

        @Override
        public Iterator<DataRecord> iterator() {
            return records.iterator();
        }

        @Override
        public DataRecordModifier addRecord() {
            return null;
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                log.warn("Data source already closed");
            }
            closed = true;
        }

        @Override
        public TestDataSource getSource() {
            return null;
        }

        public String getName() {
            return name;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
