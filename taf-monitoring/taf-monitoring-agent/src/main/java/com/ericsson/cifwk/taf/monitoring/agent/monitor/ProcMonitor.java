package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import com.ericsson.cifwk.taf.monitoring.agent.process.ProcUtil;
import com.ericsson.cifwk.taf.monitoring.config.MonitorConfig;
import com.ericsson.taf.graphite.Monitor;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProcMonitor extends OsMonitor implements Monitor {

    long process;

    private Logger logger = LoggerFactory.getLogger(MemMonitor.class);

    public ProcMonitor(long process) {
        this.process = process;
    }


    @Override
    public Map<String, Number> getSample() {
        final Map<String, Number> result = new HashMap<>();
        result.putAll(getCpuInfo());
        result.putAll(getMemInfo());
        return result;
    }

    public Map<String, Number> getCpuInfo() {
        Map<String, Number> cpuInfo = new HashMap<>();
        try {
            cpuInfo.putAll(convertMap(getSigar().getProcCpu(process).toMap()));
        } catch (SigarException e) {
            logger.debug("Failed To convert CPU info Map {}", e);
        }
        return cpuInfo;
    }

    public Map<String, Number> getMemInfo() {
        Map<String, Number> memInfo = new HashMap<>();
        try {
            memInfo.putAll(convertMap(getSigar().getProcMem(process).toMap()));
        } catch (SigarException e) {
            logger.debug("Failed To convert memory info Map {}", e);
        }
        return memInfo;
    }

    public static Map<String, Long> getProcess(MonitorConfig config) {
        ProcUtil util = new ProcUtil();
        Map<String, Long> processes = new HashMap<>();
        String processName = config.getMonitorName();
        for (long pid : util.getPID(config.getAddress())) {
            processes.put(processName + "_" + pid, pid);
        }
        util.close();
        return processes;
    }

}
