package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;

import java.io.IOException;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *
 */
@API(Internal)
public abstract class AbstractCompositeDataSource<T extends DataRecord> extends UnmodifiableDataSource<T> {

    protected final TestDataSource[] dataSources;

    public AbstractCompositeDataSource(TestDataSource... dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public void init(ConfigurationSource reader) {
        for (TestDataSource dataSource : dataSources) {
            dataSource.init(reader);
        }
    }

    @Override
    public void close() throws IOException {
        for (TestDataSource dataSource : dataSources) {
            dataSource.close();
        }
    }

    @Override
    public TestDataSource getSource() {
        return null;
    }
}
