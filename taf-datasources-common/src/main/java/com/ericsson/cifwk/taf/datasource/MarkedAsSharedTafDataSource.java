package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;

import java.io.IOException;
import java.util.Iterator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
@DataSourceOptions(shared = true)
public class MarkedAsSharedTafDataSource<T extends DataRecord> extends UnmodifiableDataSource<T> {
    private final TestDataSource<T> source;

    public MarkedAsSharedTafDataSource(TestDataSource<T> source) {
        this.source = source;
    }

    @Override
    public void init(ConfigurationSource reader) {
        source.init(reader);
    }

    @Override
    public Iterator<T> iterator() {
        return source.iterator();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public TestDataSource getSource() {
        return source;
    }
}
