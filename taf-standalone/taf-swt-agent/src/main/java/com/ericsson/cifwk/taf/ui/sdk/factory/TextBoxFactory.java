package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtTextBox;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class TextBoxFactory extends AbstractComponentFactory<SwtTextBox> {

    public TextBoxFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotTextFactory());
    }

}
