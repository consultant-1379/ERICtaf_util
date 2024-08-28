package com.ericsson.cifwk.taf.management.jmx;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.management.TafManager;
import com.ericsson.cifwk.taf.management.TafRunnerContext;


public class TafJMXManagement {

    public static final String TAF_MANAGEMENT_SYS_PROPERTY = "taf.management";
    public static final Logger LOGGER = LoggerFactory.getLogger(TafManager.class);

    public static final String JMX_BEAN_NAME = "com.ericsson.cifwk.taf:type=TafManager";
    public static final String SERVICE_NAME = "localjmx";

    private static final int SHUTDOWN_WAIT_REPEAT_COUNT = 600;
    private static final int SHUTDOWN_WAIT_TIMEOUT_MS = 1000;

    private MBeanServer jmxServer;


    public void start() {
        if (!enabled()) return;
        stop_old_taf();
        start_jmx();
    }


    public void stop() {
        try {
            if (jmxServer != null) jmxServer.unregisterMBean(new ObjectName(JMX_BEAN_NAME));
        } catch (Exception e) {
        }
    }

    protected boolean enabled() {
        if (!System.getProperties().containsKey(TAF_MANAGEMENT_SYS_PROPERTY)) return false;
        String property = System.getProperty(TAF_MANAGEMENT_SYS_PROPERTY, "*");
        if (property == null || property.trim().isEmpty() || "*".equals(property)) return true;
        String[] services = property.split(";");
        for (String name : services) {
            if (SERVICE_NAME.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    void start_jmx() {
        try {
            LOGGER.info("Start new JMX for TAF[" + TafRunnerContext.getContext().getName() + "]");
            jmxServer = ManagementFactory.getPlatformMBeanServer();
            TafManager mbean = new TafJMXManager();
            jmxServer.registerMBean(mbean, new ObjectName(JMX_BEAN_NAME));
        } catch (Exception e) {
        }
    }

    void stop_old_taf() {
        LOGGER.info("Try to find end stop old running TAF[" + TafRunnerContext.getContext().getName() + "]");
        List<VirtualMachineDescriptor> vms = getVirtualMachines();
        for (VirtualMachineDescriptor desc : vms) {
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(desc);
                try {
                    stopTafOn(vm);
                } catch (Exception e) {
                    loadAgent(vm).stopTafOn(vm);
                }
            } catch (Exception ignored) {
                LOGGER.error("Can't connect to JMX on VM " + desc.displayName() + ":" + desc.id());
            } finally {
                try {
                    if (vm != null) vm.detach();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void stopTafOn(VirtualMachine vm) throws JMException, IOException {
        JMXConnector jmx = null;
        try {
            String connectorAddress = getJmxLocalConnectorAddress(vm);
            JMXServiceURL url = new JMXServiceURL(connectorAddress);
            jmx = JMXConnectorFactory.connect(url);
            stop(jmx);
        } finally {
            try {
                if (jmx != null) jmx.close();
            } catch (Exception ignore) {/*ignore*/}
        }
    }

    void stop(JMXConnector jmx) throws JMException, IOException {
        MBeanServerConnection connection = jmx.getMBeanServerConnection();
        TafManager mbean = JMX.newMBeanProxy(connection, new ObjectName(JMX_BEAN_NAME), TafManager.class);
        String tafName = mbean.getName();
        if (!tafName.equals(TafRunnerContext.getContext().getName())) {
            LOGGER.info("TAF[" + tafName + "] not same as new TAF[" + TafRunnerContext.getContext().getName() + "]");
            return;
        }
        boolean isForceKill = System.getProperties().containsKey(TAF_MANAGEMENT_SYS_PROPERTY + ".forcekill");
        if (!isForceKill && shutdown(mbean)) {
            LOGGER.info("TAF[" + tafName + "] old version stopped");
        } else {
            mbean.kill();
            LOGGER.info("TAF[" + tafName + "] old version killed");
        }
    }

    boolean shutdown(TafManager mbean) {
        mbean.shutdown();
        int repeatCount = SHUTDOWN_WAIT_REPEAT_COUNT;
        while (!mbean.isTerminated() && repeatCount > 0) {
            repeatCount--;
            try {
                Thread.sleep(SHUTDOWN_WAIT_TIMEOUT_MS);
            } catch (InterruptedException ignored) {
            }
        }
        return repeatCount > 0;
    }


    TafJMXManagement loadAgent(VirtualMachine vm) throws IOException, AgentLoadException, AgentInitializationException {
        String agentJar = "";
        try {
            String javaHome = vm.getSystemProperties().getProperty("java.home");
            agentJar = javaHome + File.separator + "lib" + File.separator + "management-agent.jar";
            vm.loadAgent(agentJar, "com.sun.management.jmxremote");
        } catch (Exception e) {
            LOGGER.error("Can't load agent [" + agentJar + "] for VM:" + vm);
            throw e;
        }
        return this;
    }

    private static List<VirtualMachineDescriptor> getVirtualMachines() {
        try {
            return VirtualMachine.list();
        } catch (Exception e) {
            LOGGER.error("Can't get VirtualMachineDescriptors from JVM", e);
            return Collections.EMPTY_LIST;
        }
    }

    private static String getJmxLocalConnectorAddress(VirtualMachine vm) throws IOException {
        String connectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
        if (connectorAddress == null || connectorAddress.trim().isEmpty()) {
            connectorAddress = vm.getSystemProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
        }
        return connectorAddress;
    }


}
