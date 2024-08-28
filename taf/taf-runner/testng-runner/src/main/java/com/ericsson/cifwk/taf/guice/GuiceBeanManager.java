package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.inject.BeanManager;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuiceBeanManager implements BeanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceBeanManager.class);

    @Inject
    static Injector injector;

    @Override
    public <T> Optional<T> getBean(Class<T> type) {
        Preconditions.checkNotNull(injector, "Accessing Guice injector before it is prepared");

        try {
            T bean = injector.getInstance(Key.get(type));
            return Optional.of(bean);
        } catch (Exception e) {
            LOGGER.warn("Failure to get a bean ", e);
            return Optional.absent();
        }
    }

}
