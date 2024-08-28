package com.ericsson.cifwk.taf.eiffel;

import com.ericsson.duraci.datawrappers.EventId;
import com.ericsson.duraci.datawrappers.ExecutionId;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         29/02/2016
 */
public class ExecutionEvent {

    private EventId eventId;
    private ExecutionId executionId;

    public ExecutionEvent(EventId eventId, ExecutionId executionId) {
        this.executionId = executionId;
        this.eventId = eventId;
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

    public EventId getEventId() {
        return eventId;
    }
}
