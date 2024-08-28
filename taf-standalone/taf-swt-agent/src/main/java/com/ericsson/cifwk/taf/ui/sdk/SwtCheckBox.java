/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class SwtCheckBox extends SwtControl implements CheckBox {

    private SWTBotCheckBox target;

    public SwtCheckBox(SWTBotCheckBox target) {
        super(target);
        this.target = target;
    }

    @Override
    public String getValue() {
        return String.valueOf(target.isChecked());
    }

    @Override
    public void select() {
        target.select();
    }

    @Override
    public void deselect() {
        target.deselect();
    }

    @Override
    public boolean isSelected() {
        return target.isChecked();
    }
}
