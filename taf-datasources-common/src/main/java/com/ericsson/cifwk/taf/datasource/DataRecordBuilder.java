package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import java.util.Map;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Maps;

/**
 * This class allows a user to build a DataRecord, with as many fields as they want.
 */
@API(Stable)
public final class DataRecordBuilder {

    private Map<String, Object> data = Maps.newHashMap();

    /**
     * Add a field to the data record
     * @param field The field identifier
     * @param value the value for the field
     * @return the builder
     */
    public DataRecordBuilder setField(String field, Object value){
        data.put(field, value);
        return this;
    }

    /**
     * Add the contents of the map as individual fields to the data record
     * @param map The Key, Value pairs to add to the data record
     * @return the builder
     */
    public DataRecordBuilder setFields(Map map){
        data.putAll(map);
        return this;
    }

    /**
     * Add the contents of each of the data records as individual fields to the data record
     * @param records a list of data records
     * @return the builder
     */
    public DataRecordBuilder setFields(DataRecord... records){
        for(DataRecord dataRecord : records){
            setFields(dataRecord.getAllFields());
        }
        return this;
    }

    /**
     * Build the DataRecord
     * @return a DataRecord which contains the fields set in the Builder
     */
    public DataRecord build(){
        return build(DataRecord.class);
    }

    /**
     * Build the DataRecord
     * @return a DataRecord which contains the fields set in the Builder
     * @param type
     */
    public <T extends DataRecord> T build(final Class<T> type){
        return DataRecordProxyFactory.createProxy(new DataRecordImpl(data), type);
    }
}
