package com.ericsson.cifwk.taf.inject;


import com.google.common.base.Optional;

/**
 * Bean container abstraction for plugging in dependency injection frameworks.
 */
public interface BeanManager {

    /**
     * Gets instance of a class from underlying bean container.
     *
     * @param type type of a bean
     * @param <T> class of a bean
     * @return optional bean instance
     */
    <T> Optional<T> getBean(Class<T> type);

}
