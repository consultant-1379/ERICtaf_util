package com.ericsson.cifwk.taf.findbugs.sample.ui;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponentPredicates;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 01.09.2016
 */
public class TestWaitForComponentBeforeUsage {

    private static final String WAIT_FOR_COMPONENT_BEFORE_USAGE = "WaitForComponentBeforeUsage";

    private MyComponent component = new MyComponent();

    private MyViewModel view = new MyViewModel();

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void componentPredicateDefaultWait() {
        MyComponent component = new MyComponent();
        component.waitUntil(component, UiComponentPredicates.DISPLAYED);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void componentPredicateCustomWait() {
        component.waitUntil(component, UiComponentPredicates.DISPLAYED, 1000);
        component.focus();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void componentGenericPredicateDefaultWait() {
        component.waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return component.isDisplayed();
            }
        });
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void componentGenericPredicateCustomWait() {
        component.waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return component.isDisplayed();
            }
        }, 1000);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewDefaultWait() {
        view.waitUntilComponentIsDisplayed(component);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewCustomWait() {
        view.waitUntilComponentIsDisplayed(component, 1000);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewPredicateDefaultWait() {
        view.waitUntil(component, UiComponentPredicates.DISPLAYED);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewPredicateCustomWait() {
        view.waitUntil(component, UiComponentPredicates.DISPLAYED, 1000);
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewGenericPredicateDefaultWait() {
        view.waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return component.isDisplayed();
            }
        });
        component.click();
    }

    @AssertWarning(WAIT_FOR_COMPONENT_BEFORE_USAGE)
    public void viewGenericPredicateCustomWait() {
        view.waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return component.isDisplayed();
            }
        }, 1000);
        component.click();
    }

    @AssertNoWarning("*")
    public void waitBeforeUsageWithActionInTheMiddle() {
        MyComponent component = new MyComponent();
        component.waitUntil(component, UiComponentPredicates.DISPLAYED);
        Thread.currentThread();
        component.click();
    }

}
