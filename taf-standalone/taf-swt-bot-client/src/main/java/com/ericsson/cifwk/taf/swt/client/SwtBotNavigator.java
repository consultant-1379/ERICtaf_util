package com.ericsson.cifwk.taf.swt.client;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.SwtUiNavigator;
import com.ericsson.cifwk.taf.ui.sdk.ViewModel;
import org.eclipse.swtbot.swt.finder.SWTBot;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public final class SwtBotNavigator {

    private static final String SWT_BOT_PREFIX = "bot-for-window:";

    private SwtUiNavigator swtNavigator;

    public SwtBotNavigator(String host, int port) {
        swtNavigator = new SwtUiNavigator(host, port);
    }

    public SWTBot getSwtBot(String windowName) {
        return swtNavigator.createProxy(SWTBot.class, SWT_BOT_PREFIX + windowName);
    }

    public ViewModel getView(String windowName) {
        return swtNavigator.getView(windowName);
    }

    public List<String> getViews() {
        return swtNavigator.getViews();
    }

    public <T> T as(Object widget, Class<T> clazz) {
        return swtNavigator.as(widget, clazz);
    }

    public void reset() {
        swtNavigator.reset();
    }

}
