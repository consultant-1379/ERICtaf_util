package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtSelect;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

@API(Internal)
public class SWTBotComboFactory extends SWTBotAbstractComponentFactory<Combo, SWTBotCombo, SwtSelect> {

    @Override
    protected Matcher<Combo> getBaseMatcher() {
        return widgetOfType(Combo.class);
    }

    @Override
    protected SWTBotCombo create(Combo widget) {
        return new SWTBotCombo(widget);
    }

    @Override
    protected SwtSelect create(SWTBotCombo target) {
        return new SwtSelect(target);
    }

}
