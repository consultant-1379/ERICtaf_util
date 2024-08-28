package com.ericsson.cifwk.taf.ui.sdk.factory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ui.sdk.Link;
import org.eclipse.swtbot.swt.finder.SWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class LinkFactory extends AbstractComponentFactory<Link> {

    public LinkFactory(SWTBot bot) {
        super(bot);
        registerFactory(new SWTBotLinkFactory());
    }

}
