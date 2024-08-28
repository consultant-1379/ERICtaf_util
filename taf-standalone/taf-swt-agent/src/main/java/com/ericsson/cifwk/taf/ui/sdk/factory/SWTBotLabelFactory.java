package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtLabel;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SWTBotLabelFactory extends SWTBotAbstractComponentFactory<Label, SWTBotLabel, SwtLabel> {

    @Override
    public SWTBotLabel create(Label widget) {
        return new SWTBotLabel(widget);
    }

    @Override
    protected SwtLabel create(SWTBotLabel swtBotWidget) {
        return new SwtLabel(swtBotWidget);
    }

    @Override
    protected Matcher<Label> getBaseMatcher() {
        return WidgetMatcherFactory.widgetOfType(Label.class);
    }

}
