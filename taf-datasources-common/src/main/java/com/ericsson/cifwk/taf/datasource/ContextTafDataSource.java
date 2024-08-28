package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.util.Iterator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Context data source (kept in test context)
 */
@API(Internal)
public class ContextTafDataSource extends UnmodifiableDataSource<DataRecord> {

    private final String name;
    private TestDataSource<? extends DataRecord> dataSource;

    public ContextTafDataSource(String name, TestDataSource<? extends DataRecord> dataSource) {
        this.dataSource = dataSource;
        this.name = name;
    }

    @Override
    public void init(ConfigurationSource reader) {
        // nothing to do
    }

    @Override
    public Iterator<DataRecord> iterator() {
        Iterable<DataRecord> tafDataRecords = Iterables.transform(dataSource, new Function<DataRecord, DataRecord>() {
            @Override
            public DataRecord apply(DataRecord input) {
                final DataRecord dataRecord = TestDataSourceFactory.createDataRecord(input.getAllFields());
                return DataRecordProxyFactory.createProxy(dataRecord, DataRecord.class);
            }
        });
        return tafDataRecords.iterator();
    }

    @Override
    public void close() throws IOException {
        dataSource = null;
    }

    @Override
    public TestDataSource getSource() {
        return dataSource;
    }

    @Override
    public String toString() {
        return "ContextTafDataSource{" +
                "name='" + name + '\'' +
                ", dataSource=" + dataSource +
                '}';
    }
}
