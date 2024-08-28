package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static java.lang.String.format;

/**
 * Wrapper, which reads data source in a loop. If data source is depleted it starts over.
 */
@API(Internal)
public final class CyclicDataSource<T extends DataRecord> extends UnmodifiableDataSource<T> {
    private static final Logger logger = LoggerFactory.getLogger(CyclicDataSource.class);

    /**
     * Information if data pool processing is stopped; useful only for
     * {@link com.ericsson.cifwk.taf.data.pool.DataPoolStrategy#REPEAT_UNTIL_STOPPED}
     */
    private volatile boolean stopped = false;

    private TestDataSource<T> source;

    public CyclicDataSource(TestDataSource<T> source) {
        this.source = source;
    }

    @Override
    public void init(ConfigurationSource reader) {
        // nothing to do
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> iterator = source.iterator();
        if (!iterator.hasNext()) {
            return Collections.emptyIterator();
        }

        return new UnmodifiableIterator<T>() {
            List<T> data = Lists.newArrayList();
            Iterator<T> cycle;
            AtomicInteger returnedElements = new AtomicInteger(0);

            @Override
            public boolean hasNext() {
                return !stopped;
            }

            @Override
            public T next() {
                if (stopped) {
                    throw new NoSuchElementException();
                }

                T next;

                if (iterator.hasNext()) {
                    next = iterator.next();
                    data.add(next);
                } else {
                    if (cycle == null) {
                        cycle = Iterators.cycle(data);
                    }
                    next = cycle.next();
                }

                if (returnedElements.incrementAndGet() > 1000) {
                    logger.error(format("Cyclic Data Source `%s` returned over 1000 records. Please make sure if this" +
                               " expected, or it wasn't stopped properly.", source));
                    returnedElements.set(0);
                }

                return next;
            }
        };
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public String toString() {
        return "CyclicDataSource{" +
                   "source=" + source +
                   '}';
    }

    public void stop() {
        this.stopped = true;
    }

    public TestDataSource<? extends DataRecord> getSource() {
        return source;
    }
}
