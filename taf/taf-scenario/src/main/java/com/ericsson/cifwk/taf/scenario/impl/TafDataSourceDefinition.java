package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.datasource.TestDataSourceFactory.fromServiceRegistry;
import static java.lang.String.format;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceOptions;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ProperlySharedScenarioDataSource;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSource;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static java.lang.String.format;

/**
 *
 */
@API(Internal)
public final class TafDataSourceDefinition<T extends DataRecord> implements DataSourceDefinition<T> {
    private final String name;
    private final DataSourcePredicateProvider predicateProvider;
    private final Class<T> dataRecordType;
    private final boolean allowEmpty;

    private static final Logger logger = LoggerFactory.getLogger(TafDataSourceDefinition.class);

    public TafDataSourceDefinition(String name, @Nullable DataSourcePredicateProvider predicateProvider, Class<T> dataRecordType, boolean allowEmpty) {
        this.name = name;
        this.predicateProvider = predicateProvider;
        this.dataRecordType = dataRecordType;
        this.allowEmpty = allowEmpty;
    }

    @Override
    public Supplier<Optional<T>> provideSupplier(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources) {
        ConcurrentHashMap<String, ScenarioDataSource> dataSources = context.getDataSources();

        if (!dataSources.keySet().contains(name)) {
            TestDataSource<T> dataSource = fromServiceRegistry(name, dataRecordType);

            if (isShared(dataSource)) {
                dataSources.putIfAbsent(name, new ProperlySharedScenarioDataSource<>(dataSource));
            } else {
                dataSources.putIfAbsent(name, new ScenarioDataSourceAdapter<>(dataSource));
            }
        }

        ScenarioDataSource<T> testDataSource = dataSources.get(name);

        logger.info(format("Providing Data Source `%s` shared=%s", name, testDataSource.isShared()));

        if (predicateProvider != null) {
            Predicate<DataRecord> predicate = predicateProvider.provide(context, parentDataSources);
            return testDataSource.supplier((Predicate<T>) predicate);
        } else {
            return testDataSource.supplier();
        }
    }

    private boolean isShared(TestDataSource dataSource) {
        while (dataSource != null) {
            DataSourceOptions options = dataSource.getClass().getAnnotation(DataSourceOptions.class);
            if (options != null && options.shared()) {
                return true;
            }
            dataSource = dataSource.getSource();
        }

        return false;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean allowsEmpty() {
        return allowEmpty;
    }

}
