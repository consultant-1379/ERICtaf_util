package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLink;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtLink extends SwtControl implements Link {

    private SWTBotLink target;

    public SwtLink(SWTBotLink target) {
        super(target);
        this.target = target;
    }

    @Override
    public String getUrl() {
        return target.getText();
    }
}
