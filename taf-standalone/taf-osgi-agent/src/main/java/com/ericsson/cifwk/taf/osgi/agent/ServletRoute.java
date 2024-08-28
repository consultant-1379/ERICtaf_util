package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Route description for the servlet router, consists of a method name and a
 * relative path pattern.
 * 
 * @see com.ericsson.cifwk.taf.osgi.agent.ServletRouter
 */
@API(Internal)
public class ServletRoute {

    private static final Logger log = Logger.getLogger(ServletRoute.class.getName());

    private final String method;
    private final Pattern path;

    public ServletRoute(String method, Pattern path) {
        this.method = method;
        this.path = path;
    }

    /**
     * Checks if the given request matches this route.
     * 
     * @param req
     *            request to match
     * @return true, if request was made to this route
     */
    public MatchResult match(HttpServletRequest req) {
        String debugMessage = String.format("Expected %s:%s, Actual %s:%s", method, path, req.getMethod(), getRelativePath(req));
        log.log(Level.FINEST, debugMessage);
        Matcher matcher = path.matcher(getRelativePath(req));
        if (req.getMethod().equals(method) && matcher.matches()) {
            return matcher.toMatchResult();
        }
        return null;
    }

    private String getRelativePath(HttpServletRequest req) {
        String path = req.getPathInfo();
        return path != null ? path : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ServletRoute that = (ServletRoute) o;

        if (!method.equals(that.method))
            return false;
        if (!path.equals(that.path))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

}
