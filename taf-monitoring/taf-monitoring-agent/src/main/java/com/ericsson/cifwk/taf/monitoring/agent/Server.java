package com.ericsson.cifwk.taf.monitoring.agent;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.CpuMonitor;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.JBossMonitor;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.JMXMonitor;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.MemMonitor;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.MonitorType;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.ProcMonitor;
import com.ericsson.cifwk.taf.monitoring.config.MonitorConfig;
import com.ericsson.cifwk.taf.monitoring.config.MonitorPropertiesProcessor;
import com.ericsson.taf.graphite.GraphiteEmitter;
import com.ericsson.taf.graphite.Monitor;

import javax.management.JMException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private final GraphiteEmitter reporter = new GraphiteEmitter();

    private final List<Monitor> monitorRegistry = new ArrayList<>();
    Logger log = LoggerFactory.getLogger(Server.class);

    public void start() {
        long monitoringInterval = Beemo.getMonitoringInterval();
        for (MonitorConfig config : MonitorPropertiesProcessor.process(Beemo.getConfiguration())) {
            switch (config.getType()) {
                case OS:
                    registerOSMonitor(config);
                    break;
                case PROC:
                    registerProcMonitor(config);
                    break;
                case JMX:
                    registerJMXMonitor(config);
                    break;
                case JBOSS:
                    registerJMXMonitor(config);
                    break;
                default:
                    registerOSMonitor(config);
                    break;
            }
        }
        reporter.start(monitoringInterval, TimeUnit.MILLISECONDS);
    }


    public void stop() {
        reporter.stop();
        for (Monitor monitor : monitorRegistry) {
            monitor.close();
        }
        Beemo.close();
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        while (!Beemo.shouldShutdown()) {
            try {
                Thread.sleep(Beemo.getReloadInterval());
            } catch (InterruptedException e) {
                // NOSONAR
                Thread.currentThread().interrupt();
            }
        }
        server.stop();
    }

    private Map<String, ProcMonitor> getProcMonitor(MonitorConfig config) {
        Map<String, ProcMonitor> procMonitorMap = new HashMap<>();
        for (Map.Entry<String, Long> process : ProcMonitor.getProcess(config).entrySet()) {
            procMonitorMap.put(process.getKey(), new ProcMonitor(process.getValue()));
        }
        return procMonitorMap;
    }

    private void registerJMXMonitor(MonitorConfig monitor) {
        String beanName = monitor.getAddress();
        String simpleMBeanName = beanName.substring(beanName.lastIndexOf("=") + 1);
        for (String hostName : monitor.getHosts()) {
            Host host = DataHandler.getHostByName(hostName);
            if (monitor.getType().equals(MonitorType.JBOSS)) {
                try {
                    reporter.registerGauges(hostName + "." + simpleMBeanName, new JBossMonitor(host, beanName));
                } catch (IOException | JMException e) {
                    log.debug("Failed to register Gaues for" + hostName + "." + simpleMBeanName, e);
                }
            } else {
                try {
                    reporter.registerGauges(hostName + "." + simpleMBeanName, new JMXMonitor(host, beanName));
                } catch (IOException | JMException e) {
                    log.debug("Failed to register Gauges for" + hostName + "." + simpleMBeanName, e);
                }
            }
        }

    }

    private void registerOSMonitor(MonitorConfig monitor) {
        String monitorName = monitor.getMonitorName() + ".os";
        reporter.registerGauges(monitorName + ".cpu", new CpuMonitor());
        reporter.registerGauges(monitorName + ".memory", new MemMonitor());
    }

    private void registerProcMonitor(MonitorConfig monitor) {
        for (Map.Entry<String, ProcMonitor> procMonitorEntry : getProcMonitor(monitor).entrySet()) {
            reporter.registerGauges(procMonitorEntry.getKey(), procMonitorEntry.getValue());
        }
    }
}
