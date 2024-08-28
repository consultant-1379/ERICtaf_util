package com.ericsson.cifwk.taf.allure;


import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * <p>A wrapper for AspectJ Weaver used in Allure core.</p>
 *
 * <p>It is needed to initialize agent in runtime, because AspectJ Weaver agent can only be loaded via CMD option "-javaagent"
 * which is not available when invoking TAF via TAF Maven plugin (powered by maven-exec-plugin).</p>
 */
public class DelegatingJavaAgent {

    private DelegatingJavaAgent() {
        // hiding constructor
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        org.aspectj.weaver.loadtime.Agent.premain(agentArgs, inst);
    }

    public static String getThisAgentJarName() {
        try {
            InputStream propertyStream = DelegatingJavaAgent.class.getClassLoader().getResourceAsStream("agent.properties");
            Properties props = new Properties();
            props.load(propertyStream);
            return props.getProperty("agent.jar.name");
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect agent's JAR name", e); // NOSONAR
        }
    }
}
