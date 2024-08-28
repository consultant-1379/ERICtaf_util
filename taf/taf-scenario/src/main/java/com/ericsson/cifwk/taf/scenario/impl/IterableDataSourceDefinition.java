package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSourceAdapter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *
 */
@API(Internal)
public final class IterableDataSourceDefinition implements DataSourceDefinition<DataRecord> {

    private final String name;
    private final Iterable<Map<String, Object>> iterable;

    public IterableDataSourceDefinition(String name, Iterable<Map<String, Object>> iterable) {
        this.name = name;
        this.iterable = iterable;
    }

    @Override
    public Supplier<Optional<DataRecord>> provideSupplier(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources) {
        Iterable<DataRecord> transformer = Iterables.transform(iterable, new Function<Map<String, Object>, DataRecord>() {
            @Override
            public DataRecord apply(Map<String, Object> input) {
                return new DataRecordImpl(input);
            }
        });

        return new ScenarioDataSourceAdapter.IteratorToSupplierAdapter(transformer.iterator());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean allowsEmpty() {
        return false;
    }

}
