package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Simple event bus for sending TAF test events with priorities.</p>
 * <p>Use {@link Subscribe} to subscribe to receiving those events.</p>
 *
 * <p>Partially source was taken from Guava 17.0 EventBus. Some modifications were applied to support subscriber priorities which
 * are not supported in Guava Event Bus (see https://github.com/google/guava/issues/838).
 * It was impossible to extend Guava Event Bus, because it's still in beta, and doesn't have means to extend it in a safe way.</p>
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
class TestEventBusImpl implements TestEventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEventBus.class);

    /**
     * A thread-safe cache for keeping the test class hierarchy.
     */
    private static final LoadingCache<Class<?>, Set<Class<?>>> flattenHierarchyCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(new CacheLoader<Class<?>, Set<Class<?>>>() {
                        @SuppressWarnings({"unchecked", "rawtypes"}) // safe cast
                        @Override
                        public Set<Class<?>> load(Class<?> concreteClass) {
                            return (Set) TypeToken.of(concreteClass).getTypes().rawTypes();
                        }
                    });

    /**
     * All registered event subscribers, indexed by event class name, and sorted by priority.
     */
    private final SetMultimap<String, EventSubscriber> subscribersByClassName = TreeMultimap.create();
    private final ReadWriteLock subscribersByTypeLock = new ReentrantReadWriteLock();

    private final String identifier;
    /**
     * <code>true</code> if sent events must have appropriate subscribers
     */
    private boolean eventsMustHaveSubscribers = false;

    /** queues of events for the current thread to dispatch */
    private final ThreadLocal<Queue<EventWithSubscriber>> eventsToDispatch =
            new ThreadLocal<Queue<EventWithSubscriber>>() {
                @Override protected Queue<EventWithSubscriber> initialValue() {
                    return new LinkedList<>();
                }
            };

    /** true if the current thread is currently dispatching an event */
    private final ThreadLocal<Boolean> isDispatching =
            new ThreadLocal<Boolean>() {
                @Override protected Boolean initialValue() {
                    return false;
                }
            };

    /**
     * Creates a new TestEventBus named "default".
     */
    public TestEventBusImpl() {
        this("default");
    }

    /**
     * Creates a new TestEventBus with the given {@code identifier}.
     *
     * @param identifier  a brief name for this bus, for logging purposes.  Should
     *                    be a valid Java identifier.
     */
    public TestEventBusImpl(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Registers all subscriber methods on {@code object} to receive events.
     * @param subscriber  object whose subscriber methods should be registered.
     */
    public void register(Object subscriber) {
        LOGGER.debug("Registering subscriber {}", subscriber);
        Multimap<String, EventSubscriber> methodsInListener = SubscriberProvider.getAllSubscribers(subscriber);
        boolean hasSubscribingMethods = !methodsInListener.isEmpty();
        if (!hasSubscribingMethods) {
            warnAboutEmptySubscriberClass(subscriber);
        } else {
            LOGGER.debug("Subscriber methods found in {}: {}", subscriber, methodsInListener);
        }
        subscribersByTypeLock.writeLock().lock();
        try {
            boolean addedSomething = subscribersByClassName.putAll(methodsInListener);
            if (!addedSomething && hasSubscribingMethods) {
                // It's possible only when the same object is already registered
                warnAboutDuplicateRegistration(subscriber);
            }
        } finally {
            subscribersByTypeLock.writeLock().unlock();
        }
    }

    @VisibleForTesting
    void warnAboutEmptySubscriberClass(Object subscriber) {
        LOGGER.warn("Subscriber class {} doesn't have any methods annotated with {}, it won't receive any TAF test events",
                subscriber.getClass(), Subscribe.class.getName());
    }

    @VisibleForTesting
    void warnAboutDuplicateRegistration(Object subscriber) {
        LOGGER.warn("Subscriber class {} is already registered. This registration will be ignored", subscriber.getClass());
    }

    /**
     * Unregisters all subscriber methods on a registered {@code object}.
     * @param subscriber  object whose subscriber methods should be unregistered.
     */
    public void unregister(Object subscriber) {
        Multimap<String, EventSubscriber> methodsInListener = SubscriberProvider.getAllSubscribers(subscriber);
        for (Map.Entry<String, Collection<EventSubscriber>> entry : methodsInListener.asMap().entrySet()) {
            String eventClassName = entry.getKey();
            subscribersByTypeLock.writeLock().lock();
            try {
                subscribersByClassName.removeAll(eventClassName);
            } finally {
                subscribersByTypeLock.writeLock().unlock();
            }
        }
    }

    /**
     * Posts an event to all registered subscribers.  This method will return
     * successfully after the event has been posted to all subscribers, and
     * regardless of any exceptions thrown by subscribers.
     *
     * @param event  event to post.
     */
    public void post(TestEvent event) {
        LOGGER.trace("Sending event " + event);
        Set<Class<?>> dispatchTypes = getFullClassHierarchy(event.getClass());
        boolean sent = false;
        for (Class<?> eventType : dispatchTypes) {
            subscribersByTypeLock.readLock().lock();
            try {
                Set<EventSubscriber> wrappers = subscribersByClassName.get(eventType.getName());
                if (!wrappers.isEmpty()) {
                    sent = true;
                    for (EventSubscriber wrapper : wrappers) {
                        enqueueEvent(event, wrapper);
                    }
                }
            } finally {
                subscribersByTypeLock.readLock().unlock();
            }
        }

        if (!sent && eventsMustHaveSubscribers) {
            LOGGER.warn("{} Event {} was not sent - no subscribers", getBusId(), event);
        }

        dispatchQueuedEvents();
    }

    public boolean isEventsMustHaveSubscribers() {
        return eventsMustHaveSubscribers;
    }

    public void setEventsMustHaveSubscribers(boolean eventsMustHaveSubscribers) {
        this.eventsMustHaveSubscribers = eventsMustHaveSubscribers;
    }

    /**
     * Queue the {@code event} for dispatch during
     * {@link #dispatchQueuedEvents()}. Events are queued in-order of occurrence
     * so they can be dispatched in the same order.
     */
    void enqueueEvent(Object event, EventSubscriber subscriber) {
        eventsToDispatch.get().offer(new EventWithSubscriber(event, subscriber));
    }

    /**
     * Drain the queue of events to be dispatched. As the queue is being drained,
     * new events may be posted to the end of the queue.
     */
    void dispatchQueuedEvents() {
        // don't dispatch if we're already dispatching, that would allow reentrancy
        // and out-of-order events. Instead, leave the events to be dispatched
        // after the in-progress dispatch is complete.
        if (isDispatching.get()) {
            return;
        }

        isDispatching.set(true);
        try {
            Queue<EventWithSubscriber> events = eventsToDispatch.get();
            EventWithSubscriber eventWithSubscriber;
            while ((eventWithSubscriber = events.poll()) != null) {
                dispatch(eventWithSubscriber.event, eventWithSubscriber.subscriber);
            }
        } finally {
            isDispatching.remove();
            eventsToDispatch.remove();
        }
    }

    /**
     * Dispatches {@code event} to the subscriber in {@code wrapper}.
     * @param event  event to dispatch.
     * @param wrapper  wrapper that will call the subscriber.
     */
    void dispatch(Object event, EventSubscriber wrapper) {
        try {
            wrapper.handleEvent(event);
        } catch (InvocationTargetException e) {
            LOGGER.error(String.format("%s Got exception while trying to handle event %s", getBusId(), event), e.getCause());
        }
    }

    public String getBusId() {
        return String.format("[event-bus-%s]", identifier);
    }

    /**
     * Flattens a class's type hierarchy into a set of Class objects.  The set
     * will include all superclasses (transitively), and all interfaces
     * implemented by these superclasses.
     *
     * @param concreteClass  class whose type hierarchy will be retrieved.
     * @return {@code clazz}'s complete type hierarchy, flattened and uniqued.
     */
    @VisibleForTesting
    Set<Class<?>> getFullClassHierarchy(Class<?> concreteClass) {
        try {
            return flattenHierarchyCache.getUnchecked(concreteClass);
        } catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    static class EventWithSubscriber {
        final Object event;
        final EventSubscriber subscriber;
        public EventWithSubscriber(Object event, EventSubscriber subscriber) {
            this.event = checkNotNull(event);
            this.subscriber = checkNotNull(subscriber);
        }
    }
}
