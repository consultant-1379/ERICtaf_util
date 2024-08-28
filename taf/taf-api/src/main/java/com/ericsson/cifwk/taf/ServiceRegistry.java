package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.eventbus.TestEventBusProvider;
import com.ericsson.cifwk.taf.inject.BeanManager;
import com.ericsson.cifwk.taf.spi.ConfigurationProvider;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.ericsson.cifwk.taf.spi.TestContextProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Entry API for all of the internal TAF services. Could be used in static context.
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/03/2016
 */
@API(Internal)
public final class ServiceRegistry {

    @VisibleForTesting
    static final TafServiceInitializer INSTANCE = new TafServiceInitializer();

    private ServiceRegistry() {
    }

    public static TestContextProvider getTestContextProvider() {
        return getUniqueServiceInstance(TestContextProvider.class);
    }

    public static ConfigurationProvider getConfigurationProvider() {
        return getUniqueServiceInstance(ConfigurationProvider.class);
    }

    public static TafAnnotationManagerFactory getTafAnnotationManagerFactory() {
        return getUniqueServiceInstance(TafAnnotationManagerFactory.class);
    }

    public static BeanManager getBeanManager(){
        return getUniqueServiceInstance(BeanManager.class);
    }

    public static <T> T getUniqueServiceInstance(Class<T> clazz) {
        return INSTANCE.getUniqueServiceInstance(clazz);
    }

    public static TestEventBus getTestEventBus() {
        return TestEventBusProvider.provide();
    }

    public static List<DataSourceAdapter> getAllDataSourceAdapters() {
        List<DataSourceAdapter> adapters = INSTANCE.getAllServiceInstances(DataSourceAdapter.class);
        if (adapters.isEmpty()) {
            throw new IllegalStateException("Cannot find any data source adapters!");
        }
        return adapters;
    }

    public static class TafServiceInitializer {

        private final Map<Class, List<?>> allServices = Maps.newHashMap();

        @VisibleForTesting
        <T> List<T> findAllServices(Class<T> clazz) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
            Iterator<T> serviceIterator = serviceLoader.iterator();
            List<T> results = Lists.newArrayList();
            if (serviceIterator.hasNext()) {
                do {
                    results.add(serviceIterator.next());
                } while (serviceIterator.hasNext());
            } else {
                throw new RuntimeException(String.format("No bindings found for %s. " +
                        "Check artifact containing required service is in classpath.", clazz.getName()));
            }

            return results;
        }

        @VisibleForTesting
        <T> void handleMultipleBindings(Set<String> serviceInstances, Class<T> clazz) {
            String error = String.format("Multiple bindings found for %s: [%s]", clazz.getName(), Joiner.on(",").join(serviceInstances));
            throw new MultipleServiceBindingsException(error);
        }

        @VisibleForTesting
        <T> Set<String> getClassNames(List<T> allServiceInstances) {
            Iterable<String> classNames = Iterables.transform(allServiceInstances, new Function<T, String>() {
                @Override
                public String apply(T o) {
                    return o.getClass().getName();
                }
            });
            return Sets.newHashSet(classNames);
        }

        @SuppressWarnings("unchecked")
        protected synchronized <T> T getUniqueServiceInstance(Class<T> clazz) {
            List<T> allServiceInstances = getAllServiceInstances(clazz);
            if (allServiceInstances.size() > 1) {
                handleMultipleBindings(getClassNames(allServiceInstances), clazz);
            }
            return allServiceInstances.get(0);
        }

        @SuppressWarnings("unchecked")
        protected synchronized <T> List<T> getAllServiceInstances(Class<T> clazz) {
            List<T> allServiceInstances = (List<T>) allServices.get(clazz);
            if (allServiceInstances == null) {
                allServiceInstances = findAllServices(clazz);
                allServices.put(clazz, allServiceInstances);
            }
            return allServiceInstances;
        }

    }
}
