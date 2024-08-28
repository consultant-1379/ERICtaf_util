package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.MethodInvocation;
import com.ericsson.cifwk.taf.swt.agent.MethodInvoker;
import com.ericsson.cifwk.taf.swt.agent.MethodResponse;
import com.ericsson.cifwk.taf.swt.agent.ObjectLocator;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.WidgetWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetHandler implements ServletHandler {

    private static final Gson json = new Gson();

    private static final Set<Class<?>> simpleObjectClasses = new HashSet<Class<?>>();

    static {
        simpleObjectClasses.add(Integer.class);
        simpleObjectClasses.add(Long.class);
        simpleObjectClasses.add(String.class);
        simpleObjectClasses.add(Boolean.class);
    }

    private MethodInvoker invoker;

    private ObjectLocator objectLocator;

    public WidgetHandler(ObjectLocator objectLocator) {
        this.objectLocator = objectLocator;
        this.invoker = new MethodInvoker();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {

        // object location
        String widgetId = URLDecoder.decode(matches.group(1), "UTF-8");
        Object widget = objectLocator.get(widgetId);
        if (widget == null) {
            throw new IllegalArgumentException(String.format("No widget found for id: '%s'", widgetId));
        }

        // method invocation
        Object result;
        try {
            MethodInvocation invocation = getEntity(request);
            result = invoker.invoke(widget, invocation);
        } catch (RuntimeException e) {
            result = com.ericsson.cifwk.taf.osgi.agent.AgentServlet.getMessages(e);
        }

        // serializing invocation result
        PrintWriter writer = response.getWriter();
        writer.write(json.toJson(toResponse(result)));
        writer.flush();
    }

    protected MethodInvocation getEntity(HttpServletRequest request) throws JsonSyntaxException, JsonIOException, IOException {
        return json.fromJson(request.getReader(), MethodInvocation.class);
    }

    protected MethodResponse toResponse(Object newWidget) {
        if (newWidget == null) {
            return new MethodResponse(null, null);
        }

        Class<?> resultClass = newWidget.getClass();
        if (simpleObjectClasses.contains(resultClass)) {
            return new MethodResponse(newWidget.toString(), newWidget.getClass().getName());
        }
        if (WidgetWrapper.class.equals(resultClass)) {
            WidgetWrapper wrapper = (WidgetWrapper) newWidget;
            newWidget = wrapper.getTarget();
        }

        String newWidgetId = objectLocator.put(newWidget);
        String newWidgetType = getFirstPublicType(resultClass).getName();
        return new MethodResponse(newWidgetId, newWidgetType);
    }

    protected Class<?> getFirstPublicType(Class<?> returnType) {
        if (!Modifier.isPublic(returnType.getModifiers()) || UiComponent.class.isAssignableFrom(returnType)) {
            Class<?>[] interfaces = returnType.getInterfaces();
            if (interfaces.length > 0) {
                return interfaces[0];
            }
            return getFirstPublicType(returnType.getSuperclass());
        }
        return returnType;
    }

}
