package com.ericsson.cifwk.taf.swt.agent.selector.matchers;

import org.eclipse.swt.widgets.Widget;

// Subsitute for SWT's Text widget, which has some native lib init problems in Jenkins which runs on 64-bit Solaris
public class TextWidget extends Widget {
    private SimpleWidget simpleWidget;

    public TextWidget(Widget parent, int style) {
        super(parent, style);
        simpleWidget = new SimpleWidget(this, 1);
    }

    public String getLineDelimiter() {
        return "";
    }

    public String getText() {
        return "";
    }

    public int getCaretPosition() {
        return -1;
    }

    public boolean isEnabled() {
        return true;
    }

    public String getToolTipText() {
        return "";
    }

    public SimpleWidget getSimple() {
        return simpleWidget;
    }
}
