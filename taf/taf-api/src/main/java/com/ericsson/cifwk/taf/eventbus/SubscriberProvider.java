package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Utility for finding methods marked as test event subscribers
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 23/08/2016
 */
class SubscriberProvider {

    /**
     * A thread-safe cache that contains the mapping from each class to all methods in that class and
     * all super-classes, that are annotated with {@code @Subscribe}. The cache is shared across all
     * instances of this class; this greatly improves performance if multiple event bus instances are
     * created and objects of the same class are registered on all of them.
     */
    private static final LoadingCache<Class<?>, Set<Method>> subscriberMethodsCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(new CacheLoader<Class<?>, Set<Method>>() {
                        @Override
                        public Set<Method> load(Class<?> subscriberClass) throws Exception {
                            return getAnnotatedMethods(subscriberClass);
                        }
                    });

    static Multimap<String, EventSubscriber> getAllSubscribers(Object listener) {
        Multimap<String, EventSubscriber> result = TreeMultimap.create();
        Set<Method> methods;
        try {
            methods = subscriberMethodsCache.getUnchecked(listener.getClass());
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
        for (Method aMethod :  methods) {
            addEventSubscriberToMap(result, listener, aMethod);
        }
        return result;
    }

    private static void addEventSubscriberToMap(Multimap<String, EventSubscriber> map, Object listener, Method aMethod) {
        Subscribe subscribeAnnotation = aMethod.getAnnotation(Subscribe.class);
        Priority priorityAnnotation = aMethod.getAnnotation(Priority.class);

        EventSubscriber eventSubscriber;
        if (priorityAnnotation != null) {
            int priority = priorityAnnotation.value();
            Preconditions.checkArgument(priority > 0, "Subscriber priority should be > 0");
            eventSubscriber = new EventSubscriber(listener, aMethod, priority);
        } else {
            eventSubscriber = new EventSubscriber(listener, aMethod);
        }

        Class<?>[] parameterTypes = aMethod.getParameterTypes();
        Class<?> eventType = parameterTypes[0];
        map.put(eventType.getName(), eventSubscriber);
    }

    @VisibleForTesting
    static Set<Method> getAnnotatedMethods(Class<?> subscriberClass) {
        Set<Method> result = Sets.newHashSet();
        Set<? extends Class<?>> allHierarchy = TypeToken.of(subscriberClass).getTypes().rawTypes();
        for (Class<?> aClass : allHierarchy) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method aMethod : declaredMethods) {
                if (aMethod.isAnnotationPresent(Subscribe.class)) {
                    // Ensure that method accepts only one argument that is TAF test event
                    Class<?>[] parameterTypes = aMethod.getParameterTypes();
                    Preconditions.checkArgument(
                            parameterTypes.length == 1 && parameterTypes[0] != null &&
                                    TestEvent.class.isAssignableFrom(parameterTypes[0]),
                            String.format("Method %s has a wrong signature; TAF Test event subscriber has to accept only one non-null parameter that extends %s",
                                    aMethod, TestEvent.class.getName()));
                    result.add(aMethod);
                }
            }
        }
        return result;
    }

}
