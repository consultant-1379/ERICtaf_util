package com.ericsson.cifwk.taf.monitoring.agent.process;


import com.ericsson.cifwk.taf.monitoring.agent.monitor.OsMonitor;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProcUtil extends OsMonitor {

    private Sigar sigar = getSigar();
    private Logger logger = LoggerFactory.getLogger(ProcUtil.class);
    /**
     * @param processes
     * @return PID of specified process
     */
    public List<Long> getPID(String processes) {
        List<Long> processIndex = new ArrayList<>();
        try {
            for (long processId : sigar.getProcList()) {
                if (containsAll(processId, processes)) {
                    processIndex.add(processId);
                }
            }
        } catch (SigarException e) {
            logger.debug("Could not get process list {}", e);
        }
        return processIndex;
    }

    private boolean containsAll(long processId, String processes) {
        boolean addIndex = true;
        for (String processName : getProcessDescription(processes)) {
            if (!includedInProcessArgs(processId, processName)) {
                addIndex = false;
            }
        }
        return addIndex;
    }

    private boolean includedInProcessArgs(Long processId, String process) {
        return getProcArgs(processId).contains(process);
    }

    private String getProcArgs(long process) {
        String processArgs = "";
        try {
            for (String arg : sigar.getProcArgs(process)) {
                processArgs += arg + " ";
            }
        } catch (SigarException e) {
            logger.debug("Could not get process arguments {}", e);
        }
        return processArgs;
    }

    private String[] getProcessDescription(String process) {
        return process.split(",");
    }

}
