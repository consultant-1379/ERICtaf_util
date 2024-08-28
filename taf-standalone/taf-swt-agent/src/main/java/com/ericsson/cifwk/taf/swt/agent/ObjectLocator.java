package com.ericsson.cifwk.taf.swt.agent;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtUiComponent;
import com.ericsson.cifwk.taf.ui.sdk.ViewModelSwtImpl;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.eclipse.swtbot.swt.finder.SWTBot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ObjectLocator {

    private static final Pattern WINDOWS_PATTERN = Pattern.compile("window:(.+)");

    private static final Pattern BOT_PATTERN = Pattern.compile("bot-for-window:(.+)");

    private static final Pattern SHELL_PATTERN = Pattern.compile("shell-for-window:(.+)");

    private static final Map<Object, String> keys = new HashMap<Object, String>();

    private static RemovalListener<String, Object> listener = new RemovalListener<String, Object>() {
        @Override
        public void onRemoval(RemovalNotification<String, Object> notification) {
            keys.remove(notification.getValue());
        }
    };

    private static final Cache<String, Object> objects = CacheBuilder.newBuilder().maximumSize(1000).removalListener(listener).build();

    public Object get(String objectId) {
        Matcher matcher = WINDOWS_PATTERN.matcher(objectId);
        if (matcher.matches()) {
            String windowTitle = matcher.group(1);
            SWTBot windowBot = findWindow(windowTitle);
            if (windowBot == null) {
                return null;
            }
            return new ViewModelSwtImpl(windowBot);
        }

        matcher = BOT_PATTERN.matcher(objectId);
        if (matcher.matches()) {
            String windowTitle = matcher.group(1);
            return findWindow(windowTitle);
        }

        matcher = SHELL_PATTERN.matcher(objectId);
        if (matcher.matches()) {
            Object shell = objects.getIfPresent(objectId);
            if (shell == null) {
                String windowTitle = matcher.group(1);
                shell = findShell(windowTitle);
                put(objectId, shell);
            }
            return shell;
        }

        return objects.getIfPresent(objectId);
    }

    public String put(Object object) {
        if (keys.containsKey(object)) {
            return keys.get(object);
        }

        String objectId = UUID.randomUUID().toString();
        put(objectId, object);
        return objectId;
    }

    public void put(String objectId, Object object) {
        Object oldValue = objects.getIfPresent(objectId);
        keys.remove(oldValue);

        objects.put(objectId, object);
        keys.put(object, objectId);

        Preconditions.checkState(objects.size() == keys.size());
    }

    public void reset() {
        objects.invalidateAll();
        objects.cleanUp();

        Preconditions.checkState(objects.size() == 0);
        Preconditions.checkState(keys.size() == 0);
    }

    protected SWTBot findWindow(String windowTitle) {
        return WindowsManager.findWindow(windowTitle);
    }

    private SwtUiComponent findShell(String windowTitle) {
        return new SwtUiComponent(findWindow(windowTitle).shell(windowTitle));
    }

}
