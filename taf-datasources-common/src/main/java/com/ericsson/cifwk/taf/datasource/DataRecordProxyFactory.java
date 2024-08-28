package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.lang.reflect.Proxy;

import com.ericsson.cifwk.meta.API;

/**
 *
 */
@API(Internal)
public final class DataRecordProxyFactory {

    private DataRecordProxyFactory() {
    }

    public static <T extends DataRecord> T createProxy(DataRecord dataRecord, Class<T> type) {

        return type.cast(Proxy.newProxyInstance(
                   DataRecord.class.getClassLoader(),
                   new Class[] { type },
                   new DataRecordBridge(dataRecord)));
    }
}
