/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.impl.DataSourcePredicateProvider;
import com.ericsson.cifwk.taf.scenario.impl.ExpressionPredicateProvider;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioDataSourceContext;
import com.ericsson.cifwk.taf.scenario.impl.TafDataSourceDefinition;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class TafDataSourceDefinitionBuilder<T extends DataRecord> extends DataSourceDefinitionBuilder {
    private DataSourcePredicateProvider predicateProvider = null;
    private Class<T> dataRecordType;

    public TafDataSourceDefinitionBuilder(String name, Class<T> dataRecordType) {
        super(name);
        this.dataRecordType = dataRecordType;
    }

    public TafDataSourceDefinitionBuilder withFilter(String filter) {
        if (!Strings.isNullOrEmpty(filter)) {
            this.predicateProvider = new ExpressionPredicateProvider(filter);
        }
        this.dataTransformerAddedAtLastStep = false;
        return this;
    }

    public TafDataSourceDefinitionBuilder withFilter(final Predicate<? super T> predicate) {
        this.predicateProvider = new DataSourcePredicateProvider() {
            @Override
            public Predicate<DataRecord> provide(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources) {
                return (Predicate<DataRecord>) predicate;
            }
        };
        this.dataTransformerAddedAtLastStep = false;
        return this;
    }

    public DataSourceDefinition<T> build() {
        return new TafDataSourceDefinition<>(dataSourceName, predicateProvider, dataRecordType, allowsEmpty);
    }
}
