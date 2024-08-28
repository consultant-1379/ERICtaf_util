package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.swt.agent.selector.GeneralSelectorParser;
import com.ericsson.cifwk.taf.swt.agent.selector.MatcherBuilder;
import com.ericsson.cifwk.taf.swt.agent.selector.SwtSelector;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public abstract class SWTBotAbstractComponentFactory<W extends Widget, B extends AbstractSWTBot<W>, T extends UiComponent> {

    private static final GeneralSelectorParser selectorParser = new GeneralSelectorParser();

    protected abstract Matcher<W> getBaseMatcher();

    boolean canCreate(Widget widget) {
        return getBaseMatcher().matches(widget);
    }

    protected abstract B create(W widget);

    protected abstract T create(B target);

    protected int getIndex(String selector) {
        return getSelector(selector).getIndex();
    }

    protected Matcher<Widget> getMatcher(String selector) {
        SwtSelector swtSelector = getSelector(selector);
        return new MatcherBuilder(swtSelector).withMatcher(getBaseMatcher()).toMatcher();
    }

    private SwtSelector getSelector(String selector) {
        return selectorParser.parse(selector);
    }

}
