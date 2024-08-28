package com.ericsson.cifwk.taf.eiffel.exception;

public class UnknownParentEventException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8922595459061226863L;

    public UnknownParentEventException() {
        super();
    }

    public UnknownParentEventException(String message) {
        super(message);
    }

    public UnknownParentEventException(String message, Throwable parent) {
        super(message, parent);
    }
}
