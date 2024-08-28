package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.RadioButton;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class RadioButtonFactory extends AbstractComponentFactory<RadioButton> {

    public RadioButtonFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotRadioFactory());
        registerFactory(new SWTBotToolbarRadioButtonFactory());
    }
}
