package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.WindowsManager;
import com.google.common.io.ByteStreams;
import org.eclipse.swtbot.swt.finder.SWTBot;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ScreenshotHandler implements ServletHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {
        String windowTitle = URLDecoder.decode(matches.group(1), "utf-8");
        SWTBot bot = WindowsManager.findWindow(windowTitle);
        File tempFile = File.createTempFile("screenshot", ".png");
        bot.captureScreenshot(tempFile.getAbsolutePath());
        response.setContentType("image/png");
        ByteStreams.copy(new FileInputStream(tempFile), response.getOutputStream());
    }

}
