package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtLink;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLink;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

@API(Internal)
public class SWTBotLinkFactory extends SWTBotAbstractComponentFactory<Link, SWTBotLink, SwtLink> {

    @Override
    protected Matcher<Link> getBaseMatcher() {
        return widgetOfType(Link.class);
    }

    @Override
    protected SWTBotLink create(Link widget) {
        return new SWTBotLink(widget);
    }

    @Override
    protected SwtLink create(SWTBotLink target) {
        return new SwtLink(target);
    }

}
