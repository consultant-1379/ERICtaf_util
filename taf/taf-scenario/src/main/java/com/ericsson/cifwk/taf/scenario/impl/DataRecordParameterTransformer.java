package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordFieldReplacement;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class DataRecordParameterTransformer implements DataRecordTransformer {

    private Map<String, Object> parameters;

    public DataRecordParameterTransformer(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public LinkedHashMap<String, DataRecord> apply(LinkedHashMap<String, DataRecord> dataSourcesRecords, String testStepName) {
        for (String dataSourceName : dataSourcesRecords.keySet()) {
            for (String parameter : parameters.keySet()) {
                if (parameter.startsWith(dataSourceName + ".")) {
                    String field = parameter.substring(dataSourceName.length() + 1);
                    DataRecord edited = edit(dataSourcesRecords.get(dataSourceName), field, parameters.get(parameter));
                    dataSourcesRecords.put(dataSourceName, edited);
                }
            }
        }
        return dataSourcesRecords;
    }

    @Override
    public void setTestStepName(String testStepName) {
        //do nothing
    }

    @Override
    public String getTestStepName() {
        return null;
    }

    private DataRecord edit(DataRecord dataRecord, String field, Object newValue) {
        Map<String, Object> allFields = new HashMap<>(dataRecord.getAllFields());
        if (newValue instanceof DataRecordFieldReplacement) {
            newValue = ((DataRecordFieldReplacement) newValue).getReplacementFieldValue();
        }
        allFields.put(field, newValue);
        return new DataRecordImpl(dataRecord.getDataSourceName(), allFields);
    }
}
