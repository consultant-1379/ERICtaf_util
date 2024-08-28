package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtLabel;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCLabel;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SWTBotCLabelFactory extends SWTBotAbstractComponentFactory<CLabel, SWTBotCLabel, SwtLabel> {

    @Override
    protected Matcher<CLabel> getBaseMatcher() {
        return WidgetMatcherFactory.widgetOfType(CLabel.class);
    }

    @Override
    protected SWTBotCLabel create(CLabel widget) {
        return new SWTBotCLabel(widget);
    }

    @Override
    protected SwtLabel create(SWTBotCLabel target) {
        return new SwtLabel(target);
    }

}
