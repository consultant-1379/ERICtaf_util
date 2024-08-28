/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.swt.agent.introspection;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class WidgetDto {

    private String type;

    private int index;

    private String id;

    private String text;

    private String toolTipText;

    private boolean active;

    private boolean enabled;

    private boolean visible;

    public WidgetDto() {
        // required by Gson
    }

    public WidgetDto(String type, int index, String id, String text, String toolTipText) {
        this.type = type;
        this.index = index;
        this.id = id;
        this.text = text;
        this.toolTipText = toolTipText;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public String getToolTipText() {
        return toolTipText;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
