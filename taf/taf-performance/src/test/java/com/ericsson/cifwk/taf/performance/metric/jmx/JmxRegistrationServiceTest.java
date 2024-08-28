package com.ericsson.cifwk.taf.performance.metric.jmx;

import com.ericsson.cifwk.taf.performance.PerformancePluginServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.StandardMBean;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;

public class JmxRegistrationServiceTest {

    private JmxRegistrationService<StandardMBean> service;

    @Before
    public void setUp() throws Exception {
        service = new JmxRegistrationService<>();
    }

    @After
    public void tearDown() throws Exception {
        service.shutdown();
    }

    @Test
    public void testFindMBean() throws Exception {
        StandardMBean noMBean = service.find(PerformancePluginServices.DEFAULT_JMX_KEY);

        assertEquals(null, noMBean);

        StandardMBean mBean = new StandardMBean("", Serializable.class);
        service.register(mBean, PerformancePluginServices.DEFAULT_JMX_KEY);
        StandardMBean foundMBean = service.find(PerformancePluginServices.DEFAULT_JMX_KEY);

        assertEquals(mBean, foundMBean);
    }

}
