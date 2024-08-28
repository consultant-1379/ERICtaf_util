package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtRadioButton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;

@API(Internal)
public class SWTBotRadioFactory extends SWTBotAbstractComponentFactory<Button, SWTBotRadio, SwtRadioButton> {

    @Override
    @SuppressWarnings("unchecked")
    protected Matcher<Button> getBaseMatcher() {
        Matcher<Button> styleMatcher = withStyle(SWT.RADIO, "SWT.RADIO");
        return allOf(widgetOfType(Button.class), styleMatcher);
    }

    @Override
    protected SWTBotRadio create(Button widget) {
        return new SWTBotRadio(widget);
    }

    @Override
    protected SwtRadioButton create(SWTBotRadio target) {
        return new SwtRadioButton(target);
    }

}
