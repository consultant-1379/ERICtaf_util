package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.BundleContextAwareServlet;
import com.ericsson.cifwk.taf.osgi.agent.ServletRouter;
import com.ericsson.cifwk.taf.swt.agent.handler.CloseHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.PingHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.ScreenshotHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.SwtWindowHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.WidgetHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.WindowHandler;
import com.ericsson.cifwk.taf.swt.agent.handler.WindowsHandler;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
@SuppressWarnings("serial")
public class AgentServlet extends HttpServlet implements BundleContextAwareServlet {

    private final ServletRouter router = new ServletRouter();

    private BundleContext bundleContext;

    public AgentServlet() {
    }

    @Override
    public void init() throws ServletException {
        ObjectLocator objectLocator = new ObjectLocator();
        router.register("POST", "/widgets/(.+)", new WidgetHandler(objectLocator));
        // router.register("GET", "/widgets/(.+)", new WidgetGetHandler(objectLocator));
        router.register("DELETE", "/widgets/", new CloseHandler(objectLocator));
        router.register("GET", "/swt-windows/(.+)", new SwtWindowHandler());
        router.register("GET", "/windows/(.+)\\.json", new WindowHandler());
        router.register("GET", "/windows/(.+)\\.png", new ScreenshotHandler());
        router.register("GET", "/windows/", new WindowsHandler());
        router.register("GET", "/", new PingHandler());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        router.handle(req, resp);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
