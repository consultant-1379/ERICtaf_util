package com.ericsson.cifwk.taf.osgi.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;

/**
 * Class providing configuration and OSGi bundles handling
 */
public class OsgiContainerHandler {

    private String scriptPath;
    private String configPath;
    private int osgiCliPort;
    private int osgiDataPort;
    private Host rcHost;
    private Shell shell = null;

    private CLI cli;
    private RemoteObjectHandler remoteObjectHandler;

    private final static Logger LOG = LoggerFactory.getLogger(OsgiContainerHandler.class);
    private final static String STATUS_ACTIVE = "ACTIVE";
    private final static String TMP_FOLDER_PREFIX = "/tmp/";
    private final static String OSGI_PROMPT = "osgi>";
    private final static long EXPECT_DEFAULT_WAIT = 300;

    private enum BundleStatus {
        UNINSTALLED,
        INSTALLED,
        RESOLVED,
        STARTING,
        STOPPING,
        ACTIVE,
        LAZY
    }

    public OsgiContainerHandler(Host rcHost, String startupScriptPath, String configPath, int osgiCliPort, int osgiDataPort){
        this.scriptPath = startupScriptPath;
        this.configPath = configPath;
        this.osgiCliPort = osgiCliPort;
        this.osgiDataPort = osgiDataPort;
        this.rcHost = rcHost;
        try{
            User user = rcHost.getUsers(UserType.OPER).get(0);
            cli = new CLI(rcHost, user);
            remoteObjectHandler = new RemoteObjectHandler(rcHost, rcHost.getUsers(UserType.OPER).get(0));
        }catch(IndexOutOfBoundsException e){
            LOG.info("No users exists of type {}", UserType.OPER);
        }
    }

    /**
     * Deploy file as OSGi bundle
     * @param localFile - file containing bundle for deployment
     * @param bundleName - name of the bundle inside OSGi container
     * @return true if bundle was activated successfully
     * @throws ContainerNotReadyException
     */
    public boolean deploy(File localFile, String bundleName) throws ContainerNotReadyException {
        String fileName = String.format("%s%s", TMP_FOLDER_PREFIX, localFile.getName());
        remoteObjectHandler.copyLocalFileToRemote(localFile.getAbsolutePath(), fileName);
        if(!STATUS_ACTIVE.equals(getStatus(bundleName))) {
            install(fileName, bundleName);
            start(bundleName);
        }else {
            LOG.info("Bundle is already installed", bundleName);
        }
        return STATUS_ACTIVE.equals(getStatus(bundleName));
    }

    /**
     * Start first bundle matching filter
     * @param bundleName
     * @throws ContainerNotReadyException
     */
    public void start(String bundleName) throws ContainerNotReadyException{
        try{
            runOsgiCommand("start "+getBundleNumber(bundleName).get(0));
        } catch (IndexOutOfBoundsException e) {
            throw new ContainerNotReadyException(String.format("Cannot start bundle, no bundle found matching %s", bundleName));
        }
        catch (IOException e) {
            LOG.error("Connection dropped while starting bundle");
        }
    }

    /**
     * Stop first bundle matching filter
     * @param bundleName
     * @throws ContainerNotReadyException
     */
    public void stop(String bundleName)throws ContainerNotReadyException{
        try{
            runOsgiCommand("stop "+getBundleNumber(bundleName).get(0));
        } catch (IndexOutOfBoundsException e) {
            throw new ContainerNotReadyException(String.format("Cannot stop bundle, no bundle found matching %s", bundleName));
        }
        catch (IOException e) {
            LOG.error("Connection dropped while stopping bundle");
        }
    }

    /**
     * Get status of first bundle matching filter
     * @param bundleName
     * @return bundles status as OSGi container returns it
     * @throws ContainerNotReadyException
     */
    public String getStatus(String bundleName) throws ContainerNotReadyException{
        LOG.debug("Getting Status of " + bundleName);
        String result = callRunOsgiCommand(String.format("ss %s", bundleName));
        LOG.debug("Getting status from {}", result);
        String [] resSplit = result.split("\n");
        for(String s : resSplit){
            LOG.debug("Checking output "+s+" for Bundle status");
            if(s.contains(bundleName)){
                for (BundleStatus status : BundleStatus.values()){
                    if(s.contains(status.toString())){
                        LOG.debug("Bundle "+bundleName+" is "+ status.toString());
                        return status.toString();
                    }
                }
                throw new ContainerNotReadyException(String.format("No bundle status available for bundle %s", bundleName));
            }
        }
        return result;
    }

    /**
     * Get numbers for bundles matching filter
     * @param bundleName
     * @return
     * @throws ContainerNotReadyException
     */
    public List<String> getBundleNumber(String bundleName) throws ContainerNotReadyException{
        LOG.debug("Getting Number of " + bundleName);
        String result = callRunOsgiCommand(String.format("ss %s", bundleName));
        LOG.debug("Getting bundle number from {}", result);
        String [] resSplit = result.split("\n");
        List <String> results = new ArrayList<>();
        boolean found = false;
        for(String s : resSplit){
            if(s.contains(bundleName)){
                found = true;
                LOG.debug("Checking "+s+" for Bundle Number");
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(s);
                if(m.find()){
                    results.add(m.group());
                }
                else {
                    LOG.warn("Bundle Number not found");
                }
            }
        }
        if(found){
            return results;
        }
        throw new ContainerNotReadyException(String.format("Cannot find number for bundle: %s", bundleName));
    }

    /**
     * @param bundleName
     * @return
     * @throws ContainerNotReadyException 
     */
    private String callRunOsgiCommand(String bundleName) throws ContainerNotReadyException {
        String result = null;
        try{
            result = runOsgiCommand(bundleName);
        }
        catch (IOException e) {
            LOG.error("Connection dropped while starting bundle");
        }
        return result;
    }

    /**
     * Install file as OSGi bundler
     * @param remoteFileLocation
     * @param bundleName
     * @return bundle number
     */
    public String install(String remoteFileLocation, String bundleName){
        try{
            runOsgiCommand(String.format("install file://%s", remoteFileLocation));
        }
        catch (IOException e) {
            LOG.error("Connection dropped while installing bundle");
        }
        String result = null;
        try{
            result = getBundleNumber(bundleName).get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("Installed bundle {} not found", bundleName);
        } catch (ContainerNotReadyException e) {
            LOG.error("Cannot install {}, error during installation",bundleName);
        }
        return result;
    }

    /**
     * Uninstall all bundles matching the filter
     * @param bundleName
     */
    public void uninstall(String bundleName){
        try {
            List<String> bundles = getBundleNumber(bundleName);
            for(String bundle : bundles){
                if(STATUS_ACTIVE.equals(getStatus(bundleName))){
                    runOsgiCommand(String.format("stop %s", bundle));
                }
                
                runOsgiCommand(String.format("uninstall %s", bundle));
            }
        } catch (ContainerNotReadyException e) {
            LOG.debug("Bundle {} is not installed", bundleName);
        }
        catch (IOException e) {
            LOG.error("Connection dropped while uninstalling bundle");
        }
    }

    /**
     * Refresh first bundle matching filter
     * @param bundleName
     * @throws ContainerNotReadyException
     */
    public void refresh(String bundleName)throws ContainerNotReadyException{
        try {
            runOsgiCommand(String.format("refresh %s", getBundleNumber(bundleName).get(0)));
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.warn("Cannot refresh bundle {}", bundleName);
        }
        catch (IOException e) {
            LOG.error("Connection dropped while refreshing bundle");
        }
    }

    /**
     * Stop the OSGi container
     */
    public void stopContainer(){
        try {
            if(shell != null) {
               shell.writeln("exit");
            }
        } catch (NullPointerException e) {
            LOG.warn("Container is not started");
        } finally {
            shell = null;
        }
    }

    /**
     * Start container by invoking specified script. Environment variable DISPLAY is set to value provided in the method
     * @param display
     */
    public void startContainer(String display){
        cli.executeCommand(String.format("declare -x DISPLAY=%s; nohup %s", display, scriptPath));
    }

    /**
     * Verify if OSGi console can be accessed
     * @return
     */
    public boolean canConnect(){
        try {
            new Socket(rcHost.getIp(),osgiCliPort ).close();
            return true;
        } catch (Exception e){
            LOG.warn("Cannot connect to OSGi container console using {} : {} .", rcHost.getIp(), osgiCliPort);
            LOG.debug("OSGi console cannot be accessed, printing stack trace: ", e);
            return false;
        }
    }

    private boolean hasDifferentCliPort(String config){
        return config.contains("-Dosgi.console=") && ! hasConsoleEntry(config);
    }

    private boolean hasDifferentDataPort(String config){
        return config.contains("-Dorg.osgi.service.http.port=") && ! hasDataEntry(config);
    }

    private void changeConfig(boolean clear) throws ContainerNotReadyException{
        Shell configShell = cli.openShell(null);
        configShell.writeln(String.format("cat %s", configPath));
        String configFile = configShell.read();
        if (clear || hasDifferentCliPort(configFile)) {
            configFile = configFile.replaceAll("-Dosgi\\.console=\\d*", "");
        }
        if (clear || hasDifferentDataPort(configFile)) {
            configFile = configFile.replaceAll("-Dorg\\.osgi\\.service\\.http\\.port=\\d*", "");
        }
        if (! clear && ! hasConsoleEntry(configFile)) {
            configFile += "\n-Dosgi.console=" + osgiCliPort;
        }
        if (! clear && ! hasDataEntry(configFile)) {
            configFile += "\n-Dorg.osgi.service.http.port=" + osgiDataPort;
        }
        configFile = configFile.replaceAll("\r\n+", "\n");
        configFile = configFile.replaceAll("\n+", "\n");
        configFile = configFile.replaceAll("\\\\", "\\\\\\\\");
        final String updatedMarker = "UPDATED";
        configShell.writeln(String.format("echo \" %s \" > %s", configFile, configPath));
        configShell.writeln(String.format("echo %s", updatedMarker));
        try {
            configShell.expect(updatedMarker);
        } catch (TimeoutException e){
            throw new ContainerNotReadyException(String.format("Cannot update application configuration file %s", configPath));
        } finally {
            configShell.disconnect();
        }
    }

    /**
     * Update application configuration file so the CLI port and data port can be accessed
     */
    public void updateConfig() {
        try {
            changeConfig(false);
        } catch (ContainerNotReadyException e) {
            LOG.error("Cannot update config file");
        }
    }

    /**
     * Method removing updates to configuration file required for OSGi agent
     */
    public void clearConfig(){
        try {
            changeConfig(true);
        } catch (ContainerNotReadyException e) {
            LOG.warn("Error occurred when cleaning config file");
        }
    }

    private boolean hasConsoleEntry(String config){
        return config.contains(String.format("-Dosgi.console=%s", osgiCliPort));
    }

    private boolean hasDataEntry(String config){
        return config.contains(String.format("-Dorg.osgi.service.http.port=%s", osgiDataPort));
    }

    /**
     * Check if configuration file contains entries for CLI port and data port
     * @return
     */
    public boolean checkConfig() {
        String configFile = cli.executeCommand(String.format("cat %s", configPath)).read();
        LOG.trace("Checking config file {} for {} and {} \n {}",configPath, osgiCliPort, osgiDataPort , configFile);
        return hasConsoleEntry(configFile) && hasDataEntry(configFile);
    }

    /**
     * Run any OSGi command using default timeout of 30 seconds
     * @param command
     * @return String display on OSGi console
     * @throws UnknownHostException
     * @throws IOException
     */
    public String runOsgiCommand(String command) throws IOException{
        return runOsgiCommand(command, EXPECT_DEFAULT_WAIT);
    }

    /**
     * Run any OSGi command
     * @param command
     * @param timeOutSeconds
     * @return String display on OSGi console
     * @throws UnknownHostException
     * @throws IOException
     */
    public String runOsgiCommand(String command, Long timeOutSeconds) throws IOException{
        if (shell == null){
            CLI cli = new CLI(rcHost); 
            shell = cli.openTelnetShell(osgiCliPort);
            shell.expect(OSGI_PROMPT);
        }
        shell.writeln(command);
        String response = shell.expect(OSGI_PROMPT, timeOutSeconds);
        LOG.debug("Response from expect is: "+response);
        return response;
    }

    /**
     * @return the configPath
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * @param configPath the configPath to set
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
