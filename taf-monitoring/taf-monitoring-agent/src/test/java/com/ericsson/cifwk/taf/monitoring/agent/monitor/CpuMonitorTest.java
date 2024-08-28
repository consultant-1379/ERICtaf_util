package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.hasKey;

public class CpuMonitorTest {

    @Test
    public void shouldGetSample() throws Exception {
        CpuMonitor cpuMonitor = new CpuMonitor();
        Assert.assertNotNull(cpuMonitor.getSample());
        Assert.assertThat(cpuMonitor.getSample(), hasKey("IdlePercentage"));
        Assert.assertThat(cpuMonitor.getSample(), hasKey("CombinedPercentage"));
    }
}
