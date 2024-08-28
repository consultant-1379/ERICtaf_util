package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.WindowsManager;
import com.ericsson.cifwk.taf.swt.agent.introspection.WindowDto;
import com.ericsson.cifwk.taf.swt.agent.introspection.WindowIntrospector;
import com.google.gson.Gson;
import org.eclipse.swtbot.swt.finder.SWTBot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WindowHandler implements ServletHandler {

    private static final Gson gson = new Gson();

    private WindowIntrospector introspector = new WindowIntrospector();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {
        String windowTitle = URLDecoder.decode(matches.group(1), "utf-8");
        SWTBot bot = WindowsManager.findWindow(windowTitle);
        WindowDto windowDto = introspector.introspect(bot);
        gson.toJson(windowDto, response.getWriter());
    }

}
