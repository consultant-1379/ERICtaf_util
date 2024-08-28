package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotButtonFactory extends SWTBotAbstractComponentFactory<Button, SWTBotButton, SwtButton> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<Button> getBaseMatcher() {
        Matcher<Button> styleMatcher = withStyle(SWT.PUSH, "SWT.PUSH");
        return WidgetMatcherFactory.allOf(widgetOfType(Button.class), styleMatcher);
    }

    @Override
    public SWTBotButton create(Button widget) {
        return new SWTBotButton(widget);
    }

    @Override
    protected SwtButton create(SWTBotButton swtBotWidget) {
        return new SwtButton(swtBotWidget);
    }

}
