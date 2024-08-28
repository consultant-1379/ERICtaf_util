package com.ericsson.cifwk.taf.handlers.netsim.implementation

import com.ericsson.cifwk.meta.API
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper
import groovy.util.logging.Log4j

import com.ericsson.cifwk.taf.data.Host

/**
 * Class to communicate with and control a netsim installation over SSH
 *
 */
@Log4j
@API(API.Quality.Deprecated)
@API.Since(2.17d)
@Deprecated
class SshNetsimHandler {

    Host host
    List<String> listOfSimulations
    List<NetsimNE> listOfNetworkElements
    CLICommandHelper netsimConnection

    public static final String CMD_START = "echo -e '"
    public static final String CMD_SIM = ".open "
    public static final String CMD_MID = "\\n"
    public static final String CMD_END = "' | /netsim/inst/netsim_pipe"
    private static final String CMD_IS_STARTED = "echo -e '.show started" + CMD_END
    public static final String SIM_PATH = "/netsim/netsim_dbdir/simdir/netsim/netsimdir"
    public static final String SIM_START_IDENTIFIER = "total"
    private static final String SHOW_SIM = ".show simulations"
    public static final String SELECT = ".selectnocallback "
        private static final String START_NE = "\\n.start -parallel"
    private static final String STOP_NE = "\\n.stop -parallel"
    private static final String IP_SEC = "\\n status:sec;"
    private static final String SEC_ON = "on"
    private static final String SEC_OFF = "off"
    private static final String SEC_SPLIT = "Corba security:"
    private static final String NOT_STARTED = "Not started!"
    private static final String NOT_EXIST = "These objects do not exist"
    private static final String SHOW_RESP = ">> .show"
    public static final String OPEN_RESP = ">> $CMD_SIM"
    private static final String SHOW_NES = ".show simnes"
    private static final String STARTED = ".show started"
    private static final String DEF_SIM = "default"
    private static final String EMPTY_STRING = ""
    private static final String SPACE = " "
    private static final String NEW_LINE = "\n"
    public static final String NE_NAME = "NE Name"
    public static final String OK = "OK"


    SshNetsimHandler(Host host){
        this.host = host
        this.netsimConnection = new CLICommandHelper(host)
        updateListOfSimulations()
        updateListOfNetworkElements()
    }

    /**
     * Method to check if Netsim is running
     *
     * @return true is Netsim is running properly
     */
    public boolean isNetsimRunning(){
        String cmdResponse = netsimConnection.simpleExec(CMD_IS_STARTED)
        if(cmdResponse.startsWith(SHOW_RESP)) {
            log.info "Netsim is running properly"
            return true
        }
        else{
            log.info "Netsim is not running properly"
            return false
        }
    }

    /**
     * Method to retrieve the list of network elements in the provided simulation
     *
     * @param simulations - the simulation(s) name(s) as a string
     * @return a List of network elements (of type NetsimNE)
     */
    public List<NetsimNE> getAllNEs(String... simulations){
        List<NetsimNE> neList = []
        if (simulations == []){
            return neList
        }
        List<String> listOfCommands = []
        simulations.each { simulation ->
            listOfCommands.add("$CMD_SIM$simulation")
            listOfCommands.add("$SHOW_NES")
        }
        String firstSimulation = simulations.first()
        String expectedStartOfResponse = ">> $CMD_SIM$firstSimulation\nOK\n>> $SHOW_NES"
        String cmdResponse = executeCommand(*listOfCommands)
        log.trace "Response from '$SHOW_NES' on each simulation: \n $cmdResponse"
        if(cmdResponse.startsWith(expectedStartOfResponse)) {
            log.debug "Got a valid response from the server"}
        else {
            log.debug "Got an invalid response from the netsim server, Please check Netsim is started correctly"
            return neList
        }
        int startLine, endLine
        List<String> responseLines = cmdResponse.split("$NEW_LINE")
        responseLines.with { lines ->
            List<Number> simLines = lines.findIndexValues{ it.startsWith("$OPEN_RESP") }
            simLines.each { simLine ->
                String simulation = lines[simLine.toInteger()].split(" ")[2]
                startLine = lines.findIndexOf(simLine.toInteger(), {it.startsWith("$NE_NAME")}) +1
                endLine = lines.findIndexOf(startLine, { it.startsWith("$OK")}) -1
                Map<String,Integer> indecies = getMapOfIndecies(lines[startLine-1])
                lines[startLine..endLine].each { line ->
                    Map<String,String> neDetails = getNeDetails(line, indecies)
                    neList << this.instantiateNE(neDetails['name'],neDetails['type'],neDetails['techType'],neDetails['nodeType'],neDetails['mim'],neDetails['address'],simulation)
                }
            }
        }
        return neList
    }

    /**
     * Method to retrieve the list of all started network elements
     * @return a List of network elements (of type NetsimNE)
     */
    public List<NetsimNE> getAllStartedNEs() {
        List<NetsimNE> startedNeList = []
        String cmdResponse = executeCommand(STARTED)

        if(cmdResponse.startsWith(">> $STARTED")) {
            log.debug "Got a valid response from the server"}
        else {
            log.debug "Got an invalid response from the netsim server, Please check Netsim is started correctly"
            return startedNeList
        }

        listOfNetworkElements.each { networkElement ->
            if (cmdResponse.contains("${SPACE}${networkElement.name}${SPACE}")) {
                startedNeList.add(networkElement)
            }
        }
        return startedNeList
    }

    /**
     * Method to retrieve the list of started network elements for a specified simulation
     * @param simulation - the simulation name as a string
     * @return a List of network elements (of type NetsimNE)
     */
    public List<NetsimNE> getAllStartedNEs(String simulation) {
        return getAllStartedNEs().findAll { listOfSimulations.contains(it.simulation) }
    }

    /**
     * Method to create a map of the indices of the date relative to creating a network element object
     *
     * @param headerLine - the line containing the header information (used to calculate the index values
     * @return Map containing indices for key network element information
     */
    protected Map<String,Integer> getMapOfIndecies(String headerLine){
        Map<String,Integer> indecies = [:]
        indecies.put("name", headerLine.indexOf("NE Name"))
        indecies.put("type", headerLine.indexOf("Type"))
        indecies.put("server", headerLine.indexOf("Server"))
        indecies.put("address", headerLine.indexOf("In Address"))
        indecies.put("dest", headerLine.indexOf("Default dest"))
        log.trace "nameIndex = ${indecies['name']}, typeIndex = ${indecies['type']}, serverIndex = ${indecies['server']}, addressIndex = ${indecies['address']}, destIndex = ${indecies['dest']}"
        return indecies
    }

    /**
     * Method to extract the relevant data to create a network element based on a map of indices
     *
     * @param line - the line containing the data
     * @param indecies - the map of indices
     * @return Map of strings that can be used to create a network element
     */
    protected Map<String,String> getNeDetails(String line, Map indecies){
        Map<String,String> neDetails = [:]

        String longTypeName = line[indecies["type"]..indecies["server"]-1].trim()
        String[] type = longTypeName.split("\\s+");
        log.trace("Populating NE element from NetSim response from ${line}")
        populateMapIfPossible {neDetails.put("name",line[indecies["name"]..indecies["type"]-1].trim())}
        populateMapIfPossible {neDetails.put("type",line[indecies["type"]..indecies["server"]-1].trim())}
        populateMapIfPossible {neDetails.put("techType",type[0])}
        populateMapIfPossible {neDetails.put("nodeType",type[1])}
        populateMapIfPossible {neDetails.put("mim",type[2])}
        populateMapIfPossible {neDetails.put("address",line[indecies["address"]..indecies["dest"]-1].trim())}

        return neDetails
    }

    private void populateMapIfPossible(Closure codeToExecute){
        try {
            codeToExecute.call()
        } catch (Throwable ignore){
            log.warn("Issue during parsing NetSim response",ignore.message)
        }
    }
    /**
     * Method to execute a command on a list of Network Elements on the netsim server
     * @param neList - the list of NetsimNE objects against which a command should be executed
     * @param netSimCommands - the command to be executed (as a string)
     * @return the response from the server as a string
     */
    public String sendCommandToNetworkElements(List<NetsimNE> neList, String... netSimCommands) {
        List<String> listOfCommands = []

        listOfSimulations.each { sim ->
            List<NetsimNE> simNeList = neList.findAll { it.simulation == sim}

            if (simNeList.size > 0) {
                listOfCommands += CMD_SIM + sim
                simNeList.each { ne ->
                    listOfCommands += this.SELECT + ne.getName()
                    netSimCommands.each { cmd ->
                        listOfCommands += cmd
                    }
                }
            }
        }
        return executeCommand(*listOfCommands)
    }

    /**
     * Method to execute a "with simulation" command on a netsim server
     *
     * @param simulation - the simulation name as a string
     * @param netSimCommands - the command to be executed (as a string)
     * @return the response from the server as a string
     */
    public String executeCommandWithSimulation(String simulation, String... netSimCommands){
        String commands = netSimCommands.join("\\n")
        String fullCommandString = CMD_START + CMD_SIM +simulation + CMD_MID + commands + CMD_END
        return netsimConnection.simpleExec(fullCommandString)
    }

    /**
     * Method to execute a command on a netsim server
     *
     * @param netSimCommands - the command to be executed (as a string)
     * @return the response from the server as a string
     */
    public String executeCommand(String... netSimCommands){
        String commands = netSimCommands.join("\\n")
        String fullCommandString = CMD_START + commands + CMD_END
        return netsimConnection.simpleExec(fullCommandString)
    }

    /**
     * Method to update the list of simulations on the netsim server
     */
    private void updateListOfSimulations(){
        listOfSimulations = []
        String cmdResponse = executeCommand(SHOW_SIM)
        log.trace "Response from '$SHOW_SIM': \n $cmdResponse"
        if(!cmdResponse.startsWith(">> $SHOW_SIM")) {
            log.error "Got an invalid response from the netsim server, Please check Netsim is started correctly"
        }
        else {
            List<String> responseLines = cmdResponse.split("\n").findAll{ it != DEF_SIM && !( it ==~ /.*zip$/ ) }.toList()
            responseLines[1..(responseLines.size()-1)].each { line ->
                listOfSimulations << line
            }
            log.debug "Found $listOfSimulations.size simulations on netsim sever: $listOfSimulations"
        }
    }

    /**
     * Method to update the list of network elements on the netsim server
     */
    private void updateListOfNetworkElements(){
        listOfNetworkElements = []
        listOfNetworkElements = getAllNEs(*listOfSimulations)
    }

    /**
     * Method to check if a network element is started
     * @param neName - the name of the network element
     * @return True is the network element is started
     */
    public boolean isNeStarted(String neName){
        String cmdResponse = netsimConnection.simpleExec(CMD_IS_STARTED)
        if(cmdResponse.startsWith("$SHOW_RESP")) {
            log.debug "$CMD_IS_STARTED  executed properly, checking if $neName is started or not"
            return cmdResponse.contains(neName)
        }
        else{
            log.debug "Error executing $CMD_IS_STARTED , please check Netsim is running properly"
            return false
        }
    }

    /**
     * Method to start a network element
     * @param simulation - the simulation containing the network element
     * @param node - the network element to be started
     * @return True if the network element was started
     */
    public boolean startNE(String simulation, NetsimNE node){
        String cmdResponse = executeCommandWithSimulation(simulation, "$SELECT $node$START_NE")
        return isNeStarted(node.name)==true
    }

    /**
     * Method to stop a network element
     * @param simulation - the simulation containing the network element
     * @param node - the network element to be stopped
     * @return True if the network element was stopped
     */
    public boolean stopNE(String simulation, NetsimNE node){
        String cmdResponse = executeCommandWithSimulation(simulation,  "$SELECT $node$STOP_NE")
        return isNeStarted(node.name)==false
    }


    /**
     * Method to check Node Security Status
     * @param simulation - the simulation containing the network element
     * @param node - the network element to check
     * @return True if security on, False if security off
     * @throws RuntimeException if Node is not started, does not exist or unknown security state returned
     */
    public boolean secStatus(String simulation, NetsimNE node) throws Exception{
        boolean state
        String cmdResponse = executeCommandWithSimulation(simulation, "$SELECT $node$IP_SEC")
        if (cmdResponse.contains(NOT_STARTED) || cmdResponse.contains(NOT_EXIST)){
            throw new RuntimeException("$node is not started or does not exist")
        } else {
            List<String> responseLines = cmdResponse.split(SEC_SPLIT)
            String state1 = responseLines.last().toString()
                if ( state1.contains(SEC_ON) ){
                    state = true
                }else if (state1.contains(SEC_OFF) ){
                    state = false
                }else{
                    throw new RuntimeException("Unknown Security State: $state1")
                }
            return state
        }
    }

    /**
     * Method to create a network element object
     *
     * @param name - the name of the network element
     * @param type - the type of the network element
     * @param techType - the technology type of the network element
     * @param nodetype - the node type of the network element
     * @param mim - the mim version of the network element
     * @param ip - the IP address of the network element
     * @param simulation - the simulation containing the network element
     * @return a new network element object
     */
    protected NetsimNE instantiateNE(String name, String type, String techType,String nodeType,String mim, String ip, String simulation){
        NetsimNE newNE = new NetsimNE(
                name:name,
                type:type,
                techType:techType,
                nodeType:nodeType,
                mim:mim,
                ip:ip,
                netsimHost:host,
                simulation:simulation
                )
        return newNE

    }

}
