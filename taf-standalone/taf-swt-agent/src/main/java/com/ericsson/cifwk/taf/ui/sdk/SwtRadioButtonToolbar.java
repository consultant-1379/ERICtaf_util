package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarRadioButton;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtRadioButtonToolbar extends SwtUiComponent implements RadioButton {

    private SWTBotToolbarRadioButton target;

    public SwtRadioButtonToolbar(SWTBotToolbarRadioButton target) {
        super(target);
        this.target = target;
    }

    @Override
    public String getValue() {
        return String.valueOf(target.isChecked());
    }

}
