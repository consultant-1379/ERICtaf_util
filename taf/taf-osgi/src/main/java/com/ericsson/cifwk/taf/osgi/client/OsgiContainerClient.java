package com.ericsson.cifwk.taf.osgi.client;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.utils.FileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class managing configuration of OSGi client Requires port 12345 for OSGi
 * command line interface to be available from the test running machine; TAF
 * agent port is discovered automatically
 */
public abstract class OsgiContainerClient {

    private static Logger log = LoggerFactory.getLogger(OsgiContainerClient.class);

    public static final String CLI_PORT_PROP = "osgiCliPort";
    public static final String DATA_PORT_PROP = "osgiDataPort";

    private final int cliPort;
    protected final int dataPort;
    private final OsgiContainerHandler handler;
    protected final Host host;

    private static final ReentrantReadWriteLock osgiContainerLock = new ReentrantReadWriteLock();

    public abstract List<String> getBundles();

    public int getDataPort() {
        return dataPort;
    }

    public int getCliPort() {
        return cliPort;
    }

    /**
     * Constructor allowing to specify the container specifics
     * @param hostname
     *            - the given host name or IP of host
     * @param port
     *            - the given port number
     * @param startupScriptPath
     *            - path script used to start the application
     * @param configPath
     *            - path to file containing configuration of the OSGi container
     *            for the application
     */
    protected OsgiContainerClient(String hostname, int port, String startupScriptPath, String configPath) {
        this(Host.builder().withName(hostname).withHttpPort(port).build(), startupScriptPath, configPath);
    }

    /**
     * Constructor allowing to specify the container specifics
     *
     * @param host
     *            - OS with the OSGi application
     * @param startupScriptPath
     *            - path script used to start the application
     * @param configPath
     *            - path to file containing configuration of the OSGi container
     *            for the application
     */
    protected OsgiContainerClient(Host host, String startupScriptPath,
                                  String configPath) {
        this.host = host;
        cliPort = Integer.valueOf(System.getProperty(CLI_PORT_PROP,
                String.valueOf(PortProvider.getCliPort(host))));
        dataPort = Integer.valueOf(System.getProperty(DATA_PORT_PROP,
                String.valueOf(PortProvider.getDataPort(host))));
        handler = new OsgiContainerHandler(host, startupScriptPath, configPath,
                cliPort, dataPort);
        log.debug(String.format("Using CLI port %d and Data port %d",getCliPort(), getDataPort()));
    }

    /**
     * Check if communication with OSGi container is possible
     */
    public boolean canConnect() {
        return handler.canConnect();
    }

    /**
     * Get instance of OsgiContainerHandler
     *
     * @return
     */
    public OsgiContainerHandler getOsgiContainerHandler() {
        return handler;
    }

    /**
     * Prepare configuration of the application if necessary
     *
     * @throws ContainerNotReadyException
     */
    public void prepareApplication() throws ContainerNotReadyException {
        int stubbornnessLevel = 3;
        while (!handler.checkConfig() && stubbornnessLevel > 0) {
            log.debug("Updating application config file");
            handler.updateConfig();
            stubbornnessLevel--;
        }

        if (!handler.checkConfig()) {
            throw new ContainerNotReadyException("Cannot update config file "
                    + handler.getConfigPath());
        }
    }

    /**
     * Prepare configuration and bundles of the application. If application
     * cannot be connected, it will be started
     *
     * @param display
     *            - value of DISPLAY variable to be set for newly started
     *            application
     * @param applicationStartupTime
     *            - application startup time before OSGi container can be
     *            reached
     * @throws ContainerNotReadyException
     */
    public void prepare(String display, Long applicationStartupTime)
            throws ContainerNotReadyException {
        osgiContainerLock.writeLock().lock();
        log.debug("Preparing OSGi application");
        try {
            prepareApplication();
            if (!handler.canConnect()) {
                log.debug("Starting OSGi container");
                handler.startContainer(display);
            }
            try {
                Thread.sleep(applicationStartupTime);
            } catch (InterruptedException ignore) {
                log.debug("Application startup has been interrupted", ignore);
                Thread.currentThread().interrupt();
            }
            handler.clearConfig();
            prepareBundles();

        } finally {
            osgiContainerLock.writeLock().unlock();
        }
        log.debug("OSGi container prepared");

    }

    /**
     * Stop the application
     */
    public void stopApplication() {
        handler.stopContainer();
    }

    /**
     * Deploy and activate bundles required for API tests inside OSGi container.
     * Assumes that configuration is correct and application is started
     *
     * @throws ContainerNotReadyException
     */
    public void prepareBundles() throws ContainerNotReadyException {
        if (!handler.canConnect()) {
            throw new ContainerNotReadyException(
                    "Cannot connect to OSGi container using port "
                            + getCliPort());
        }
        handler.start("http.jetty");
        deployBundles();
    }

    protected String getHostName() {
        return host.getIp();
    }

    private void deployBundles() throws ContainerNotReadyException {
        for(String file: getBundles()){

            handler.uninstall(getBundleName(file));
            log.info("Attempting to deploy {} bundle", file);
            if (! handler.deploy(getFileFromList(FileFinder.findFile("jar", file)), getBundleName(file)))
                throw new ContainerNotReadyException("Bundle " + file + " cannot be activated");
        }
    }

    private File getFileFromList(List<String> fileNames) {
        for (String fileName : fileNames) {
            File f = new File(fileName);
            if (!f.isDirectory()) {
                return f;
            }
        }
        return null;
    }

    private String getBundleName(String file){
        if(file.startsWith("taf") && file.endsWith("agent")){
            file = file.replace("-",".");
        }
        return file;
    }
}
