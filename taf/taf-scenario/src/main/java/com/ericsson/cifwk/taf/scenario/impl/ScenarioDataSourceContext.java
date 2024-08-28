package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSource;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ScenarioDataSourceContext {
    private final ConcurrentHashMap<String, ScenarioDataSource> dataSources = new ConcurrentHashMap<>();

    public DataSourceSupplier provideDataSources(DataSourceDefinition[] dataSources, Map<String, DataRecord> parentDataSources) {
        try {

            LinkedHashMap<String, Supplier<Optional<DataRecord>>> suppliers = new LinkedHashMap<>();

            for (DataSourceDefinition definition : dataSources) {
                Supplier<Optional<DataRecord>> supplier = definition.provideSupplier(this, parentDataSources);
                suppliers.put(definition.getName(), supplier);
            }

            return new DataSourceSupplier(suppliers);
        } catch (Exception e) {
            throw new InvalidDataSourceException("Error loading datasource due to error (see cause in stack trace)", e);
        }
    }

    public ConcurrentHashMap<String, ScenarioDataSource> getDataSources() {
        return dataSources;
    }
}
