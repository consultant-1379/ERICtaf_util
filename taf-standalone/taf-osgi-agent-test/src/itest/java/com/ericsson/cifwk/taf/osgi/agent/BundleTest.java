package com.ericsson.cifwk.taf.osgi.agent;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.inject.Inject;

import static com.ericsson.cifwk.taf.osgi.agent.TestUtils.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class BundleTest {

    private static final int OSGI_CONSOLE_PORT = 12_345;

    @Inject
    private BundleContext ctx;

    @Configuration
    public Option[] config() {
        String localRepo = System.getProperty("maven.repo.local", "");
        return options(
                junitBundles(),

                // for Jenkins
                when(localRepo.length() > 0).useOptions(
                        systemProperty("org.ops4j.pax.url.mvn.localRepository").value(localRepo)
                ),

                mavenBundle("javax.servlet", "servlet-api", "2.5.0.v200806031605"),
                mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "2.1.6"),
                mavenBundle("org.codehaus.groovy", "groovy-all", "2.3.1"),
                mavenBundle("com.ericsson.cifwk", "taf-osgi-agent"),

                systemProperty("osgi.console").value(Integer.toString(OSGI_CONSOLE_PORT))
        );
    }

    @Test
    public void testBundleContext() {
        assertNotNull(ctx);
    }

    @Test
    public void testBundleActivated() throws BundleException {
        final Bundle bundle = getBundle(ctx, BUNDLE_SN);
        assertEquals(Bundle.ACTIVE, bundle.getState());
    }

}
