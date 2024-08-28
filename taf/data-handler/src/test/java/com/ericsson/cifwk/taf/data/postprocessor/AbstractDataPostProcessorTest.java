package com.ericsson.cifwk.taf.data.postprocessor;

import groovy.util.ConfigObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractDataPostProcessorTest {

    @Test
    public void mappingTest() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("a.n.a", "a");
        map.put("a.aa.a", "a");
        map.put("b.1", "a");
        map.put("b.b.b", "");

        Assert.assertEquals(2, AbstractDataPostProcessor.getMappedValues(map, "a").size());
    }

    @Test
    public void filteringTests() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("a.n.a", "a");
        map.put("a.aa.a", "a");
        map.put("b.1", "a");
        map.put("b.b.b", "");

        Assert.assertEquals(0, AbstractDataPostProcessor.getMappedValues(map, "b").size());
        Assert.assertEquals(2, AbstractDataPostProcessor.getMappedValues(map, "b", false).size());
    }

    @Test
    public void mergeJsonProperties() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(8);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.type", "MS");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node.n1.ip", "10.0.0.2");
        map.put("host.UnitTest.node.n1.offset", "7");
        map.put("host.UnitTest.node.n1.port.rmi", "4441");
        map.put("host.json.path/to/hosts.properties.json", "[{\"hostname\":\"UnitTest\",\"ip\":\"0.0.0.1\"}]");

        // JSON properties should rewrite existing object properties fully
        Map hostsMap = AbstractDataPostProcessor.getMappedValues(map, "host");
        Assert.assertEquals(1, hostsMap.size());

        ConfigObject configObj = (ConfigObject) hostsMap.get("UnitTest");
        Assert.assertNotNull(configObj.get("ip"));
        Assert.assertNull(configObj.get("type"));
        Assert.assertNull(configObj.get("user"));
        Assert.assertNull(configObj.get("node"));
    }

}
