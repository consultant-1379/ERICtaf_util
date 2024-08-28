package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import java.util.Map;

import com.ericsson.taf.graphite.Monitor;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemMonitor extends OsMonitor implements Monitor {

    private Logger logger = LoggerFactory.getLogger(MemMonitor.class);

    @Override
    public Map<String, Number> getSample() {
        return getMemInfo();
    }

    public Map<String, Number> getMemInfo() {
        try {
            return convertMap(getSigar().getMem().toMap());
        } catch (SigarException e) {
            logger.debug("Failed to convert memory info map {}", e);
            return null;
        }
    }
}
