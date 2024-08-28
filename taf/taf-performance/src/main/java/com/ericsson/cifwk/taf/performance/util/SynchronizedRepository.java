package com.ericsson.cifwk.taf.performance.util;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedRepository<T, U> {

    private final Supplier<U> supplier;
    private final HashMap<T, U> map;

    public SynchronizedRepository(Supplier<U> supplier) {
        this.supplier = supplier;
        map = new HashMap<>();
    }

    public synchronized U getOrCreate(T key) {
        U value = map.get(key);
        if (value == null) {
            value = supplier.get();
            map.put(key, value);
        }
        return value;
    }

    public synchronized Map<T, U> asMap() {
        return ImmutableMap.copyOf(map);
    }

}
