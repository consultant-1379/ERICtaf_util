package com.ericsson.cifwk.taf.datasource;

/**
 * Data record that allows custom field definition. For internal TAF usage.
 */
public interface DataRecordModifier {

    /**
     * Sets the value for the data record field. If a field with such name already exists, its value will be overwritten.
     * @param name  data source field name
     * @param value data source field value
     * @return  the instance of this data record
     */
    DataRecordModifier setField(String name, Object value);

    /**
     * Copies all values from provided data record to current data record.
     * If a field with such name already exists, its value will be overwritten.
     * @param record data record to be copied
     * @return the instance of this data record
     */
    DataRecordModifier setFields(DataRecord record);



}
