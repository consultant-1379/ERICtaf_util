package com.ericsson.cifwk.taf.datasource;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Class used inside the withParameter method of a testStep to replace the original data with data from another field
 */
public class DataRecordFieldReplacement {

    private String replacementFieldHeader;
    private String replacementDataSourceName;
    private Object replacementFieldValue;
    private boolean fieldInAnotherDatasource = false;

    /**
     * At runtime the datarecord field will be replaced by a value from the same record
     *
     * @param replacementFieldHeader header value of field to use as replacement
     */
    public DataRecordFieldReplacement(String replacementFieldHeader) {
        this.replacementFieldHeader = replacementFieldHeader;
        fieldInAnotherDatasource = false;
    }

    /**
     * At runtime the datarecord field will be replaced by a field from a different datasource
     *
     * @param replacementDataSourceName the name of the datasource where the field is to be taken from
     * @param replacementFieldHeader    header value of field from alternative datasource to use as replacement
     */
    public DataRecordFieldReplacement(String replacementDataSourceName, String replacementFieldHeader) {
        this.replacementDataSourceName = replacementDataSourceName;
        this.replacementFieldHeader = replacementFieldHeader;
        fieldInAnotherDatasource = true;
    }

    public Object findColumnValue(LinkedHashMap<String, DataRecord> dataSourceRecords) {
        if (fieldInAnotherDatasource) {
            replacementFieldValue = findColumnValueFromAlternativeDataSource(dataSourceRecords);
        } else {
            replacementFieldValue = findColumnValueFromSameDataSource(dataSourceRecords);
        }
        return replacementFieldValue;
    }

    public Object findColumnValueFromSameDataSource(LinkedHashMap<String, DataRecord> dataSourceRecords) {
        Iterator<String> iterator = dataSourceRecords.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object actualDataRecord = getFieldValue(dataSourceRecords, key);
            if (actualDataRecord != null) {
                return actualDataRecord;
            }
        }
        throw new IllegalArgumentException("Replacement parameter '" + replacementFieldHeader + "' could not be found");
    }

    public Object findColumnValueFromAlternativeDataSource(LinkedHashMap<String, DataRecord> dataSourceRecords) {
        Iterator<String> iterator = dataSourceRecords.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.equals(replacementDataSourceName)) {
                Object actualDataRecord = getFieldValue(dataSourceRecords, key);
                if (actualDataRecord != null) {
                    return actualDataRecord;
                }
            }
        }
        throw new IllegalArgumentException("Replacement parameter '" + replacementFieldHeader + "' could not be found");
    }

    private Object getFieldValue(LinkedHashMap<String, DataRecord> dataSourceRecords, String key) {
        DataRecord actualDataRecord = dataSourceRecords.get(key);
        if (actualDataRecord.getAllFields().containsKey(replacementFieldHeader)) {
            return actualDataRecord.getFieldValue(replacementFieldHeader);
        }
        return null;
    }

    public Object getReplacementFieldValue() {
        return replacementFieldValue;
    }
}
