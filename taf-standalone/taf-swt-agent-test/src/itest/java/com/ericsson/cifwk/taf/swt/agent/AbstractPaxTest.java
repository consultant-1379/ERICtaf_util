package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.ui.DesktopNavigator;
import com.ericsson.cifwk.taf.ui.UI;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;

public abstract class AbstractPaxTest {

    public static final String SWT_AGENT_BUNDLE_ID = "com.ericsson.cifwk.taf.swt.agent";

    public static final String SWT_SAMPLE_APP_BUNDLE_ID = "com.ericsson.cifwk.taf.osgi.swt-sample-app-bundle";

    private static PaxExamContainer paxExamContainer = new PaxExamContainer(SwtAgentConfiguration.class);
    static {
        paxExamContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                paxExamContainer.stop();
                System.out.println("PAX Container stopped.");
            }
        });
    }

    protected DesktopNavigator swtNavigator;

    public AbstractPaxTest() {
        swtNavigator = UI.newSwtNavigator(getHost());
    }

    public Bundle getBundle(String bundleId) {
        return paxExamContainer.getBundle(bundleId);
    }

    @Before
    public final void before() {
        paxExamContainer.startBundle(SWT_SAMPLE_APP_BUNDLE_ID);
    }

    @After
    public final void after() {
        paxExamContainer.stopBundle(SWT_SAMPLE_APP_BUNDLE_ID);
    }

    private Host getHost() {
        Host host = new Host();
        host.setIp("localhost");
        host.setHostname("localhost");
        Map<Ports, String> map = Maps.newHashMap();
        map.put(Ports.HTTP, "8080");
        host.setPort(map);
        return host;
    }

    public static class SwtAgentConfiguration {

        private static final Logger log = LoggerFactory.getLogger(AbstractPaxTest.class);

        private static final int OSGI_CONSOLE_PORT = 12_345;

        private static Pattern resources = Pattern.compile("swt.artifactId=(.+);taf.version=(.+)");

        @Configuration
        public Option[] config() {
            String localRepo = System.getProperty("maven.repo.local", "");
            String artifactName = getArtifactName();
            String tafVersion = checkNotNull(getTafVersion(), "Please 'maven install' module 'taf-swt-agent-test' before launching integration tests");
            log.info("Artifact used: {}", artifactName);
            log.info("Agent bundle version: {}", tafVersion);
            return options(

                    // for Jenkins
                    when(localRepo.length() > 0).useOptions(systemProperty("org.ops4j.pax.url.mvn.localRepository").value(localRepo)),

                    // HTTP service
                    mavenBundle("javax.servlet", "servlet-api", "2.5.0.v200806031605"),
                    mavenBundle("org.osgi", "org.osgi.compendium", "4.2.0"),
                    mavenBundle("org.osgi", "org.osgi.core", "4.2.0"),
                    mavenBundle("org.ops4j.pax.web", "pax-web-jetty-bundle", "2.0.2"),

                    // logging
                    mavenBundle("org.slf4j", "slf4j-api", "1.7.7"),
                    mavenBundle("org.slf4j", "jcl-over-slf4j", "1.7.7"),
                    mavenBundle("ch.qos.logback", "logback-classic", "1.1.2"),
                    mavenBundle("ch.qos.logback", "logback-core", "1.1.2"),
                    mavenBundle("log4j", "log4j", "1.2.17"),

                    // other dependencies
                    mavenBundle("com.google.code.gson", "gson", "2.2.4"),
                    mavenBundle("com.google.guava", "guava", "15.0"),

                    // SWT library
                    mavenBundle("com.ericsson.cifwk", artifactName, "3.5.1"),

                    // TAF bundles
                    mavenBundle("com.ericsson.cifwk", "swt-sample-app-bundle", tafVersion),
                    mavenBundle("com.ericsson.cifwk", "taf-swt-agent", tafVersion),

                    systemProperty("osgi.console").value(Integer.toString(OSGI_CONSOLE_PORT)));
        }

        private String getArtifactName() {
            try {
                String result = "_bundle";
                InputStream resource = new FileInputStream("./target/classes/swt-artifact.txt");
                Matcher matcher = resources.matcher(IOUtils.toString(resource));
                if (matcher.matches()) {
                    result = matcher.group(1) + result;
                }
                return result;
            } catch (IOException e) {
                return "org.eclipse.swt.win32.win32.x86_64_bundle";
            }
        }

        private String getTafVersion() {
            try {
                InputStream resource = new FileInputStream("./target/classes/swt-artifact.txt");
                Matcher matcher = resources.matcher(IOUtils.toString(resource));
                if (matcher.matches()) {
                    return matcher.group(2);
                }
                return null;
            } catch (IOException e) {
                log.error("", e);
                return null;
            }
        }

    }

}
