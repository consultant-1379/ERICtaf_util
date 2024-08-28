package com.ericsson.cifwk.taf.handlers.impl
import groovy.transform.WithWriteLock

import org.jboss.as.cli.CommandContext
import org.jboss.as.cli.CommandContextFactory
import org.jboss.dmr.ModelNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ericsson.cifwk.taf.handlers.PrintUtils
/**
 * Utility to execute command on JBOSS node using local client 
 *
 */
class JbossLocalClient {

	public static final int commandContextTimeOut = 5000
	private static final String FAILURE = "failure"
	private static Logger logger = LoggerFactory.getLogger(JbossLocalClient)

	private static Map<CommandContext,Thread>  timeoutTrackers = [:]

	private static void startTimeoutTracker(CommandContext ctx){
		if (timeoutTrackers[ctx])
			timeoutTrackers[ctx].interrupt()
		timeoutTrackers[ctx] = Thread.start timeOutTracker(ctx)
	}

	static private Closure timeOutTracker = { CommandContext ctx ->
		try {
			Thread.sleep(commandContextTimeOut)
			ctx = null
		} catch (Exception e) {
		}
	}

	/**
	 * Map of all available connection if a form: node > port > user > connection
	 */
	private static Map<String,Map<String,Map<String,CommandContext>>> activeContexts = [:]

	/**
	 * Method looking for already established connection to JBOSS node
	 * @param remoteNode
	 * @param remotePort
	 * @param user
	 * @return
	 */
	@WithWriteLock
	private static CommandContext findContext(String remoteNode,String remotePort, String user){
		CommandContext ctx = activeContexts[remoteNode]?.get(remotePort)?.get(user)
		if (ctx)
			startTimeoutTracker(ctx)
		return ctx
	}

	/**
	 * Method to establish new connection and create context and update map with new context
	 * @param remoteNode
	 * @param remotePort
	 * @param user
	 * @param password
	 * @return
	 */
	private static CommandContext createContext(String remoteNode,String remotePort, String user, String password){
		CommandContext ctx
		boolean connected = false
		try {
			ctx = CommandContextFactory.getInstance().newCommandContext(user,password as char[])
		} catch (Exception e) {
			logger.error "Cannot create command context with user $user and pass $password",e
		}
		try {
			ctx.connectController(remoteNode,remotePort.toInteger())
			connected = true
		} catch (Exception e) {
			logger.error "Cannot connect to host $remoteNode on port $remotePort"
		}

		if (! activeContexts[remoteNode])
			activeContexts[remoteNode] = [:]
		if (! activeContexts[remoteNode][remotePort])
			activeContexts[remoteNode][remotePort] = [:]
		activeContexts[remoteNode][remotePort][user] = ctx

		if (connected)
			startTimeoutTracker(ctx)
		return ctx
	}

	/**
	 * Method getting new command context by re-using current one or creating new one. Method is thread-safe
	 * @param remoteNode
	 * @param remotePort
	 * @param user
	 * @param password
	 * @return
	 */
	@WithWriteLock
	static CommandContext getContext(String remoteNode,String remotePort, String user, String password){
		CommandContext ctx
		if (! (ctx = findContext(remoteNode,remotePort,user))){
			ctx = createContext(remoteNode, remotePort, user, password)
		}
		return ctx
	}

	/**
	 * Method to execute command on JBOSS node using local jboss client
	 * @param command
	 * @param remoteNode
	 * @param remotePort
	 * @param user
	 * @param password
	 * @return
	 */
	static String executeCommand(String command, String remoteNode, String remotePort, String user, String password){
		CommandContext ctx = getContext(remoteNode,remotePort,user,password)
		String responseString
		try {
			ModelNode request = ctx.buildRequest(command)
			ModelNode response = ctx.modelControllerClient.execute(request)
			responseString = response.toString()
			logger.debug "Response: ${response.toJSONString(false)}"
		} catch (Exception e) {
			responseString = PrintUtils.runAndCaptureOutput {ctx.handle(command)}
		}

		assert ! responseString.contains(FAILURE) : responseString
		return responseString
	}
}
