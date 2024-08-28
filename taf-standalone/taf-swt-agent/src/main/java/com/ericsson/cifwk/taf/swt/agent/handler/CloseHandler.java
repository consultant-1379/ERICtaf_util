package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.ObjectLocator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class CloseHandler implements ServletHandler {

    private ObjectLocator objectLocator;

    public CloseHandler(ObjectLocator objectLocator) {
        this.objectLocator = objectLocator;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {
        objectLocator.reset();
        response.getWriter().write("OK");
    }

}
