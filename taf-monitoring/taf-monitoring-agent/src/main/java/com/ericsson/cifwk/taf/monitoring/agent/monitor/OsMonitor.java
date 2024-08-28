package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import org.apache.commons.beanutils.ConvertUtils;
import org.hyperic.sigar.Sigar;

import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OsMonitor implements Closeable {

    private Sigar sigar;

    Logger log = LoggerFactory.getLogger(OsMonitor.class);

	protected synchronized Sigar getSigar() {
        if (sigar == null) {
            String currentLP = System.getProperty("java.library.path");
            currentLP += File.pathSeparatorChar + new File("").getAbsolutePath() + "/target/lib/";
            System.setProperty("java.library.path", currentLP);
            log.info(System.getProperty("java.library.path"));
            sigar = new Sigar();
        }
        return sigar;
    }

    protected Map<String, Number> convertMap(
            @SuppressWarnings("rawtypes") final Map<String, Object> input) {
        final Map<String, Number> convertedResult = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            convertedResult.put(entry.getKey(), (Number) ConvertUtils.convert(
                    input.get(entry.getKey()), Double.class));
        }
        return convertedResult;
    }

    @Override
    public void close() {
        if (sigar != null)
            sigar.close();
        sigar = null;
    }

}
