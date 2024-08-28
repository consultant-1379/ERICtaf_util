package com.ericsson.cifwk.taf.handlers

import com.ericsson.cifwk.meta.API
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.User
import com.ericsson.cifwk.taf.data.UserType
import com.ericsson.cifwk.taf.data.Ports
import groovy.util.logging.Log4j
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.handlers.impl.JbossLocalClient
import com.ericsson.cifwk.taf.handlers.impl.JbossRemoteClient

/**
 * Class to execute command on either local or remote JBOSS client 
 *
 */
@Log4j
class JbossCommandExecutor {

    public static final String SUCCESS = "success"
    String response
    Host jbossNode
    Host parentServer

    private String jbossUser
    private String jbossPass
    private String hostName
    private String jbossManagementPort
    private int jbossOffset
    private String jbossHome

    /**
     * Constructor using explicit arguments. If parentHost argument is delivered, remote CLI will be used to execute commands
     * @param hostName
     * @param jbossUser
     * @param jbossPass
     * @param jbossManagementPort
     * @deprecated This constructor is depracated and it does not allow to use tunneling; Use {@link #JbossCommandExecutor(Host, Host)} instead
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.6d)
    @Deprecated
    JbossCommandExecutor(String hostName,String jbossUser, String jbossPass, String jbossManagementPort, int jbossOffset = 0,Host parentHost=null){
        this.hostName = hostName
        this.jbossUser = jbossUser
        this.jbossPass = jbossPass
        this.jbossManagementPort = jbossManagementPort
        this.parentServer = parentHost
        this.jbossOffset = jbossOffset
    }

    /**
     * Constructor using JBOSS node as host argument, if parent host is specified, remote CLI will be used
     * @param jbossNode
     */
    JbossCommandExecutor(Host jbossNode, Host parentHost = null, String jbossHome){
        log.debug "Creating Jboss command executor for node ${jbossNode.dump()}"
        this.jbossNode = jbossNode
        this.hostName = jbossNode.ip
        User jbossAdmin = jbossNode.users.find { it.getType() == UserType.ADMIN}
        if (jbossAdmin == null )log.error  "Cannot find Jboss management realm user for node $jbossNode and parent host $parentHost"
        this.jbossUser = jbossAdmin?.getUsername()
        this.jbossPass = jbossAdmin?.getPassword()
        this.jbossOffset = (jbossNode.offset)?jbossNode.offset.toInteger() : 0
        this.jbossManagementPort = jbossNode.port[Ports.JBOSS_MANAGEMENT]
        this.parentServer = parentHost
        this.jbossHome = jbossHome
    }

    /**
     * Check if remote CLI is going to be use
     * @return
     */
    boolean useRemote(){
        return parentServer != null
    }

    /**
     * Execute command on either local or remote client. Returns false if execution of method didn't finish successfully
     * @param command
     * @return
     */
    private boolean executeCommand(String command){
        try{
            if (useRemote()){
                String jbossRemoteManagementPort = jbossManagementPort
                String jbossNodeIp = hostName
                if (jbossNode && jbossNode.tunneled){
                    log.debug "Using original setting with the tunnel for " + jbossNode.dump()
                    jbossRemoteManagementPort = (jbossNode.originalPort[Ports.JBOSS_MANAGEMENT])?:jbossNode.originalPort[Ports.JMX]
                    jbossNodeIp = jbossNode.originalIp
                }
                response = JbossRemoteClient.executeCommand(jbossHome, command, jbossNodeIp,parentServer.ip,parentServer.user,parentServer.pass,parentServer.port[Ports.SSH],jbossUser,jbossPass,jbossRemoteManagementPort,jbossOffset)
            }
            else {
                response = JbossLocalClient.executeCommand(command, hostName, jbossManagementPort, jbossUser, jbossPass)
            }
            log.debug "Received response: $response"
            return true
        } catch (Throwable e) {
            log.error "Cannot execute command due to error" ,e
            response=e.message
            return false
        }
    }

    /**
     * Execute command passed with arguments as single string. Returns response from the command execution
     * @param cmdWithArgs
     * @return
     */
    public String simplExec(String cmdWithArgs) {
        executeCommand(cmdWithArgs)
        return response
    }

    /**
     * Execute command with arguments specified as list. Returns response from the command execution
     * @param cmdWithOutArgs
     * @param arguments
     * @return
     */
    public String simplExec(String cmdWithOutArgs, String... arguments) {
        execute(cmdWithOutArgs,arguments)
        return response
    }

    /**
     * Execute command with arguments in one string. Returns status of execution. Result of the execution is available via getResponse() method
     * @param cmdWithArgs
     * @return
     */
    public boolean execute(String cmdWithArgs) {
        return executeCommand(cmdWithArgs)
    }

    /**
     * Execute command with arguments specified as a list. Returns status of execution. Result of the execution is available via getResponse()
     * @param cmdWithOutArgs
     * @param arguments
     * @return
     */
    public boolean execute(String cmdWithOutArgs, String... arguments) {
        return executeCommand("$cmdWithOutArgs ${arguments.join(' ')}")
    }

    /**
     * Getting ssh connection to remote server
     * @return
     */
    public CLICommandHelper getSshConnection(){
        CLICommandHelper ssh = new CLICommandHelper(parentServer);
        return ssh
    }

}