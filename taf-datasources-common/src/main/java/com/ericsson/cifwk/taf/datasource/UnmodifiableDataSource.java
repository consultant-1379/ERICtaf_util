package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;

/**
 *
 */
@API(Stable)
abstract class UnmodifiableDataSource<T extends DataRecord> implements TestDataSource<T> {

    @Override
    public DataRecordModifier addRecord() {
        throw new UnsupportedOperationException();
    }
}
