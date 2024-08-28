package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.AbstractServletActivator;
import com.ericsson.cifwk.taf.osgi.agent.BundleContextAwareServlet;
import org.osgi.framework.BundleContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class Activator extends AbstractServletActivator {

    public static final String AGENT_PATH = "/swt-agent";

    @Override
    protected String getServletPath() {
        return AGENT_PATH;
    }

    @Override
    protected Class<? extends BundleContextAwareServlet> getServletClass() {
        return AgentServlet.class;
    }

    @Override
    protected void afterServletStart(BundleContext context) {

    }

    @Override
    protected void afterServletStop(BundleContext context) {

    }

}
