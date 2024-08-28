package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.datasource.DataRecord;

import java.lang.reflect.Proxy;

class DataRecordProxyFactory {

    private DataRecordProxyFactory() {
    }

    public static <T extends DataRecord> T createProxy(DataRecord dataRecord, Class<T> type) {

        return type.cast(Proxy.newProxyInstance(
                DataRecord.class.getClassLoader(),
                new Class[]{type},
                new DataRecordBridge(dataRecord)));
    }
}
