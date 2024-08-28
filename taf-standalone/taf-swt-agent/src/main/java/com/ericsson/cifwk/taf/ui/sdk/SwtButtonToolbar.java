package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtButtonToolbar extends SwtUiComponent implements Button {

    public SwtButtonToolbar(SWTBotToolbarButton target) {
        super(target);
    }

}
