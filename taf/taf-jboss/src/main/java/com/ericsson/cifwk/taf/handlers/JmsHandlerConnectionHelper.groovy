package com.ericsson.cifwk.taf.handlers

import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.DeliveryMode
import javax.jms.Destination
import javax.jms.MessageConsumer
import javax.jms.MessageProducer
import javax.jms.QueueBrowser
import javax.jms.Session
import javax.jms.Topic

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ericsson.cifwk.taf.data.DataHandler
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.Ports
import com.ericsson.cifwk.taf.data.User
import com.ericsson.cifwk.taf.data.UserType

class JmsHandlerConnectionHelper {

	/**
	 * Default JNDI lookup for JMS connection factory
	 */
	public static final String DEFAULT_JMS_CONNECTION_FACTORY = "jms/RemoteConnectionFactory"

	protected String destinationName
	protected Host jbossNode
	protected boolean transacted
	protected  int acknowledgmentMode

	private static Logger logger = LoggerFactory.getLogger(JmsHandlerConnectionHelper)
	private ConnectionFactory connectionFactoryInstance
	private Destination destinationInstance
	private Connection connectionInstance
	private Session sessionInstance
	private MessageConsumer consumerInstance
	private MessageProducer producerInstance
	private QueueBrowser queueBrowserInstance
	private static AsRmiHandler asRmi



	/**
	 * Start the connection to start listening for messages
	 */
	protected void startConnection(){
		try {
			getConnection().start()
			logger.debug "Connection started for $jbossNode in ${this}"
		} catch (Exception e) {
			logger.error "Couldn't instantiate JmsHandler because of the following exception" ,e
		}
	}



	/**
	 * Create a connection using the credentials passed in the Host object in the constructor
	 * @return
	 */
	private Connection getConnection() throws Exception{
		if (! connectionInstance){
			User jbossApplicationRealUser = jbossNode.users.find { User user-> user.getType() == UserType.OPER}
			String userName
			String userPass
			if (jbossApplicationRealUser == null){
				logger.error "Cannot find application realm user for jboss node $jbossNode"
				userName = jbossNode.getUser()
				userPass = jbossNode.getPass()
				logger.warn "Using default user $userName with $userPass"
			} else {
				userName = jbossApplicationRealUser.getUsername()
				userPass = jbossApplicationRealUser.getPassword()
			}
			logger.debug "Creating JMS connection using user: ${userName} with pass: ${userPass}"
			if (jbossNode.isTunneled()){
				logger.debug "Connection is done via tunnel. Hacking connection"
				hackConnectionFactory()
			}
			connectionInstance = getConnectionFactory().createConnection(userName,userPass)
		}
		if (logger.traceEnabled) logger.trace "Returning connection $connectionInstance"
		return connectionInstance
	}

	/**
	 * This hack is enabled if tunnel is enabled
	 * params of TransportConfiguration are overriden with local ip and local port
	 */
	private void hackConnectionFactory(){
		try{
			getConnectionFactory().serverLocator.initialConnectors.each {it.params.port=jbossNode.port[Ports.JMS]; it.params.host=jbossNode.ip}
			logger.debug "Connection factory hacked"
		} catch (Exception e){
			logger.debug "Hacking failed $e"
			logger.trace "Details:", e
		}
	}

	/**
	 * Look up the destination to use
	 * @return
	 */
	protected Destination getDestination(){
		if (! destinationInstance){
			logger.debug "Creating destination in ${this}"
			if(!asRmi){
				asRmi = new AsRmiHandler(jbossNode)
				logger.debug "Created RMI in getDestination"
			}

			try{
				destinationInstance = (Destination) asRmi.getServiceViaJndiLookup(destinationName)
			}	catch(Exception e)	{
				logger.debug "Cannot get ServiceViaJndiLookup $e"
			}
		}
		if (logger.traceEnabled) logger.trace "Returning destination $destinationInstance"
		return destinationInstance
	}

	/**
	 * Create a session across the connection.
	 * @return
	 */
	protected Session getSession(){
		if (! sessionInstance){
			logger.debug "Creating session in ${this}"
			try{
				sessionInstance = getConnection().createSession(transacted, acknowledgmentMode)
			} catch(Exception e) {
				logger.debug "Creating session failed ${this}"
			}
		}
		if (logger.traceEnabled)	logger.trace "Returning session $sessionInstance"
		return sessionInstance
	}

	/**
	 * Create the default consumer to use to get messages.
	 * @return
	 */
	protected MessageConsumer getConsumer(){
		try{
			if (! consumerInstance){
				logger.debug "Creating consumer in ${this}"
				String filter = null
				consumerInstance =getSession().createConsumer(getDestination(),filter)
				logger.debug "Consumer created ${consumerInstance.dump()} in ${this}"
			}
		} catch(Exception e) {
			logger.debug "Issue in creating consumner $e"
		}
		if (logger.traceEnabled)	logger.trace "Returning consumer $consumerInstance for ${this}"
		return consumerInstance
	}

	/**
	 * Check if destination is queue
	 * @return
	 */
	protected boolean isQueue(Destination destination=getDestination()){
		boolean queue = false
		try {
			queue = queue || destination.getProperties()["queue"]
			queue = queue || Queue.isInstance(destination)
			queue = queue || destination instanceof Queue
			if (logger.traceEnabled)	logger.trace "Destination ${destination.dump()} is queue? $queue"
		} catch (Throwable ignore){
		}
		return  queue
	}

	/**
	 * Check if destination is topic
	 * @return
	 */
	protected boolean isTopic(Destination destination=getDestination()){
		boolean topic = false
		try {
			topic = topic || destination.getProperties()["topic"]
			topic = topic || destination instanceof Topic
			topic = topic || Topic.isInstance(destination)
			if (logger.traceEnabled)	logger.trace "Destination $destination is topic? $topic"
		} catch (Throwable ignore){}
		return  topic
	}

	/**
	 * Create the producer to create and send messages
	 * @return prod - the producer
	 */
	protected MessageProducer getProducer(){
		if (! producerInstance){
			logger.debug "Creating producer in ${this}"
			producerInstance = getSession().createProducer(getDestination())
			producerInstance.setDeliveryMode(DeliveryMode.NON_PERSISTENT)
		} else
		if (logger.traceEnabled) logger.trace "Returning producer $producerInstance"
		return producerInstance
	}

	/**
	 * Create the connectionFactory if not already created.
	 * @return coonFactory
	 */
	private ConnectionFactory getConnectionFactory() throws Exception{
		if (! connectionFactoryInstance){
			final String connectionFactoryString = DataHandler.getAttribute("remote.jms.connection.factory")
			if (! connectionFactoryString || connectionFactoryString==null){
				connectionFactoryString = DEFAULT_JMS_CONNECTION_FACTORY
			}
			if(!asRmi){
				asRmi = new AsRmiHandler(jbossNode)
				logger.debug "creating new RMI in getConnectionFactory $asRmi"
			}
			connectionFactoryInstance = (ConnectionFactory) asRmi.getServiceViaJndiLookup(connectionFactoryString)

			logger.debug "Created ConnectionFactory $connectionFactoryInstance"
		}
		return connectionFactoryInstance
	}

	/**
	 * Method to restart connection to JMS service via closing current one and opening new
	 */
	protected void restartConnection(){
		logger.debug "Restarting the connection during poll for message"
		close()
		startConnection()

	}

	/**
	 * Close the connection, session and stop the threads polling on a boolean
	 */
	protected void close(){
		logger.debug "Closing JmsHandler ${this}"
		consumerInstance?.close()
		consumerInstance = null
		producerInstance?.close()
		producerInstance = null
		queueBrowserInstance?.close()
		queueBrowserInstance = null
		try {
			connectionInstance?.stop()
			connectionInstance?.close()
			sessionInstance?.close()
			logger.debug "All JMS connecions Closed"
		} catch (Exception ignore)  {
			logger.trace "Issue in Closing JMS Connection $ignore "
		}  finally  {
			sessionInstance = null
			connectionInstance = null
			destinationInstance = null
            asRmi.close();
		}
		if (logger.traceEnabled) logger.trace "Dump of this: ${this.dump()}"
	}

}
