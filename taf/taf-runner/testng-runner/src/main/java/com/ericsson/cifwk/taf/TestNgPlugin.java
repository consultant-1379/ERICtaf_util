package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.datadriven.GenericDataProvider;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.execution.ContextSetterTestListener;
import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.ericsson.cifwk.taf.testng.CompositeTestNGListener;
import com.ericsson.cifwk.taf.testng.GroupsListener;
import com.ericsson.cifwk.taf.testng.SaveAssertsListener;
import com.ericsson.cifwk.taf.testng.TestOptionsListener;
import com.ericsson.cifwk.taf.testng.ThinkTimeListener;

/**
 *
 */
public class TestNgPlugin implements TafPlugin {

    @Override
    public void init() {
        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        testEventBus.register(new ContextSetterTestListener());
        testEventBus.register(new GenericDataProvider());
        testEventBus.register(new ThinkTimeListener());
        testEventBus.register(new TestOptionsListener());

        //TODO: create TAF Test API for method interception and refactor this into Test API + event subscription
        CompositeTestNGListener.addListener(new GroupsListener(), 5);
        CompositeTestNGListener.addListener(new SaveAssertsListener(), 54);
    }

    @Override
    public void shutdown() {
        // do nothing
    }

}
