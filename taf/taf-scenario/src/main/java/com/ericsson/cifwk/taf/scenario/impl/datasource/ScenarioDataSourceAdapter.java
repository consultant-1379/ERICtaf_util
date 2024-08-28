package com.ericsson.cifwk.taf.scenario.impl.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

@API(Internal)
public class ScenarioDataSourceAdapter<T extends DataRecord> implements ScenarioDataSource<T> {
    private final Iterable<T> iterable;

    public ScenarioDataSourceAdapter(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Supplier<Optional<T>> supplier() {
        return new IteratorToSupplierAdapter(iterable.iterator());
    }

    @Override
    public Supplier<Optional<T>> supplier(Predicate<T> filter) {
        return new IteratorToSupplierAdapter(Iterators.filter(iterable.iterator(), filter));
    }

    @Override
    public boolean isShared() {
        return false;
    }

    public static class IteratorToSupplierAdapter<T extends DataRecord> implements Supplier<Optional<T>> {
        private Iterator<T> iterator;

        public IteratorToSupplierAdapter(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Optional<T> get() {
            try {
                if (!iterator.hasNext()) {
                    return Optional.absent();
                }

                return Optional.of(iterator.next());
            } catch (NoSuchElementException e) {
                return Optional.absent();
            }
        }
    }
}
