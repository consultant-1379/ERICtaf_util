package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtButtonToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToggleButton;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotToggleButtonFactory extends SWTBotAbstractComponentFactory<Button, SWTBotToggleButton, SwtButtonToggle> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<Button> getBaseMatcher() {
        Matcher<Button> styleMatcher = withStyle(SWT.TOGGLE, "SWT.TOGGLE");
        return allOf(widgetOfType(Button.class), styleMatcher);
    }

    @Override
    protected SWTBotToggleButton create(Button widget) {
        return new SWTBotToggleButton(widget);
    }

    @Override
    protected SwtButtonToggle create(SWTBotToggleButton target) {
        return new SwtButtonToggle(target);
    }

}
