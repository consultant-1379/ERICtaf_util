package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.meta.API;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * To be replaced with Java 8 streams.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 13.04.2016
 */
@API(Internal)
class CollectionUtils {

    public static <T> List<T> filter(List<T> list, Java7Predicate<? super T> predicate) {
        List<T> result = new ArrayList<>();
        for(T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static <T, R> List<R> transform(List<T> list, Java7Function<? super T, ? extends R> function) {
        List<R> result = new ArrayList<>();
        for(T item : list) {
            result.add(function.apply(item));
        }
        return result;
    }

    private static void requireNonNull(Object object) {
        if (object == null) {
            throw new RuntimeException("Null value is not allowed");
        }
    }

    public static abstract class Java7Predicate<T> {

        public abstract boolean test(T t);

        public Java7Predicate<T> and(final Java7Predicate<? super T> other) {
            requireNonNull(other);
            return new Java7Predicate<T>(){
                @Override
                public boolean test(T t) {
                    return Java7Predicate.this.test(t) && other.test(t);
                }
            };
        }

        public Java7Predicate<T> or(final Java7Predicate<? super T> other) {
            requireNonNull(other);
            return new Java7Predicate<T>(){
                @Override
                public boolean test(T t) {
                    return Java7Predicate.this.test(t) || other.test(t);
                }
            };
        }

        public Java7Predicate<T> negate() {
            return new Java7Predicate<T>(){
                @Override
                public boolean test(T t) {
                    return !Java7Predicate.this.test(t);
                }
            };
        }

    }

    public abstract static class Java7Function<T, R> {

        public abstract R apply(T t);

        // before(V -> T) this(T -> R)
        public <V> Java7Function<V, R> compose(final Java7Function<? super V, ? extends T> before) {
            requireNonNull(before);
            return new Java7Function<V, R>(){
                @Override
                public R apply(V v) {
                    return Java7Function.this.apply(before.apply(v));
                }
            };
        }

        // this(T -> R) after(R -> V)
        public <V> Java7Function<T, V> andThen(final Java7Function<? super R, ? extends V> after) {
            requireNonNull(after);
            return new Java7Function<T, V>(){
                @Override
                public V apply(T t) {
                    return after.apply(Java7Function.this.apply(t));
                }
            };
        }

    }

}
