package com.ericsson.cifwk.taf.osgi.agent;

import org.junit.Test;

import static com.ericsson.cifwk.taf.osgi.agent.TestUtils.readResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GroovyInvocationTest {

    @Test
    public void testInvoke() throws Exception {
        String source = readResource("scripts/AddOperator.groovy");
        GroovyInvocation adder = GroovyInvocation.fromSource(source);
        Object result = adder.invoke("add", "2", "3");

        assertEquals("5", result);
    }

    @Test
    public void testInvalidInvoke() throws Exception {
        String source = readResource("scripts/AddOperator.groovy");
        GroovyInvocation adder = GroovyInvocation.fromSource(source);
        try {
            adder.invoke("add", "2");
            fail();
        } catch (Exception ignored) {
        }
        try {
            adder.invoke("xyzzy");
            fail();
        } catch (Exception ignored) {
        }
    }

}
