package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.allure.DelegatingJavaAgent;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.logging.StandardOutputSetter;
import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.google.common.annotations.VisibleForTesting;
import com.sun.tools.attach.VirtualMachine; // NOSONAR
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import static java.lang.String.format;

/**
 * TAF Plugin which registers Test NG listeners reporting to Allure framework
 */
public class AllureTafPlugin implements TafPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureTafPlugin.class);

    protected static final String DISABLE_ALLURE_PROPERTY = "allure.report.disable";

    @Override
    public void init() {
        if (shouldAddAllure()) {
            TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
            testEventBus.register(new AllureTafTestListener());
            if (isAspectjWeaverLoaded()) {
                LOGGER.info("AspectJ Weaver javaagent for Allure is already loaded");
            } else {
                initAllureAgent();
            }
            StandardOutputSetter.set();
        }
    }

    @Override
    public void shutdown() {
        if (shouldAddAllure()) {
            StandardOutputSetter.reset();
        }
    }

    private static boolean isAspectjWeaverLoaded() {
        try {
            org.aspectj.weaver.loadtime.Agent.getInstrumentation();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) { // NOSONAR
            return false;
        }
        return true;
    }

    @VisibleForTesting
    protected boolean shouldAddAllure() {
        return System.getProperty(DISABLE_ALLURE_PROPERTY) == null;
    }

    private static void initAllureAgent() {
        LOGGER.info("Searching for AspectJ Weaver javaagent...");
        String agentJarName = DelegatingJavaAgent.getThisAgentJarName();
        String jar = InternalFileFinder.findFile(agentJarName);
        if (jar == null) {
            LOGGER.warn("Failed to find AspectJ Weaver agent's library '{}', test report scope will be limited", agentJarName);
            return;
        }
        LOGGER.info("AspectJ Weaver javaagent jar file found");
        VirtualMachine vm = null;
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try {
            vm = VirtualMachine.attach(pid);
            LOGGER.info("Loading AspectJ Weaver javaagent for Allure advanced features...");
            vm.loadAgent(jar, "");
        } catch (Exception | UnsatisfiedLinkError e) {
            LOGGER.error(format("Failed to load AspectJ Weaver agent's library '%s', test report scope will be limited.", jar), e);
            return;
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) { // NOSONAR
                    // ignore
                }
            }
        }
        LOGGER.info("AspectJ Weaver javaagent for Allure successfully loaded");
    }

}
