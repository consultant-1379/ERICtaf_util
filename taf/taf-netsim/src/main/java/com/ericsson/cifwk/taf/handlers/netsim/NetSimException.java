package com.ericsson.cifwk.taf.handlers.netsim;

/**
 * Basic NetSim exception
 */
public class NetSimException extends RuntimeException {
	private static final long serialVersionUID = 6609758953831995332L;

	public NetSimException(String message) {
		super(message);
	}

	public NetSimException(Throwable cause) {
		super(cause);
	}
}
