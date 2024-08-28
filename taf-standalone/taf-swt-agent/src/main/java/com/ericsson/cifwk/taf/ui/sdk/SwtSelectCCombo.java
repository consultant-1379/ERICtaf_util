package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Throwables;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtSelectCCombo extends SwtControl implements Select {

    private SWTBotCCombo target;

    public SwtSelectCCombo(SWTBotCCombo target) {
        super(target);
        this.target = target;
    }

    @Override
    public void selectByValue(String optionValue) {
        try {
            target.setSelection(Integer.parseInt(optionValue));
        } catch (NumberFormatException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void selectByTitle(String optionTitle) {
        target.setSelection(optionTitle);
    }

    @Override
    public String getValue() {
        checkIfMultipleSelection();
        return String.valueOf(target.selectionIndex());
    }

    @Override
    public String getText() {
        checkIfMultipleSelection();
        return target.selection();
    }

    @Override
    public List<Option> getSelectedOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Option> getAllOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearSelection() {
        throw new UnsupportedOperationException();
    }

    protected void checkIfMultipleSelection() {
        if (getSelectedOptions().size() > 1) {
            throw new IllegalStateException("Multiple values are selected");
        }
    }

}
