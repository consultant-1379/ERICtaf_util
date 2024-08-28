package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * <p>Simple event bus for sending TAF test events.</p>
 * <p>Use {@link Subscribe} to subscribe to receiving those events.</p>
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
@API(Internal)
public interface TestEventBus {

    /**
     * Registers all subscriber methods on {@code object} to receive events.
     * @param subscriber  object whose subscriber methods should be registered.
     */
    void register(Object subscriber);

    /**
     * Unregisters all subscriber methods on a registered {@code object}.
     * @param subscriber  object whose subscriber methods should be unregistered.
     */
    void unregister(Object subscriber);

    /**
     * Posts an event to all registered subscribers.  This method will return
     * successfully after the event has been posted to all subscribers, and
     * regardless of any exceptions thrown by subscribers.
     *
     * @param event  event to post.
     */
    void post(TestEvent event);

    /**
     * @return bus ID
     */
    String getBusId();

}
