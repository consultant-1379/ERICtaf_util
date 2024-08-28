package com.ericsson.cifwk.taf.scenario.impl.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

@API(Internal)
public interface ScenarioDataSource<T extends DataRecord> {
    Supplier<Optional<T>> supplier();

    Supplier<Optional<T>> supplier(Predicate<T> filter);

    boolean isShared();
}
