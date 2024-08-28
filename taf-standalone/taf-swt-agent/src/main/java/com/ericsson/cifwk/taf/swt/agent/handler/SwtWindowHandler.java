package com.ericsson.cifwk.taf.swt.agent.handler;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.osgi.agent.ServletHandler;
import com.ericsson.cifwk.taf.swt.agent.WindowsManager;
import com.google.common.collect.Sets;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Primitives;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.Finder;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.MatchResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtWindowHandler implements ServletHandler {

    private static final Gson gson = new GsonBuilder()
            .disableInnerClassSerialization()
            .setExclusionStrategies(new SwtExclusionStrategy())
            .create();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, MatchResult matches) throws ServletException, IOException {
        String windowTitle = URLDecoder.decode(matches.group(1), "utf-8");
        final SWTBot bot = WindowsManager.findWindow(windowTitle);
        if (bot == null) {
            response.setStatus(404);
            response.getWriter().write(String.format("Window '%s' not found", windowTitle));
            return;
        }
        WindowDto windowDto = introspect(bot);
        gson.toJson(windowDto, response.getWriter());
    }

    private WindowDto introspect(final SWTBot bot) {
        final List<String> widgets = new ArrayList<String>();
        final List<MenuItemDto> menuItems = new ArrayList<MenuItemDto>();
        final List<String> widgetJsons = new CopyOnWriteArrayList<String>();
        WindowDto windowDto = new WindowDto(widgets, menuItems, widgetJsons);
        final AtomicReference<Integer> widgetsCount = new AtomicReference<Integer>(1);

        bot.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    runInternal();
                    // Please do not remove catch(Throwable) - needed for debug purposes!
                } catch (Exception e) {
                    // widgetJsons.add(e.getClass().getName());
                    widgetJsons.add(stackTraceAsString(e));
                }
            }

            private void runInternal() {
                final Finder finder = bot.getFinder();
                final Matcher<Widget> anyWidgetMatcher = new IsAnything<Widget>();
                final Matcher<MenuItem> menuItemMatcher = new IsAnything<MenuItem>();

                List<MenuItem> menus = finder.findMenus(menuItemMatcher);
                for (MenuItem menuItem : menus) {
                    menuItems.add(new MenuItemDto(menuItem.getText(), menuItem.getEnabled()));
                }

                List<Widget> controls = finder.findControls(anyWidgetMatcher);
                widgetsCount.set(controls.size());

                for (Widget widget : controls) {
                    widgets.add(widget.getClass().getName() + ": " + widget.toString());
                }

                for (Widget widget : controls) {
                    String widgetJson;
                    try {
                        widgetJson = gson.toJson(widget);
                    } catch (Exception e) {
                        widgetJson = String.format("Failed to create JSON from this widget: %s %s %s",
                                                   e.getClass().getName(),
                                                   e.getMessage(),
                                                   stackTraceAsString(e));
                    }
                    widgetJsons.add(widget.toString() + ": " + widgetJson);
                }
            }

        });

        List<String> widgetsSorted = new ArrayList<String>(widgets);
        Collections.sort(widgetsSorted);
        windowDto.setWidgetsSorted(widgetsSorted);
        return windowDto;
    }

    // To avoid importing Apache's commons-lang with ExceptionUtils.getStackTrace(e)...
    public static String stackTraceAsString(Throwable ex) {
        if (ex == null) {
            return "";
        }
        StringWriter str = new StringWriter();
        PrintWriter writer = new PrintWriter(str);
        try {
            ex.printStackTrace(writer);
            return str.getBuffer().toString();
        } finally {
            try {
                str.close();
                writer.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    protected static class WindowDto {

        private List<String> widgetsOriginal;

        private List<String> widgetsSorted;

        private List<MenuItemDto> menuItems;

        private List<String> widgetJsons;

        public WindowDto(List<String> widgets, List<MenuItemDto> menuItems, List<String> widgetJsons) {
            this.widgetsOriginal = widgets;
            this.menuItems = menuItems;
            this.widgetJsons = widgetJsons;
        }

        public void setWidgetsSorted(List<String> widgetsSorted) {
            this.widgetsSorted = widgetsSorted;
        }

        public List<String> getWidgetsOriginal() {
            return widgetsOriginal;
        }

        public List<String> getWidgetsSorted() {
            return widgetsSorted;
        }

        public List<MenuItemDto> getMenuItems() {
            return menuItems;
        }

        public List<String> getWidgetJsons() {
            return widgetJsons;
        }
    }

    protected static class MenuItemDto {

        private String text;

        private boolean enabled;

        public MenuItemDto(String text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }

        public String getText() {
            return text;
        }

        public boolean isEnabled() {
            return enabled;
        }

    }

    private static class SwtExclusionStrategy implements ExclusionStrategy {

        @SuppressWarnings("unchecked")
        private Set<Class<?>> excludedClasses = Sets.newHashSet(
                Shell.class, Display.class, Device.class, Image.class, Font.class, Cursor.class, Listener.class, ImageData.class);

        @SuppressWarnings("unchecked")
        private Set<Class<?>> includedNotNumericClasses = Sets.newHashSet(
                Collection.class, String.class, Map.class, Serializable.class);

        @Override
        public boolean shouldSkipClass(Class<?> candidate) {
            return excludedClasses.contains(candidate);
        }

        @Override
        public boolean shouldSkipField(FieldAttributes candidate) {
            Class<?> fieldClass = candidate.getDeclaredClass();
            String fieldName = candidate.getName();
            if ("data".equals(fieldName)) {
                return false;
            }
            return !(Primitives.isPrimitive(fieldClass) || Primitives.isWrapperType(fieldClass)
                    || isAcceptableClass(fieldClass));
        }

        private boolean isAcceptableClass(Class<?> fieldClass) {
            for (Class<?> clazz : includedNotNumericClasses) {
                if (clazz.isAssignableFrom(fieldClass)) {
                    return true;
                }
            }
            return false;
        }
    }

}
