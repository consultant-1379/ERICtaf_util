package com.ericsson.cifwk.taf.handlers.netsim;

/**
 * Thrown if NetSim entity (simulation, NE, etc.) was not found
 */
public class NetSimEntityNotFoundException extends NetSimException {
	private static final long serialVersionUID = 6991211520247856691L;

	public NetSimEntityNotFoundException(String message) {
		super(message);
	}
}
