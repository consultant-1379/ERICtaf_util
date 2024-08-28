package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.hasKey;

public class MemoryMonitorTest {

    @Test
    public void shouldGetSample() throws Exception {
        MemMonitor memoryMonitor = new MemMonitor();
        Assert.assertNotNull(memoryMonitor.getSample());
        Assert.assertThat(memoryMonitor.getSample(), hasKey("FreePercent"));
        Assert.assertThat(memoryMonitor.getSample(), hasKey("ActualUsed"));
    }
}
