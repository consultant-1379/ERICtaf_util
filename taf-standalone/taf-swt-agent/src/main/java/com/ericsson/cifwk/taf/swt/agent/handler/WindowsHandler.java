package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.WindowsManager;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WindowsHandler implements ServletHandler {

    private static final Gson json = new Gson();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {
        List<String> windows = WindowsManager.listWindows();
        json.toJson(windows, response.getWriter());
    }

}
