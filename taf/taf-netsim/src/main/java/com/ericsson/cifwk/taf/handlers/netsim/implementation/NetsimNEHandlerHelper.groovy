package com.ericsson.cifwk.taf.handlers.netsim.implementation

import com.ericsson.cifwk.meta.API
import groovy.transform.WithWriteLock

import com.ericsson.cifwk.taf.data.Host

/**
 * Helper class to map and store netsim handlers for hosts and simulations
 *
 */
@API(API.Quality.Deprecated)
@API.Since(2.17d)
@Deprecated
class NetsimNEHandlerHelper {
	public static Map<Host,SshNetsimHandler> allNetsimHandler = [:]
	
	/**
	 * Method to find a NetsimHandler for a given netsim host and simulation
	 * 
	 * @param nsHost - the netsim server
	 * @param simulation - the simulation 
	 * @return a NetsimHandler for the given host and simulation
	 */
	@WithWriteLock
	protected static SshNetsimHandler findNetsimHandler(Host nsHost){
		if (! allNetsimHandler[nsHost]) {
			createNetsimHandler(nsHost)
		}
		return allNetsimHandler[nsHost]
	}
	
	/**
	 * Method to create a NesimHandler for a host and simulation
	 * 
	 * @param nsHost - the netsim server
	 * @param simulation - the simulation 
	 * @return a NetsimHandler for the given host and simulation
	 */
	private static SshNetsimHandler createNetsimHandler(Host nsHost){
		SshNetsimHandler nsh = new SshNetsimHandler(nsHost)
		allNetsimHandler[nsHost] = nsh
		return nsh
	}
}
