package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.SwtLabel;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class LabelFactory extends AbstractComponentFactory<SwtLabel> {

    public LabelFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotLabelFactory());
        registerFactory(new SWTBotCLabelFactory());
    }

}
