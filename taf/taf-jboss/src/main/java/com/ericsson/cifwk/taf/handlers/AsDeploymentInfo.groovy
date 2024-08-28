/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.handlers

import com.ericsson.cifwk.taf.data.Host
import groovy.util.logging.Log4j

import javax.management.*
import java.nio.charset.MalformedInputException;


/**
 * Class to handle Deployment Information on JBoss Node
 *
 */
@Log4j
public class AsDeploymentInfo{

	private static int PERSISTENT_POS = 7
	private static int ENABLED_POS = 8
	private static int STATUS_POS = 9
	JbossCommandExecutor commandService
	Host jbossNode
	Host server
	JmxHandler jmxService
	private String jbossHome
	private static String MBEAN_JBOSS_HOME="jboss.as:path=jboss.home.dir"
	private static String JBOSS_MBEAN_BASEDIR = "jboss.as:path=jboss.server.base.dir"
	private static String JBOSS_MBEAN_LOGDIR = "jboss.as:path=jboss.server.log.dir"
	private static String JBOSS_MBEAN_PATH = "path"
	
	/**
	 * Constructor using jboss node and optionally server that jboss is deployed on for remote usage
	 * @param jbossNode
	 */
	AsDeploymentInfo(Host jbossNode, Host server = null){
		this.jbossNode = jbossNode
		this.server = server
		jmxService = new JmxHandler(jbossNode)
		this.jbossHome = getJbossHome()
		commandService = new JbossCommandExecutor(jbossNode,server,jbossHome)
	}
		
	
	/**
	 * Lists the Valid states for Persistent, Enabled and Running values for deployStatusFile to validate returned states.
	 */
	 private enum EnabledPersistent { TRUE, FALSE }
	 private enum StatusEnum { OK, STOPPED }
	
	
	/**
	 * Run a command to check deployment enabled status for ear file on JBoss node.
	 * @param ear fileName
	 * @return true if deployed file has: Enabled=true, false if Enabled=false
	 * @throws Exception if enabled result does not contain true/false
	 * @throws MalformedInputException if invalid enabled status found
	 */
	public boolean isEnabled(String fileName){
		String result
		Boolean status = commandService.execute("deployment-info --name=$fileName")
		log.debug "File Deployment Status executed with status: $status. Response: $commandService.response"
		try {
		result = commandService.response.split()[ENABLED_POS]
		} catch (Exception e){
			throw new MalformedInputException("MalFormed Enabled Response Received: $result")
		}	
		if (EnabledPersistent.values().toString().contains(result.toUpperCase()))
			return result
		else
			throw new Exception("Unknown Persistent State: $result")
	}
	
	/**
	 * Run a command to check deployment persistent status for ear file on JBoss node.
	 * @param ear fileName
	 * @return true if deployed file has: Persistent=true, false if Persistent=false
	 * @throws MalformedInputException if invalid enabled status found
	 * @throws Exception if persistent result does not contain true/false
	 */
	public boolean isPersistent(String fileName){
		String result
		Boolean status = commandService.execute("deployment-info --name=$fileName")
		log.debug "File Deployment Status executed with status: $status. Response: $commandService.response"
		try {
		result = commandService.response.split()[PERSISTENT_POS]
		} catch (Exception e){
			throw new MalformedInputException("MalFormed Persistent Response Received: $result")
		}
		if (EnabledPersistent.values().toString().contains(result.toUpperCase()))
			return result
		else
			throw new Exception("Unknown Persistent State: $result")		
	}
	
	/**
	 * Run a command to check deployment status for ear file on JBoss node.
	 * @param ear fileName
	 * @return Status of either OK/STOPPED
	 * @throws MalformedInputException if invalid enabled status found
	 * @throws Exception if Status result does not contain OK/STOPPED values
	 */
	public String getStatus(String fileName){
		Boolean status = commandService.execute("deployment-info --name=$fileName")
		log.debug "File Deployment Status executed with status: $status. Response: $commandService.response"
		String result
		try {		
		result = commandService.response.split()[STATUS_POS]
		} catch (Exception e){
			throw new MalformedInputException("MalFormed Status Response Received: $result")
		} 
		if ( StatusEnum.values().toString().contains(result) )
			return result
		else
			throw new Exception("Unknown Status State: $result")	
	}
	
	/**
	 * Retrieve the value of jboss.as:path=jboss.home.dir JBOSS environment property
	 * @return Path to Jboss Instance Home directory
	 */
	public String getJbossHome() {
		return getJbossJmxBean(MBEAN_JBOSS_HOME, JBOSS_MBEAN_PATH)
	}
	
	/**
	 * Retrieve the value of jboss.server.base.dir JBOSS environment property
	 * @param
	 * @return Path to JBoss Instance base directory
	 */
	public String getJbossServerBaseDir() {
		return getJbossJmxBean(JBOSS_MBEAN_BASEDIR, JBOSS_MBEAN_PATH)
	}

	/**
	 * Retrieve the value of jboss.server.log.dir JBOSS environment property
	 * @param
	 * @return Path to JBoss Instance Log directory
	 */
	public String getJbossServerLogDir() {
		return getJbossJmxBean(JBOSS_MBEAN_LOGDIR, JBOSS_MBEAN_PATH)
	}
		
	/**
	 * Retrieve the value of given JBoss MBean.
	 * @param MBean 
	 * @param MBean attribute
	 * @return Path to JBoss Instance Log directory
	 */
	private String getJbossJmxBean(String MBean, String MbeanPath) {
		try {
			return jmxService.getMBean(MBean).getProperty(MbeanPath)
		} catch (InstanceNotFoundException e) {
			log.error "Error in getting $MBean from JBoss MBean for node $jbossNode" + e
		} catch (AttributeNotFoundException e) {
			log.error "Error in getting $MbeanPath attribute from JBoss MBean $MBean for node $jbossNode" + e
		}
	}
	
}
