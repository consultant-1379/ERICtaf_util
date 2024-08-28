package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtRadioButtonToolbar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotToolbarRadioButtonFactory extends SWTBotAbstractComponentFactory<ToolItem, SWTBotToolbarRadioButton, SwtRadioButtonToolbar> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<ToolItem> getBaseMatcher() {
        Matcher<ToolItem> styleMatcher = withStyle(SWT.RADIO, "SWT.RADIO");
        return allOf(widgetOfType(ToolItem.class), styleMatcher);
    }

    @Override
    protected SWTBotToolbarRadioButton create(ToolItem widget) {
        return new SWTBotToolbarRadioButton(widget);
    }

    @Override
    protected SwtRadioButtonToolbar create(SWTBotToolbarRadioButton target) {
        return new SwtRadioButtonToolbar(target);
    }

}
