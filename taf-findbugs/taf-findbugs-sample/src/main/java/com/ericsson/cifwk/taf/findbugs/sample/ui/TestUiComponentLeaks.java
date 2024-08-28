package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.google.common.collect.Lists;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import java.util.List;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.08.2016
 */
public class TestUiComponentLeaks extends GenericViewModel {

    public static final String UI_COMPONENT_LEAK = "UiComponentLeak";

    @AssertNoWarning("*")
    protected UiComponent notPublicLeakIsOk() {
        return null;
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public UiComponent publicComponentLeakIsForbidden() {
        return null;
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public MyComponent compositeComponentLeakIsForbidden() {
        return null;
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public MyAbstractComponent customComponentLeakIsForbidden() {
        return null;
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public List<UiComponent> publicComponentsLeakIsForbidden() {
        return Lists.newArrayList();
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public List<MyComponent> compositeComponentsLeakIsForbidden() {
        return Lists.newArrayList();
    }

    @AssertWarning(UI_COMPONENT_LEAK)
    public List<MyAbstractComponent> customComponentsLeakIsForbidden() {
        return Lists.newArrayList();
    }

    @AssertNoWarning("*")
    public List<String> returningParametrizedCollection() {
        return Lists.newArrayList();
    }

    @AssertNoWarning("*")
    public List returningRawCollection() {
        return Lists.newArrayList();
    }

}
