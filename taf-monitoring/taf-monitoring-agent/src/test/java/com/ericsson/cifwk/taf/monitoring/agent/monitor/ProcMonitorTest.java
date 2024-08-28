package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.hamcrest.Matchers.hasKey;

public class ProcMonitorTest {

    @Test
    public void shouldGetSample() throws Exception {
        ProcMonitor monitor = new ProcMonitor(getCurrentPid());
        Assert.assertNotNull(monitor.getSample());
        Assert.assertThat(monitor.getSample(), hasKey("LastTime"));
        Assert.assertThat(monitor.getSample(), hasKey("Percent"));
    }

    private int getCurrentPid() {
        // 3068@E7D4C9EFE79A2C - should be the same on Linux and Windows, but is NOT guaranteed
        String processComplexId = ManagementFactory.getRuntimeMXBean().getName();
        String[] split = StringUtils.split(processComplexId, "@");
        if (split.length != 2) {
            System.out.println(String.format("Failed to get current process id - JMX bean name was '%s'", processComplexId));
            return 0;
        } else {
            String pidStr = split[0];
            try {
                return Integer.parseInt(pidStr);
            } catch (NumberFormatException e) {
                System.out.println(String.format("Failed to parse current process id - '%s'", pidStr));
                return 0;
            }
        }
    }

}