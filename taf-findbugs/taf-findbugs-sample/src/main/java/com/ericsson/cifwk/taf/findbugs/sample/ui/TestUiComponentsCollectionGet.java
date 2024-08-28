package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.09.2016
 */
public class TestUiComponentsCollectionGet {

    public static final String WARNING = "UiComponentsCollectionGet";

    @UiComponentMapping("whatever")
    List<UiComponent> items;

    @UiComponentMapping("whatever")
    List<Button> buttons;

    List<String> labels;

    @AssertWarning(WARNING)
    public void iteratingThroughListOfUiComponents() {
        for(int i = 0; i < items.size(); i++) {
            items.get(i).click();
        }
    }

    @AssertWarning(WARNING)
    public void iteratingThroughListOfButtons() {
        for(int i = 0; i < buttons.size(); i++) {
            buttons.get(i).click();
        }
    }

    @AssertWarning(WARNING)
    public void iteratingThroughListOfButtonsViaGetter() {
        for(int i = 0; i < getButtons().size(); i++) {
            getButtons().get(i).click();
        }
    }

    @AssertNoWarning(WARNING)
    public void iteratingThroughListOfButtonsProperly() {
        for(Button button : buttons) {
            button.click();
        }
    }

    @AssertNoWarning(WARNING)
    public void iteratingThroughListOfNonUiComponents() {
        for(int i = 0; i < labels.size(); i++) {
            labels.get(i);
        }
    }

    public List<Button> getButtons() {
        return buttons;
    }
}
