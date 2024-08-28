package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 *
 */
public final class TafScopes {

    private TafScopes() {
        // hiding constructor
    }

    public static final Scope VUSER = new VUserScope();

    private static class VUserScope implements Scope {

        @Override
        public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
            return new Provider<T>() {
                @Override
                public T get() {
                    TestContext context = TafTestContext.getContext();
                    if (context == null) {
                        return unscoped.get();
                    }

                    T bean = context.getOperator(key.toString());
                    synchronized (context) {
                        if (null == bean) {
                            bean = unscoped.get();
                            context.setOperator(key.toString(), bean);
                        }
                    }

                    return bean;
                }
            };
        }

        @Override
        public String toString() {
            return "TafScopes.VUSER";
        }
    }

}
