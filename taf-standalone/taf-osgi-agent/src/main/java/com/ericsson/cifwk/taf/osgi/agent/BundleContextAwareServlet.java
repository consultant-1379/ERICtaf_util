package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;
import org.osgi.framework.BundleContext;

import javax.servlet.Servlet;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public interface BundleContextAwareServlet extends Servlet {
    void setBundleContext(BundleContext bundleContext);
}
