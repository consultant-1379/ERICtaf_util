package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtCheckBox;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class CheckBoxFactory extends AbstractComponentFactory<SwtCheckBox> {

    public CheckBoxFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotCheckBoxFactory());
    }

}
