package com.ericsson.cifwk.taf.swt.client;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.CheckBox;
import com.ericsson.cifwk.taf.ui.sdk.Select;
import com.ericsson.cifwk.taf.ui.sdk.SwtViewModel;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;

import java.util.List;

public class SelectionAndInputView extends SwtViewModel {
    @UiComponentMapping("#1")
    public Select notFoundSelection;

    @UiComponentMapping("#0")
    public Select selection;

    @UiComponentMapping(selectorType = SelectorType.DEFAULT, value = "#0")
    public TextBox textBox;

    @UiComponentMapping("#0")
    private CheckBox checkBox;

    @UiComponentMapping("{type = 'org.eclipse.swt.widgets.Button'}")
    public List<Button> allButtons;

    @UiComponentMapping("{type = 'org.eclipse.swt.widgets.Button'}")
    public Button oneOfManyButtons;

    public CheckBox getCheckBox() {
        return checkBox;
    }

}
