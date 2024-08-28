package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtButtonToolbar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarPushButton;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotToolbarButtonFactory extends SWTBotAbstractComponentFactory<ToolItem, SWTBotToolbarPushButton, SwtButtonToolbar> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<ToolItem> getBaseMatcher() {
        Matcher<ToolItem> styleMatcher = withStyle(SWT.PUSH, "SWT.PUSH");
        return allOf(widgetOfType(ToolItem.class), styleMatcher);
    }

    @Override
    protected SWTBotToolbarPushButton create(ToolItem widget) {
        return new SWTBotToolbarPushButton(widget);
    }

    @Override
    protected SwtButtonToolbar create(SWTBotToolbarPushButton target) {
        return new SwtButtonToolbar(target);
    }

}
