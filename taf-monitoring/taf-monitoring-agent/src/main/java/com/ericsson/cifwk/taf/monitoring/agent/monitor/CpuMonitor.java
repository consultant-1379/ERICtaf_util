package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.taf.graphite.Monitor;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpuMonitor extends OsMonitor implements Monitor {

    private Logger logger = LoggerFactory.getLogger(CpuMonitor.class);

    public Map<String, Number> getCpuInfo() {
        try {
            return convertMap(getSigar().getCpu().toMap());
        } catch (SigarException e) {
            logger.debug("Failed get CPU info {}", e);
            return null;
        }
    }

    public Map<String, Double> getCpuPerc() {
        CpuPerc res;
        Map<String, Double> cpuPercResult = new HashMap<>();
        try {
            res = getSigar().getCpuPerc();
            cpuPercResult.put("IdlePercentage", res.getIdle());
            cpuPercResult.put("CombinedPercentage", res.getCombined());
        } catch (SigarException e) {
            logger.debug("Failed to get CPU percentages {}", e);
            cpuPercResult = null;
        }
        return cpuPercResult;

    }

    @Override
    public Map<String, Number> getSample() {
        final Map<String, Number> result = new HashMap<>();
        result.putAll(getCpuInfo());
        result.putAll(getCpuPerc());
        return result;
    }

}
