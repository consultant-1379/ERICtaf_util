package com.ericsson.cifwk.taf.handlers.netsim.domain;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimEntityNotFoundException;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.OpenCommand;
import org.apache.commons.lang.StringUtils;

public class NetSimSimulationFactory {
	
	private NetSimSimulationFactory() {
		
	}
	
	/**
	 * Returns the instance of {@link Simulation}, not checking if such simulation really exists.
	 * @param context	NetSim context to bind the simulation to
	 * @param simName	simulation name
	 * @return	the instance of {@link Simulation}.
	 */
	public static Simulation getInstance(NetSimContext context, String simName) {
    	return new SimulationImpl(context, simName);
	}

	/**
	 * Returns the instance of {@link Simulation}.
	 * @param context	NetSim context to bind the simulation to
	 * @param simName	simulation name
	 * @return	the instance of {@link Simulation}.
	 * @throws {@link NetSimEntityNotFoundException} if such simulation doesn't exist
	 */
	public static Simulation getValidInstance(NetSimContext context, String simName) {
    	OpenCommand openSimCommand = NetSimCommands.open();
    	openSimCommand.setSimulation(simName);
    	NetSimResult openResult = context.exec(openSimCommand);
    	String rawOutput = openResult.getRawOutput();
		if (!StringUtils.contains(rawOutput, simName) || StringUtils.contains(rawOutput, "does not seem to be a simulation")) {
    		throw new NetSimEntityNotFoundException(String.format("Simulation '%s' wasn't found on server '%s'", simName, context.getHostName()));
    	}
    	
    	return getInstance(context, simName);
	}
}
