package com.ericsson.de.scenariorx.impl;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static java.lang.String.format;

import java.util.Collection;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordProxyFactory;
import com.ericsson.cifwk.taf.datasource.ObjectTypeConverter;
import com.ericsson.de.scenariorx.api.RxBasicDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecordTransformer;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * Transforms {@link RxDataRecord} to {@link DataRecord} to satisfy parameters of TAF Test Steps that expect
 * {@link DataRecord}, or interfaces that implement {@link DataRecord} (e.g. {@link NetworkNode})
 */
public class TafDataRecordTransformer<T extends DataRecord> implements RxDataRecordTransformer<T> {
    private final String dataSourceName;

    TafDataRecordTransformer(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public T transform(RxDataRecord dataRecord, Class<T> type) {
        DataRecordImpl tafDataRecord = new DataRecordImpl(dataSourceName, dataRecord.getAllFields());
        return DataRecordProxyFactory.createProxy(tafDataRecord, type);
    }

    public RxDataRecord transformRx(DataRecord tafDataRecord) {
        return RxBasicDataRecord.fromMap(tafDataRecord.getAllFields());
    }

    public boolean isCollectionOfDataRecords(Object returnedValue) {
        return returnedValue instanceof Collection &&
                Collection.class.cast(returnedValue).iterator().hasNext() &&
                DataRecord.class.isInstance(Collection.class.cast(returnedValue).iterator().next());
    }

    @Override
    public boolean canTransformTo(Class type) {
        return DataRecord.class.isAssignableFrom(type);
    }

    @Override
    public <U> U convert(String name, Object value, Class<U> targetType) {
        if (value == null) {
            return null;
        }

        U result = ObjectTypeConverter.map(value, targetType);
        Preconditions.checkArgument(result != null, format(
                "Unable to convert field `%s` of class `%s` to `%s",
                name,
                value.getClass().getSimpleName(),
                targetType.getSimpleName()));
        return result;
    }

    public Object convertDataRecordsToRx(Object object) {
        if (object instanceof DataRecord) {
            return transformRx(DataRecord.class.cast(object));
        } else if (isCollectionOfDataRecords(object)) {
            return Iterables.transform(Iterable.class.cast(object), new Function<Object, Object>() {
                @Override
                public Object apply(Object input) {
                    return transformRx(DataRecord.class.cast(input));
                }
            });
        }

        return object;
    }
}
