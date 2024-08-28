package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtSelectCCombo;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

@API(Internal)
public class SWTBotCComboFactory extends SWTBotAbstractComponentFactory<CCombo, SWTBotCCombo, SwtSelectCCombo> {

    @Override
    protected Matcher<CCombo> getBaseMatcher() {
        return widgetOfType(CCombo.class);
    }

    @Override
    protected SWTBotCCombo create(CCombo widget) {
        return new SWTBotCCombo(widget);
    }

    @Override
    protected SwtSelectCCombo create(SWTBotCCombo target) {
        return new SwtSelectCCombo(target);
    }

}
