package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtTextBox extends SwtUiComponent implements TextBox {

    private final SWTBotText target;

    public SwtTextBox(SWTBotText target) {
        super(target);
        this.target = target;
    }

    @Override
    public String getText() {
        return target.getText();
    }

    @Override
    public void setText(String text) {
        target.setText(text);
    }

    @Override
    public void clear() {
        setText("");
    }

}
