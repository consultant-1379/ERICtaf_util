package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.List;

/**
 * Stateful command execution facility that allows you to execute a batch of the NetSim commands on the defined host. 
 * 
 * <p>As opposed to {@link NetSimContext}, <code>NetSimSession</code> remembers your command history. It works the same way as 
 * <code>netsim_shell</code> binary (which, in fact, is used behind the scene here). 
 * 
 * <p>For example - you can execute the following set of commands in NetSimSession:
 * <p><pre>
 * NetSimSession session = NetSimCommandHandler.getSession(host5);
 * session.exec(NetSimCommands.open("simulation1"), NetSimCommands.selectnocallback("ne1", "ne2"), NetSimCommands.stop());
 * ....
 * session.exec(NetSimCommands.selectnocallback("ne3"), NetSimCommands.start());
 * </pre>
 *
 * <p>The seconds batch will work correctly because NetSimSession remembers that you opened the simulation "simulation1". 
 * If you would use {@link NetSimContext}, you would have to open simulation again and again if you work with the object that belongs to it.
 */
public interface NetSimSession extends NetSimCommandExecutor {

	/**
	 * Closes the session
	 */
	void close();

	/**
	 * Checks if session is closed
	 * @return	<code>true</code> if the session is closed
	 */
	boolean isClosed();

	/**
	 * Executes the NetSim commands in the same sequence as they are provided, implemented in this interface rather than NetsimCommand Executor
	 * as NetSimSession is the only service which requires a timeout value to be specified
	 * @param commands	NetSim commands. Please note that the element sequence in the List defines the execution sequence.
	 * @param timeout Timeout in which to wait for command execution to finish
	 * @return	execution result (output)
	 */
	NetSimResult exec(int timeout, List<NetSimCommand> commands);
	
	/**
	 * Executes the NetSim commands in the same sequence as they are provided, implemented in this interface rather than NetsimCommand Executor
	 * as NetSimSession is the only service which requires a timeout value to be specified
	 * @param timeout Timeout in which to wait for command execution to finish
	 * @param commands	NetSim commands. Please note that the element sequence in the array defines the execution sequence.
	 * @return	execution result (output)
	 */
	NetSimResult exec(int timeout, NetSimCommand... commands);

}
