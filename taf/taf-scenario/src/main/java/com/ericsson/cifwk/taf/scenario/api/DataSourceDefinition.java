package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioDataSourceContext;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 *
 */
@API(Stable)
public interface DataSourceDefinition<T extends DataRecord> {
    Supplier<Optional<T>> provideSupplier(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources);

    String getName();

    boolean allowsEmpty();
}
