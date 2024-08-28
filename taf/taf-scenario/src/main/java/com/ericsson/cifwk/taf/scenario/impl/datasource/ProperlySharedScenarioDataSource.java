package com.ericsson.cifwk.taf.scenario.impl.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@API(Internal)
public class ProperlySharedScenarioDataSource<T extends DataRecord> implements ScenarioDataSource<T> {
    DataRecordSuppler<T> supplier;

    public ProperlySharedScenarioDataSource(Iterable<T> dataSource) {
        this.supplier = new DataRecordSuppler<>(dataSource.iterator());
    }

    @Override
    public Supplier<Optional<T>> supplier() {
        return supplier;
    }

    @Override
    public Supplier<Optional<T>> supplier(final Predicate<T> predicate) {
        return new Supplier<Optional<T>>() {
            @Override
            public Optional<T> get() {
                return supplier.get(predicate);
            }
        };
    }

    @Override
    public boolean isShared() {
        return true;
    }

    static class DataRecordSuppler<T> implements Supplier<Optional<T>> {
        List<T> discarded = Lists.newArrayList();
        Iterator<T> source;

        DataRecordSuppler(Iterator<T> source) {
            this.source = source;
        }

        @Override
        public Optional<T> get() {
            return get(Predicates.<T>alwaysTrue());
        }

        public synchronized Optional<T> get(Predicate<T> predicate) {
            for (Iterator<T> iterator = discarded.iterator(); iterator.hasNext(); ) {
                T next = iterator.next();
                if (predicate.apply(next)) {
                    iterator.remove();
                    return Optional.of(next);
                }
            }

            try {
                do {
                    if (!source.hasNext()) {
                        break;
                    }

                    T next = source.next();
                    if (predicate.apply(next)) {
                        return Optional.of(next);
                    }
                    discarded.add(next);
                } while (true);
            } catch (NoSuchElementException e) {
                //expected
            }
            return Optional.absent();
        }
    }
}
