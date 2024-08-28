package com.ericsson.cifwk.taf.handlers.netsim.implementation

import com.ericsson.cifwk.meta.API
import com.ericsson.cifwk.taf.data.Host

/**
 * Class to instantiate a netsim network element
 *
 */
@API(API.Quality.Deprecated)
@API.Since(2.17d)
@Deprecated
class NetsimNE extends NetsimNEHandlerHelper{

	String name
	String type
	String techType
	String nodeType
	String mim
	String ip
	Host netsimHost
	String simulation
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	String toString(){
		return name
	}

	/**
	 * Method to get a SshNetsimHandler for a network element
	 * 
	 * @return an SshNetsimHandler for the given network element 
	 */
	private SshNetsimHandler getNetsimHandler(){
		return findNetsimHandler(netsimHost)
	}
	
	
	/**
	 * Method to check if the network element is started
	 * 
	 * @return True if the network element is started
	 */
	public Boolean isStarted(){
		return netsimHandler.isNeStarted(name)
	}
	
}
