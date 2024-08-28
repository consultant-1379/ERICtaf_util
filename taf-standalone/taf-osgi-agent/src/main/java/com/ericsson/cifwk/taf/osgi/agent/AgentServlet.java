package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class AgentServlet extends HttpServlet implements BundleContextAwareServlet {

    private static final Logger log = Logger.getLogger(AgentServlet.class.getName());

    private BundleContext bundleContext;
    private final Map<String, GroovyInvocation> invocationMap;
    private final ServletRouter router;

    public AgentServlet(BundleContext bundleContext) {
        this();
        this.bundleContext = bundleContext;
    }

    public AgentServlet() {
        invocationMap = new ConcurrentHashMap<String, GroovyInvocation>();
        router = new ServletRouter();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        router.register("GET", "", new PingHandler());
        router.register("POST", "/classes", new RegisterClassHandler());
        router.register("POST", "/classes/([a-z\\.]+)", new InvokeClassHandler());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        router.handle(req, resp);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Handler for agent ping requests.
     */
    private class PingHandler implements ServletHandler {
        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp, MatchResult matches) throws ServletException, IOException {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("OK");
            resp.getWriter().flush();
        }
    }

    /**
     * Handler for class registration requests. Creates a new instance based on
     * a class and stores it in memory for future method invocations.
     */
    private class RegisterClassHandler implements ServletHandler {
        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp, MatchResult matches) throws ServletException, IOException {
            resp.setContentType("text/plain");
            GroovyInvocation invocation;
            try {
                invocation = GroovyInvocation.fromRequest(req);
            } catch (GroovyInvocationException e) {
                log.log(Level.SEVERE, "Class loading exception", e);
                router.badRequest(resp, getMessages(e));
                return;
            }

            String name = invocation.invocationClass().getName();
            invocationMap.put(name, invocation);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(name);
        }
    }

    /**
     * Handler for method invocation on instances of registered classes.
     */
    private class InvokeClassHandler implements ServletHandler {
        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp, MatchResult matches) throws ServletException, IOException {
            resp.setContentType("text/plain");

            String name = matches.group(1);
            GroovyInvocation invocation = invocationMap.get(name);
            if (invocation == null) {
                router.badRequest(resp, "class \"" + name + "\" is not registered");
                return;
            }

            Object result;
            try {
                result = invocation.invoke(req);
            } catch (GroovyInvocationException e) {
                log.log(Level.SEVERE, "Method invocation exception", e);
                router.badRequest(resp, getMessages(e));
                return;
            }
            if (result != null) {
                resp.getWriter().print(result.toString());
            }
        }
    }

    public static String getMessages(Throwable e) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        e.printStackTrace(writer);
        writer.flush();
        return out.toString();
    }
}
