package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtCheckBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotCheckBoxFactory extends SWTBotAbstractComponentFactory<Button, SWTBotCheckBox, SwtCheckBox> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<Button> getBaseMatcher() {
        Matcher<Button> styleMatcher = withStyle(SWT.CHECK, "SWT.CHECK");
        return allOf(widgetOfType(Button.class), styleMatcher);
    }

    @Override
    protected SWTBotCheckBox create(Button widget) {
        return new SWTBotCheckBox(widget);
    }

    @Override
    protected SwtCheckBox create(SWTBotCheckBox target) {
        return new SwtCheckBox(target);
    }

}
