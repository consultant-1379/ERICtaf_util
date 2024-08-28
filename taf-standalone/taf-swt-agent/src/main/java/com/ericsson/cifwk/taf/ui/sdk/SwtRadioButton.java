package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtRadioButton extends SwtControl implements RadioButton {

    private SWTBotRadio target;

    public SwtRadioButton(SWTBotRadio target) {
        super(target);
        this.target = target;
    }

    @Override
    public String getValue() {
        return String.valueOf(target.isSelected());
    }

}
