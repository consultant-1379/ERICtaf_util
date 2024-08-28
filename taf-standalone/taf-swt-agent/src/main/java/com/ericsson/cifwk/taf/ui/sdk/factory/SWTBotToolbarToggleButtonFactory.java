package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtButtonToolbar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarToggleButton;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotToolbarToggleButtonFactory extends SWTBotAbstractComponentFactory<ToolItem, SWTBotToolbarToggleButton, SwtButtonToolbar> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<ToolItem> getBaseMatcher() {
        Matcher<ToolItem> styleMatcher = withStyle(SWT.CHECK, "SWT.CHECK");
        return allOf(widgetOfType(ToolItem.class), styleMatcher);
    }

    @Override
    protected SWTBotToolbarToggleButton create(ToolItem widget) {
        return new SWTBotToolbarToggleButton(widget);
    }

    @Override
    protected SwtButtonToolbar create(SWTBotToolbarToggleButton target) {
        return new SwtButtonToolbar(target);
    }

}
