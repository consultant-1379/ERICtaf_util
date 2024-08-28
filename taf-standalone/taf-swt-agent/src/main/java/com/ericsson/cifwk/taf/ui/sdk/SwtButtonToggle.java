package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtButtonToggle extends SwtUiComponent implements Button {

    public SwtButtonToggle(AbstractSWTBot<?> target) {
        super(target);
    }

}
