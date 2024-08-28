package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtLabel extends SwtControl implements Label {

    public SwtLabel(AbstractSWTBotControl<?> target) {
        super(target);
    }

}
