package com.ericsson.cifwk.taf.ui.sdk;

import com.ericsson.cifwk.meta.API;
import org.eclipse.swt.widgets.Widget;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class WidgetWrapper extends UiComponentAdapter {

    private Widget target;

    public WidgetWrapper(Widget target) {
        this.target = target;
    }

    public Widget getTarget() {
        return target;
    }

    /**
     * AbstractUIComponent methods
     */
    @Override
    public String getId() {
        return target.getClass().getName() + target.hashCode();
    }

    @Override
    public String getComponentName() {
        return target.toString();
    }

    @Override
    public boolean isDisplayed() {
        return !target.isDisposed();
    }

    @Override
    public boolean exists() {
        return !target.isDisposed();
    }

    @Override
    public String getProperty(String propertyName) {
        return target.getData(propertyName).toString();
    }

}
