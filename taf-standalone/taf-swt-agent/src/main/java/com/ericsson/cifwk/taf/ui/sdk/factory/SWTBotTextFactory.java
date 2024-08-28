package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtTextBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.hamcrest.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

@API(Internal)
public class SWTBotTextFactory extends SWTBotAbstractComponentFactory<Text, SWTBotText, SwtTextBox> {

    @Override
    protected Matcher<Text> getBaseMatcher() {
        return widgetOfType(Text.class);
    }

    @Override
    protected SWTBotText create(Text widget) {
        return new SWTBotText(widget);
    }

    @Override
    protected SwtTextBox create(SWTBotText target) {
        return new SwtTextBox(target);
    }

}
