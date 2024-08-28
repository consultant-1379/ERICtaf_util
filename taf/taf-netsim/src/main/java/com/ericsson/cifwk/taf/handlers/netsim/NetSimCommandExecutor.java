package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.List;

/**
 * Abstraction of NetSim command executor
 */
public interface NetSimCommandExecutor {
	/**
	 * Executes the NetSim commands in the same sequence as they are provided 
	 * @param commands	NetSim commands. Please note that the element sequence in the array defines the execution sequence.
	 * @return	execution result (output) 
	 */
	NetSimResult exec(NetSimCommand... commands);

	/**
	 * Executes the NetSim commands in the same sequence as they are provided 
	 * @param commands	NetSim commands. Please note that the element sequence in the array defines the execution sequence.
	 * @return	execution result (output) 
	 */
	NetSimResult exec(List<NetSimCommand> commands);
}
