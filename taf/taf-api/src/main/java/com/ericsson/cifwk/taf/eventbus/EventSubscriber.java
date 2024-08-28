package com.ericsson.cifwk.taf.eventbus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps a single-argument subscriber method on a specific object.
 */
class EventSubscriber implements Comparable<EventSubscriber> {

    private final Object target;

    private final Method method;

    private final Integer priority;

    /**
     * Creates an unprioritized subscriber
     *
     * @param target
     * @param method
     */
    EventSubscriber(Object target, Method method) {
        this(target, method, Integer.MAX_VALUE);
    }

    EventSubscriber(Object target, Method method, Integer priority) {
        Preconditions.checkNotNull(target, "EventSubscriber target object cannot be null.");
        Preconditions.checkNotNull(method, "EventSubscriber method cannot be null.");
        Preconditions.checkNotNull(priority, "Priority cannot be null.");

        this.target = target;
        this.method = method;
        this.priority = priority;

        // In case it's not public
        method.setAccessible(true);
    }

    /**
     * Invokes the wrapped subscriber method to handle {@code event}.
     *
     * @param event  event to handle
     * @throws InvocationTargetException  if the wrapped method throws any
     *     {@link Throwable} that is not an {@link Error} ({@code Error} instances are
     *     propagated as-is).
     */
    void handleEvent(Object event) throws InvocationTargetException {
        checkNotNull(event);
        try {
            method.invoke(target, new Object[]{event});
        } catch (IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new Error("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "EventSubscriber{" +
                "target=" + target +
                ", method=" + method +
                ", priority=" + (priority.equals(Integer.MAX_VALUE) ? "none" : priority) +
                '}';
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + method.hashCode();
        result = 31 * result + priority.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventSubscriber) {
            EventSubscriber that = (EventSubscriber) obj;
            return (this == that) || compareTo(that) == 0;
        }
        return false;
    }

    @Override
    public final int compareTo(EventSubscriber that) {
        int prioritiesCompared = comparePriorityWith(that);
        int targetsCompared = compareTargetWith(that);
        int targetMethodsCompared = compareMethodWith(that);

        return (prioritiesCompared != 0) ? prioritiesCompared
                : (targetsCompared != 0 ? targetsCompared : targetMethodsCompared);
    }

    @VisibleForTesting
    int comparePriorityWith(EventSubscriber that) {
        return this.priority.compareTo(that.priority);
    }

    @VisibleForTesting
    int compareTargetWith(EventSubscriber that) {
        if (target.equals(that.target)) {
            return 0;
        } else {
            return target.getClass().getName().compareTo(that.target.getClass().getName());
        }
    }

    @VisibleForTesting
    int compareMethodWith(EventSubscriber that) {
        return (method.equals(that.method)) ? 0 : method.toString().compareTo(that.method.toString());
    }

}