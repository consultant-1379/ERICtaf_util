package com.ericsson.cifwk.taf.scenario.api;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.Builder;
import com.ericsson.cifwk.taf.datasource.ColumnBindTransformer;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.impl.DataSourceBindTransformer;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public abstract class DataSourceDefinitionBuilder implements Builder<DataSourceDefinition> {
    protected String dataSourceName;
    protected List<DataRecordTransformer> dataRecordTransformers = Lists.newCopyOnWriteArrayList();
    protected boolean dataTransformerAddedAtLastStep = false;
    protected boolean allowsEmpty = false;

    public DataSourceDefinitionBuilder(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public List<DataRecordTransformer> getDataRecordTransformers() {
        return Collections.unmodifiableList(dataRecordTransformers);
    }

    public DataSourceDefinitionBuilder bindTo(String newName) {
        dataRecordTransformers.add(new DataSourceBindTransformer(dataSourceName, newName));
        dataTransformerAddedAtLastStep = true;
        return this;
    }

    public DataSourceDefinitionBuilder bindColumn(String columnName, String newName) {
        dataRecordTransformers.add(new ColumnBindTransformer(dataSourceName, columnName, newName));
        dataTransformerAddedAtLastStep = true;
        return this;
    }

    public DataSourceDefinitionBuilder inTestStep(String testStepName) {
        if (!dataTransformerAddedAtLastStep) {
            throw new IllegalStateException(".inTestStep() must be called after .bindTo() or .bindColumn()");
        }

        dataRecordTransformers.get(dataRecordTransformers.size()-1).setTestStepName(testStepName);

        return this;
    }

    public DataSourceDefinitionBuilder allowEmpty() {
        this.allowsEmpty = true;
        dataTransformerAddedAtLastStep = false;
        return this;
    }
}
