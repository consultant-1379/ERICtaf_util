package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.Select;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SelectFactory extends AbstractComponentFactory<Select> {

    public SelectFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotComboFactory());
        registerFactory(new SWTBotCComboFactory());
    }
}
