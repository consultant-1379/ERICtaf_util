package com.ericsson.cifwk.taf.datasource;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Representation of TAF test data source.
 *
 * Can be used across framework to add data-driven functionality to tests.
 */
public interface TestDataSource<T extends DataRecord> extends Iterable<T>, Closeable {

    /**
     * Initializes this data source, opens connections, files etc. if needed.
     *
     * @param reader supplier of configuration properties
     */
    void init(ConfigurationSource reader);

    /**
     * Returns an iterator of {@link com.ericsson.cifwk.taf.datasource.DataRecord}s that are contained in this
     * data source.
     *
     * @return  iterator of this data source {@link com.ericsson.cifwk.taf.datasource.DataRecord}s.
     */
    @Override
    Iterator<T> iterator();

    /**
     * Adds a new record to the data source.
     *
     * @return the added record modifier.
     */
    DataRecordModifier addRecord();

    /**
     * Closes all data source resources (connections, files).
     * Being called when data source is not needed anymore.
     *
     * @throws IOException
     */
    @Override
    void close() throws IOException;

    /**
     * If data source is wrapped, unwrap data source
     */
     TestDataSource getSource();
}
