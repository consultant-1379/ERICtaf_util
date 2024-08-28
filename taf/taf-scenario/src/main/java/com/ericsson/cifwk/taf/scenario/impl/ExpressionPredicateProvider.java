package com.ericsson.cifwk.taf.scenario.impl;/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.mvel.DataSourceFilterProcessor;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.Map;

@API(Internal)
public class ExpressionPredicateProvider implements DataSourcePredicateProvider {
    private String filter;

    public ExpressionPredicateProvider(String filter) {
        this.filter = filter;
    }

    @Override
    public Predicate<DataRecord> provide(ScenarioDataSourceContext context, final Map<String, DataRecord> parentDataSources) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord dataRecord) {
                Map<String, Map<String, Object>> parentDataRecords = Maps.transformEntries(parentDataSources,
                        new Maps.EntryTransformer<String, DataRecord, Map<String, Object>>() {
                            @Override
                            public Map<String, Object> transformEntry(String dataSourceName, DataRecord dataRecord) {
                                return dataRecord.getAllFields();
                            }
                        });

                Map<String, Object> allDataRecords = Maps.newLinkedHashMap();
                allDataRecords.putAll(parentDataRecords);
                allDataRecords.putAll(dataRecord.getAllFields());

                return DataSourceFilterProcessor.applyFilter(allDataRecords, filter);
            }
        };
    }
}
