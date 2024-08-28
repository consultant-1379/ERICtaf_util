package com.ericsson.cifwk.taf.osgi.client;

import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.ericsson.cifwk.taf.osgi.agent.AgentServlet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.ericsson.cifwk.taf.osgi.client.TestUtils.readResource;
import static org.junit.Assert.*;

public class ApiClientTest {

    private static EmbeddedJetty jetty;

    private ApiClient client;

    @BeforeClass
    public static void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new AgentServlet(null), "/agent/*")
                .start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        jetty.stop();
    }

    @Before
    public void beforeMethod() throws Exception {
        client = JavaApi.createApiClient("http://localhost:" + jetty.getPort() + "/agent");
    }

    @Test
    public void shouldBeAlive() throws Exception {
        assertTrue(client.isAlive());
    }

    @Test
    public void shouldInvokeMethodsRemotely() throws Exception {
        String className = "scripts.MulOperator";
        registerSource(className, "scripts/MulOperator.groovy");

        ApiResponse firstResponse = client.invoke(className, "mul", "2", "3");

        assertTrue(firstResponse.isSuccess());
        assertEquals(firstResponse.getValue(), "6");

        ApiResponse secondResponse = client.invoke(className, "mul", "4", "3");

        assertTrue(secondResponse.isSuccess());
        assertEquals(secondResponse.getValue(), "12");
    }

    @Test
    public void shouldFailExecution() throws Exception {
        String className = "scripts.MulOperator";
        registerSource(className, "scripts/MulOperator.groovy");

        ApiResponse missingArgumentResponse = client.invoke(className, "mul", "2");
        assertFalse(missingArgumentResponse.isSuccess());

        ApiResponse missingMethodResponse = client.invoke(className, "xyzzy");
        assertFalse(missingMethodResponse.isSuccess());
    }

    @Test
    public void shouldFailOnMissingClass() throws Exception {
        String className = "com.example.NoSuchClass";

        ApiResponse response = client.invoke(className, "method");

        assertFalse(response.isSuccess());
    }

    private String registerSource(String className, String path) throws Exception {
        String source = readResource(path);

        ApiResponse response = client.register(source);

        assertTrue(response.isSuccess());
        assertEquals(response.getValue(), className);
        return className;
    }

}
