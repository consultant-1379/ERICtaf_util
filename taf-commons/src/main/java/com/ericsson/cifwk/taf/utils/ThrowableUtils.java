package com.ericsson.cifwk.taf.utils;

/**
 * Created by ekonsla on 15/08/2016.
 */
public class ThrowableUtils {

    public static RuntimeException propagate(Throwable throwable) {
        propagateIfPossible(throwable);
        throw new RuntimeException(throwable);
    }

    private static void propagateIfPossible(Throwable throwable) {
        propagateIfInstanceOf(throwable, Error.class);
        propagateIfInstanceOf(throwable, RuntimeException.class);
    }

    private static <X extends Throwable> void propagateIfInstanceOf(Throwable throwable, Class<X> declaredType) throws X {
        if (throwable != null && declaredType.isInstance(throwable)) {
            throw declaredType.cast(throwable);
        }
    }
}
