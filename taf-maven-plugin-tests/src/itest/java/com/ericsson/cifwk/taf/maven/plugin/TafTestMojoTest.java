package com.ericsson.cifwk.taf.maven.plugin;

import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.taf.maven.plugin.AssertUtils.assertSuiteExists;
import static com.ericsson.cifwk.taf.maven.plugin.AssertUtils.assertSurefireExists;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.buildTestware;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.copyPomToTemp;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.runPom;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by ekonsla on 06/01/2016.
 */
public class TafTestMojoTest {

    private static final Logger LOG = LoggerFactory.getLogger(TafTestMojoTest.class);

    @BeforeClass
    public static void setUp() throws Exception {
        buildTestware();
    }

    @Test
    public void shouldUnpackSuites() throws Exception {
        String workspace = copyPomToTemp("no-surefire-pom.xml");
        runTests("no-surefire-pom.xml", workspace);
        assertSuiteExists(workspace, "mySuite.xml");
    }

    @Test
    public void shouldRunWithSurefire() throws Exception {
        String workspace = copyPomToTemp("use-surefire-pom.xml");
        runTests("use-surefire-pom.xml", workspace);
        assertSurefireExists(workspace);
    }

    @Test
    public void shouldIgnoreZeroTestsWhenGroupsAreDefined() throws Exception {
        InvocationResult invocationResult = runTests("no-tests-groups-pom.xml", null);
        assertThat(invocationResult.getExitCode()).isEqualTo(0);
    }

    private InvocationResult runTests(String pomName, String workspace) throws Exception {
        if (workspace == null) {
            workspace = copyPomToTemp(pomName);
        }
        InvocationResult invocationResult = runPom(workspace + "/" + pomName, "clean", "test");
        int exitCode = invocationResult.getExitCode();

        CommandLineException exception = invocationResult.getExecutionException();
        if (exception != null) {
            LOG.error("Maven execution failed", exception);
            exception.printStackTrace();
        }

        assertThat(exitCode).as("Maven execution exit code").isEqualTo(0);
        return invocationResult;
    }

}


