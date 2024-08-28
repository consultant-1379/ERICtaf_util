package com.ericsson.cifwk.taf.osgi.client;

public class ContainerNotReadyException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2452741890590372836L;

    public ContainerNotReadyException() {
        super();
    }

    public ContainerNotReadyException(String message) {
        super(message);
    }
}
