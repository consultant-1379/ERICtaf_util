package com.ericsson.cifwk.taf.swt.agent.selector;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.hamcrest.Matcher;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtWidgetFinder {

    private SWTBot bot;

    public SwtWidgetFinder(SWTBot bot) {
        this.bot = bot;
    }

    public Widget findComponent(Matcher<Widget> matcher) {
        return findComponent(matcher, 0);
    }

    public Widget findComponent(Matcher<Widget> matcher, int index) {
        List<Widget> foundComponents = findComponents(matcher);
        if (foundComponents.size() < index + 1) {
            return null;
        }
        return foundComponents.get(index);
    }

    public List<Widget> findComponents(Matcher<Widget> matcher) {
        return bot.getFinder().findControls(matcher);
    }

}
