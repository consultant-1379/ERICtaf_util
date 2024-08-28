package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public abstract class AbstractServletActivator implements BundleActivator {

    private static final long BUNDLE_ACTIVATION_TIMEOUT = 60000;

    @Override
    public final void start(BundleContext context) throws Exception {
        BundleContextAwareServlet servlet = getServlet(context);
        HttpService http = getHttpService(context);
        http.registerServlet(getServletPath(), servlet, null, null);
        afterServletStart(context);
    }

    protected final BundleContextAwareServlet getServlet(BundleContext context) {
        Class<? extends BundleContextAwareServlet> servletClass = getServletClass();
        try {
            BundleContextAwareServlet bundleServlet = servletClass.newInstance();
            bundleServlet.setBundleContext(context);
            return bundleServlet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void stop(BundleContext context) throws Exception {
        HttpService http = getHttpService(context);
        http.unregister(getServletPath());
        afterServletStop(context);
    }

    public static HttpService getHttpService(BundleContext context) {
        long activationStarted = System.currentTimeMillis();
        ServiceReference sr = context.getServiceReference(HttpService.class.getName());

        /*
         * During OSGi container start-up every bundle is activated in random order, so
         * HTTP service bundle might still not be active - waiting for dependent bundle activation.
         */
        while (sr == null && (System.currentTimeMillis() - activationStarted) < BUNDLE_ACTIVATION_TIMEOUT) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sr = context.getServiceReference(HttpService.class.getName());
        }
        return (HttpService) context.getService(sr);
    }

    protected abstract String getServletPath();

    protected abstract Class<? extends BundleContextAwareServlet> getServletClass();

    protected abstract void afterServletStart(BundleContext context) throws Exception;

    protected abstract void afterServletStop(BundleContext context) throws Exception;
}
