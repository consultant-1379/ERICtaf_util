package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Utility class for routing requests to matching handlers.
 * 
 * @see com.ericsson.cifwk.taf.osgi.agent.ServletRoute
 * @see com.ericsson.cifwk.taf.osgi.agent.ServletHandler
 */
@API(Internal)
public class ServletRouter {

    private final Map<ServletRoute, ServletHandler> routeMap;

    public ServletRouter() {
        routeMap = new HashMap<ServletRoute, ServletHandler>();
    }

    /**
     * Registers a route handler.
     * 
     * @param method
     *            HTTP method
     * @param path
     *            path pattern
     * @param handler
     *            handler to invoke on match
     */
    public void register(String method, String path, ServletHandler handler) {
        Pattern pathPattern = Pattern.compile(path, Pattern.CASE_INSENSITIVE);
        register(method, pathPattern, handler);
    }

    /**
     * Registers a route handler.
     * 
     * @param method
     *            HTTP method
     * @param path
     *            path pattern
     * @param handler
     *            handler to invoke on match
     */
    public void register(String method, Pattern path, ServletHandler handler) {
        ServletRoute route = new ServletRoute(method, path);
        routeMap.put(route, handler);
    }

    /**
     * Tries to match registered routes sequentially and invokes the handler for
     * the matching route, if any. If multiple routes match, only the handler
     * for the earlier-added route is invoked.
     * 
     * @param req
     *            request object
     * @param resp
     *            response object
     * @throws ServletException
     * @throws IOException
     */
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        for (ServletRoute route : routeMap.keySet()) {
            MatchResult matches = route.match(req);
            if (matches != null) {
                routeMap.get(route).handle(req, resp, matches);
                return;
            }
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Sets up a 400 response with the given message.
     * 
     * @param resp
     *            response object to set up
     * @param message
     *            message to use as body
     * @throws IOException
     */
    public void badRequest(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("text/plain");
        resp.getWriter().print(message);
    }

}
