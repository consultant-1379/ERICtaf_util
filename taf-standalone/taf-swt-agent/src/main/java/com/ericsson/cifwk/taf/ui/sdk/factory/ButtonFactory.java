package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ButtonFactory extends AbstractComponentFactory<Button> {

    public ButtonFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotButtonFactory());
        registerFactory(new SWTBotToggleButtonFactory());
        registerFactory(new SWTBotToolbarButtonFactory());
        registerFactory(new SWTBotToolbarToggleButtonFactory());
    }
}
