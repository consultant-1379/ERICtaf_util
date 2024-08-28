package com.ericsson.cifwk.taf.eiffel;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class EmbeddedJetty {

    int JETTY_PORT = 0;

    private Server server;

    public static void main(String[] args) {
        EmbeddedJetty app = new EmbeddedJetty();
        app.start();
    }

    public void start() {
        server = new Server(JETTY_PORT);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new DefaultServlet()), "/*");
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JETTY_PORT = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class DefaultServlet extends HttpServlet {

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String configType = request.getParameter("type");
            if ("hosts".equals(configType)) {
                response.setContentType("application/json");
                InputStream input = getClass().getResourceAsStream("/hosts.json");
                if (input != null) {
                    String json = IOUtils.toString(input);
                    response.getWriter().write(json);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.getWriter().write("[]");
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else if ("properties".equals(configType)) {
                response.setContentType("text/plain");
                InputStream input = getClass().getResourceAsStream("/taf.properties");
                if (input != null) {
                    String props = IOUtils.toString(input);
                    response.getWriter().write(props);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.getWriter().write("");
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        }

    }

}
