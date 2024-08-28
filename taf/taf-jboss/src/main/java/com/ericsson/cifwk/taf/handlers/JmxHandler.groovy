package com.ericsson.cifwk.taf.handlers
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.Ports
import com.ericsson.cifwk.taf.data.User
import com.ericsson.cifwk.taf.data.UserType
import com.ericsson.cifwk.taf.handlers.impl.JbossJmxConnectionClient
import groovy.util.logging.Log4j

import javax.management.MBeanServerConnection
import javax.management.ObjectName
import javax.management.remote.JMXConnector
/**
 * Class to allow JmxConnection to JBoss AS. Uses Jboss Client
 *
 */
@Log4j
class JmxHandler extends JbossJmxConnectionClient {

	String host
	String port
	String user
	String password
	Map environment

	/**
	 * Constructor
	 * @param host name of JBoss server
	 * @param port to be use for JMX connection to JBoss server
	 * @deprecated
	 */
	JmxHandler(String host="localhost", String port="9999", String user = null, String password = null){
		this.host = host
		this.port = port
		this.user = user
		this.password = password
		log.debug "JmxHandler prepared ${this.dump()}"
	}
	
	/**
	 *  Method to  instantiate a JmxHandler with a Host object
	 * @param jmxNode as a Host object
	 */
	public void setJmxNode(Host jmxNode){
		this.host = jmxNode.ip

        if (jmxNode.port == null || jmxNode.port[Ports.JMX] == null) {
            log.error "No JMX port defined for $jmxNode"
            log.debug "JMX node ${jmxNode.dump()}"
            throw new IllegalArgumentException("No JMX port defined for $jmxNode")
        }
        this.port = jmxNode.port[Ports.JMX]

        User admin = jmxNode.users.find { it.type == UserType.ADMIN }
        if (admin == null) log.error "Cannot find management user for node ${jmxNode.dump()}"
		log.trace "Using user $admin"
		this.user = admin?.getUsername()
		this.password= admin?.getPassword()
	}
	
	/**
	 * Handler to connect to machine using JMX
	 * @param jmxNode - host to connect to
	 */
	JmxHandler(Host jmxNode){
		log.debug "Using jmxNode ${jmxNode.dump()}"
		setJmxNode(jmxNode)
		log.debug "JmxHandler instantiated ${this.dump()}"
	}

	/**
	 * Getter for the MBean connection to JBoss server
	 * @return
	 */
	MBeanServerConnection getConnection(){
		if (environment){
			user = environment[JMXConnector.CREDENTIALS][0]
			password = environment[JMXConnector.CREDENTIALS][1]
		}
		log.trace "Looking for connection  using $host, $port, $user,$password"
		def jxmConnection = getJmxConnection(host, port, user, password)
		log.trace "Returning connection $jxmConnection" 
		return  jxmConnection
	}

	/**
	 * Get the MBean from JBoss server by specified name
	 * @param mBeanName
	 * @return
	 */
	GroovyMBean getMBean(String mBeanName){
		log.trace "Getting MBean for $mBeanName"
		return new GroovyMBean(connection,mBeanName)
	}

	/**
	 * Getter for list of available MBeanse. Default method listing all
	 *  of them
	 * @return
	 */
	List<String> getMBeansList(String query="*:*"){
		log.trace "Getting MBeans for query $query"
		return connection.queryNames(new ObjectName(query),null).collect { it.toString()}
	}

	/**
	 * Close the JMX connection to JBoss server
	 */
	void close(){
		log.debug "Closing connection"
		try {
			closeConnection(host, port, user, password)
			log.debug "Connection closed"
		} catch (Exception e) {
		  log.debug "Failed to close connection", e
		}
	}
}

