package com.ericsson.cifwk.taf.scenario.impl;

/*
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
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import java.util.LinkedHashMap;
import java.util.Map;

@API(Internal)
class DataSourceSupplier implements Supplier<Optional<LinkedHashMap<String, DataRecord>>> {
    private LinkedHashMap<String, Supplier<Optional<DataRecord>>> suppliers;

    DataSourceSupplier(LinkedHashMap<String, Supplier<Optional<DataRecord>>> suppliers) {
        this.suppliers = suppliers;
    }

    public static DataSourceSupplier join(LinkedHashMap<String, Supplier<Optional<DataRecord>>> suppliers) {
        return new DataSourceSupplier(suppliers);
    }

    @Override
    public Optional<LinkedHashMap<String, DataRecord>> get() {
        try {
            LinkedHashMap<String, DataRecord> next = Maps.newLinkedHashMap();

            for (Map.Entry<String, Supplier<Optional<DataRecord>>> recordSupplier : suppliers.entrySet()) {
                // If there are multiple shared Data Sources with different length, surplus record could be read here if
                // longer Data Source will appear first in iterator. In majority of cases its ok, since Data Records will
                // be lost only if one reads from shared Data Source after its used in flow, which is strange requirement.
                // ScenarioDataSourcesTest#shouldWorkWithMultipleSharedDataSourcesWithDifferentLength

                Optional<? extends DataRecord> optional = recordSupplier.getValue().get();
                if (!optional.isPresent()) {
                    return Optional.absent();
                }

                next.put(recordSupplier.getKey(), optional.get());
            }

            return Optional.of(next);
        } catch (Exception e) {
            throw new InvalidDataSourceException("provideDataSources method threw exception", e);
        }
    }
}
