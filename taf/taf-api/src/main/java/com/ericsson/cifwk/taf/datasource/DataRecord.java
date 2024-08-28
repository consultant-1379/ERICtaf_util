package com.ericsson.cifwk.taf.datasource;

import java.util.Map;

/**
 * Data source row representation.
 * One data row is a set of key -> value objects.
 */
public interface DataRecord {

    /**
     * Returns the data source field value
     *
     * @param name  field name
     * @return  field value
     */
    <T> T getFieldValue(String name);

    /**
     * Returns a map of all data source fields
     *
     * @return  a map of all data source fields (mapping field name to field value)
     */
    Map<String, Object> getAllFields();

    /**
     * Gets name of the data source this record belongs to
     *
     * @return name of the data source
     */
    String getDataSourceName();

    /**
     * Transform a DataRecord into a bean
     * @param beanClass
     * @return An instance of the beanClass with the properties of this dataRecordObject
     */
    <T> T transformTo(Class<T> beanClass);

}
