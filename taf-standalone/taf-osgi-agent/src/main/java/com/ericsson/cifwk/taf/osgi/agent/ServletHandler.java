package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Handler of a registered route.
 *
 * @see com.ericsson.cifwk.taf.osgi.agent.ServletRouter
 * @see com.ericsson.cifwk.taf.osgi.agent.ServletRoute
 */
@API(Internal)
public interface ServletHandler {

    /**
     * Called when this handler is registered for a route matching the request.
     *
     * @param req     original request object
     * @param resp    original response object
     * @param matches route match result
     * @throws ServletException
     * @throws IOException
     */
    void handle(HttpServletRequest req, HttpServletResponse resp, MatchResult matches)
            throws ServletException, IOException;

}
