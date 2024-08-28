package com.ericsson.cifwk.taf.handlers.impl

import groovy.transform.WithWriteLock

import javax.management.MBeanServerConnection
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class JbossJmxConnectionClient {
	public static final int DEFAULT_MAX_RETRIES = 5
	public static final int DEFAULT_RETRY_SLEEP = 2000
	public static final int DEFAULT_CONNECTION_TIMEOUT = 3600000
	public static int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT
	public static int connectionRetries = DEFAULT_MAX_RETRIES
	public static int connectionRetrySleep = DEFAULT_RETRY_SLEEP
	private static Logger logger = LoggerFactory.getLogger(JbossJmxConnectionClient)
	private static Map< String, List> activeConnections = [:]

	private static String createKey(String... entries){
		return entries.join("|")
	}


	private static Map getEnvironment(String user, String password){
		if (logger.debugEnabled) logger.debug "Creating credentials environment: $user:$password"
		return [ (JMXConnector.CREDENTIALS): [user,password] as String[] ]
	}

	private static String getConnectionUrl(String host, String port){
		String url = "service:jmx:remoting-jmx://" + host + ":" + port
		if (logger.debugEnabled) logger.debug "Creating connection url: $url"
		return url
	}

	@WithWriteLock
	public static MBeanServerConnection getJmxConnection(String server, String port, String user, String password){
		if (logger.traceEnabled) logger.trace "Getting connection to $server:$port using $user with $password"
		List connectionItems = findConnection(server, port, user, password)
		MBeanServerConnection result = connectionItems?.get(0)
		if (! result){
			if (logger.debugEnabled) logger.debug "No reuse possible"
			result = createConnection(server,port,user,password)
			connectionItems = findConnection(server, port, user, password)
		} 
		else
		{
			if (logger.traceEnabled) logger.trace "Reusing connection $result"
			int retries = connectionRetries
			while (! validateConnection(connectionItems) && retries > 0){
				Thread.sleep(connectionRetrySleep)
				logger.trace "Retry create connection for jmx"
				result = createConnection(server,port,user,password)
				retries--
				logger.trace "remaining retries: ${retries}"
			}
		}
		assert result : "Cannot get valid connection"
		return result
	}

	private static boolean validateConnection(List connectionItems){
		JMXConnector connector = connectionItems?.get(1)
		MBeanServerConnection connection = connectionItems?.get(0)
		try {
			assert connector?.getConnectionId()
			assert connection?.getMBeanCount() > 0
			return true
		} catch (Throwable e){
			logger.warn("Connection is not correct", e)
			connection = null
			DefaultGroovyMethods.closeQuietly(connector)
			connector = null
			logger.trace "Returning false for validateConnection"
			return false
		}
	}
	private static List findConnection(String server, String port, String user, String password){
		List result = activeConnections[createKey(server,port,user,password)]
		if (result){
			if (logger.traceEnabled) logger.trace "Found connection connection: ${result[0]}"

			Thread oldTimeoutMonitor = result[2]
			oldTimeoutMonitor.interrupt()
			oldTimeoutMonitor = null
			result[2] = startTimeOutDeamon(server, port, user, password)
		}  else if (logger.debugEnabled) logger.debug "No connection found"
		return result
	}

	private static MBeanServerConnection createConnection(String server, String port, String user, String password){
		if (logger.debugEnabled) logger.debug "Creating new connection to server $server  on port $port with user $user and pass $password"
		JMXServiceURL serviceURL = new JMXServiceURL(getConnectionUrl(server, port))
		MBeanServerConnection mBeanConnection

		try {
			JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, getEnvironment(user, password))
			if (logger.debugEnabled) logger.debug "Connector established $jmxConnector"
			mBeanConnection = jmxConnector.getMBeanServerConnection()
			if (logger.debugEnabled) logger.debug "Connection established $mBeanConnection"
			activeConnections[createKey(server,port,user,password)] = [mBeanConnection,jmxConnector, startTimeOutDeamon(server, port, user, password)]
			if (logger.debugEnabled) logger.debug "Time out deamon started for $connectionTimeout ms"
		} catch (Throwable e){
			if (e instanceof SecurityException )
				logger.error "Authentication error with credentials $user $password" ,e
			throw e
		}

		return mBeanConnection
	}

	@WithWriteLock
	public static closeConnection(String server, String port, String user, String password){
		activeConnections[createKey(server, port, user, password)].with { connectionList ->
			if (connectionList){
				try {
					connectionList.get(1)?.close()
				} catch (Throwable ignore){}
				connectionList[0] = null
				connectionList[1] = null
				connectionList[2] = null
			}

		}
		activeConnections[createKey(server, port, user, password)] = null
		if (logger.debugEnabled) logger.debug "Current connections: " + activeConnections.collect {key, val -> return "$key:$val"}.join("\n")
	}

	protected static Thread startTimeOutDeamon(String server, String port, String user, String password){
		return Thread.startDaemon("JmxTimeoutDaemon")  {
			int timeoutTimes = connectionTimeout/10

			try {
				timeoutTimes.times {
					if (! Thread.currentThread().isInterrupted()) {
						Thread.currentThread().sleep(10);
					}	else throw new InterruptedException()
				}
				if (logger.debugEnabled) logger.debug "Timeout deamon closing connection $server:$port"
				closeConnection(server, port, user, password)
			} catch (InterruptedException ignore) {
				if (logger.traceEnabled) logger.trace "Timeout deamon interrupted"
			}
		}
	}

}
