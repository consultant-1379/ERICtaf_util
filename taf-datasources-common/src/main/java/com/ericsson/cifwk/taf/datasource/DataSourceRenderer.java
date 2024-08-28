package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import java.io.Writer;

import com.ericsson.cifwk.meta.API;

/**
 *
 */
@API(Stable)
public interface DataSourceRenderer {

    void render(Writer writer, TestDataSource<DataRecord> dataSource);

}
